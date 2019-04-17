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

package com.voxlearning.utopia.service.business.impl.service.student.spr;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.constant.IdentificationWishType;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/14/2015
 */
@Slf4j
public abstract class MissionCreationTemplate extends BusinessServiceSpringBean {

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;

    @Inject private MissionCreationTemplateManager missionCreationTemplateManager;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Getter private final WishType type;

    protected MissionCreationTemplate() {
        IdentificationWishType annotation = getClass().getAnnotation(IdentificationWishType.class);
        type = annotation.value();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        missionCreationTemplateManager.register(this);
    }

    final public MapMessage create(MissionCreationContext context) {
        if (null == context) {
            return MapMessage.errorMessage("设置任务失败");
        }
        if (StringUtils.isBlank(context.getMission()) || context.getTotalCount() == null
                || context.getTotalCount() < 1 || context.getTotalCount() > 20) {
            return MapMessage.errorMessage("设置任务失败");
        }
        Map<Long, User> users = userLoaderClient.loadUsers(Arrays.asList(context.getParentId(), context.getStudentId()));
        User student = users.get(context.getStudentId());
        User parent = users.get(context.getParentId());
        if (null == student || null == parent) {
            return MapMessage.errorMessage("设置任务失败");
        }
        // 判断家长和学生关系的正确性
        List<StudentParentRef> parents = studentLoaderClient.loadStudentParentRefs(context.getStudentId());
        Set<Long> parentIds = parents.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
        if (!parentIds.contains(context.getParentId())) {
            return MapMessage.errorMessage("您不是这个孩子的家长");
        }
        // 判断家长是否能够设置任务
        MapMessage mesg = canSetMission(context);
        if (!mesg.isSuccess()) return mesg;
        // 设置任务
        asyncBusinessCacheService.StudentParentRewardCacheManager_turnOn(student.getId()).awaitUninterruptibly();
        mesg = save(context);
        if (!mesg.isSuccess()) return mesg;
        // 给学生发消息
        String pattern = "家长设立了新目标：{0} {1}次，完成后将会奖励{2}";
        messageCommandServiceClient.getMessageCommandService().sendUserMessage(context.getStudentId(), MessageFormat.format(pattern,
                context.getMission(), context.getTotalCount(),
                context.getWishType() == WishType.INTEGRAL ? context.getIntegral() + "学豆" : context.getWish()));
        return mesg;
    }

    abstract protected MapMessage canSetMission(MissionCreationContext context);

    abstract protected MapMessage save(MissionCreationContext context);
}
