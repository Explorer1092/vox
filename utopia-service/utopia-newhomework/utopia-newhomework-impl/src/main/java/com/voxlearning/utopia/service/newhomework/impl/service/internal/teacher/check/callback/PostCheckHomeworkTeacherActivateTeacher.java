/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.queue.BusinessQueueProducer;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostCheckHomeworkTeacherActivateTeacher extends SpringContainerSupport implements PostCheckHomework {

    @Inject private BusinessQueueProducer businessQueueProducer;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        /*User invitee = context.getTeacher();
        // 判断老师是否已经被认证
        if (invitee.fetchCertificationState() != SUCCESS) return;
        // 判断班级是否是正规班级
        if (context.getClazz().getClassType() != ClazzType.PUBLIC.getType()) return;
        // 判断是否有8人完成作业
        int threshold = RuntimeMode.lt(Mode.STAGING) ? 1 : 8;
        NewAccomplishment accomplishment = context.getAccomplishment();
        if (accomplishment == null || accomplishment.size() < threshold) return;

        BusinessEvent event = new BusinessEvent();
        event.setType(BusinessEventType.TEACHER_ACTIVATE_TEACHER_FINISH);
        event.getAttributes().put("inviteeId", invitee.getId());

        Message message = Message.newMessage().withStringBody(JsonUtils.toJson(event));
        businessQueueProducer.getProducer().produce(message);*/
    }
}
