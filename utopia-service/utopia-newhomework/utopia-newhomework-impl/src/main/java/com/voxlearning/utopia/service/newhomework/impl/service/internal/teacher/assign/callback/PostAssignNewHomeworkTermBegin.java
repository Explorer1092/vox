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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.queue.BusinessQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.utopia.service.user.api.constants.UserBehaviorType.TERM_BEGIN_TEACHER_HOMEWORK_COUNT;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/17
 */
@Named
public class PostAssignNewHomeworkTermBegin extends NewHomeworkSpringBean implements PostAssignHomework {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    @Inject private BusinessQueueProducer businessQueueProducer;

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        if (NewSchoolYearActivity.isInTermBeginPeriod() || RuntimeMode.le(Mode.STAGING)) {
            // 活动期内 添加抽奖机会
            BusinessEvent event = new BusinessEvent();
            event.setType(BusinessEventType.TEACHER_TERM_BEGIN_ADD_LOTTERY_CHANCE);
            event.getAttributes().put("teacherId", teacher.getId());
            int chance = 0;

            switch (context.getHomeworkSourceType()) {
                case App:
                    chance = 5;
                    break;
                case Web:
                    chance = 2;
                    break;
                default:
                    break;
            }

            event.getAttributes().put("freeChance", chance);

            Message message = Message.newMessage().withStringBody(JsonUtils.toJson(event));
            businessQueueProducer.getProducer().produce(message);

            // 记录布置作业次数
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_incUserBehaviorCount(TERM_BEGIN_TEACHER_HOMEWORK_COUNT, teacher.getId(), 1L,
                            NewSchoolYearActivity.getSummerEndDate())
                    .awaitUninterruptibly();
        }
    }
}
