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

package com.voxlearning.utopia.service.afenti.impl.service.processor.order;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.context.OrderPaySuccessContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增加同学购买通知信息
 * 增加邀请中心购小红点，提示用户有同班同学购买成功
 *
 * @author peng.zhang.a
 * @since 16-7-26
 */
@Named
public class AOPS_NotifyClassmatesPaySuccess extends SpringContainerSupport implements IAfentiTask<OrderPaySuccessContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject AsyncGroupServiceClient asyncGroupServiceClient;

    @Override
    public void execute(OrderPaySuccessContext context) {
        if (context.getUserId() == 0 || StringUtils.isEmpty(context.getProductServiceType())) {
            return;
        }
        Subject subject = AfentiUtils.getSubject(OrderProductServiceType.safeParse(context.getProductServiceType()));

        List<Long> classmateIds = new ArrayList<>();
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(context.getUserId());
        if (clazz != null) {
            classmateIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazz.getId())
                    .stream()
                    .filter(o -> !o.equals(context.getUserId()))
                    .collect(Collectors.toList());
        }

        asyncAfentiCacheService.AfentiPaidSuccessClassmatesCacheManager_addPaidSuccessMsg(context.getUserId(), classmateIds, subject)
                .awaitUninterruptibly();
        asyncAfentiCacheService.AfentiPromptCacheManager_record(classmateIds, subject, AfentiPromptType.invitation)
                .awaitUninterruptibly();
    }
}
