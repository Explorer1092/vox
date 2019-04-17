/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.service.processor.invitation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.impl.dao.UserActivatedProductPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-7-19
 */
@Named
public class LIM_LoadBaseMessage extends SpringContainerSupport implements IAfentiTask<LoadInvitationMsgContext> {

    @Inject private UserActivatedProductPersistence userActivatedProductPersistence;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private UserLoaderClient userLoaderClient;

    @Override
    public void execute(LoadInvitationMsgContext context) {
        // 加载同班同学,包括用户自己 有一些测试班级会有很多用户，限制显示100个用户
        context.getClassmateMap().put(context.getUser().getId(), context.getUser());
        Map<Long, User> students = new HashMap<>();
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(context.getUser().getId());
        if (clazz != null) {
            List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazz.getId())
                    .stream()
                    .filter(o -> !o.equals(context.getUser().getId()))
                    .limit(100)
                    .collect(Collectors.toList());
            students = userLoaderClient.loadUsers(userIds);
        }

        context.getClassmateMap().putAll(students);
        // 加载学科
        OrderProductServiceType orderProductServiceType = AfentiUtils.getOrderProductServiceType(context.getSubject());
        context.setOrderProductServiceType(orderProductServiceType == null ? "" : orderProductServiceType.name());

        // 加载同班同学购买阿分题订单
        Set<Long> studentIds = context.getClassmateMap().keySet();
        context.getUserOrderMap().putAll(userActivatedProductPersistence.loadByUserIds(studentIds));
        context.getResult().put("userId", context.getUser().getId());
    }
}