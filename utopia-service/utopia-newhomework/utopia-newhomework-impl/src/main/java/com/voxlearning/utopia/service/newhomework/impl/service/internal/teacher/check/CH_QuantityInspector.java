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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.TEACHER_HOMEWORK_CHEATING;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/2
 */
@Named
public class CH_QuantityInspector extends SpringContainerSupport implements CheckHomeworkTask {

    @Inject private UserPopupServiceClient userPopupServiceClient;

    @Override
    public void execute(CheckHomeworkContext context) {
        // 作业题量过少，没有奖励
        if (!enough(context)) {
            context.setHomeworkQuantityNotEnough(true);
            String text = "您好，本次作业题量异常，不属于我们鼓励的作业行为，已取消此次园丁豆奖励！";
            userPopupServiceClient.createPopup(context.getTeacherId()).content(text).type(TEACHER_HOMEWORK_CHEATING)
                    .category(LOWER_RIGHT).create();

            // 重置积分计算结果
            context.setDetail(new CheckHomeworkIntegralDetail(context.getHomeworkId(), 0, 0D, 0, 0));
        }
    }

    private boolean enough(CheckHomeworkContext context) {
        int count = 0;
        // 如果作业包含基础练习或绘本，就给园丁豆
        // 否则，计算Question数量，总量小于3，则不给园丁豆
        for (NewHomeworkPracticeContent practice : context.getHomework().getPractices()) {
            if (ObjectiveConfigType.BASIC_APP.equals(practice.getType())
                    || ObjectiveConfigType.READING.equals(practice.getType())
                    || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(practice.getType())
                    || ObjectiveConfigType.NATURAL_SPELLING.equals(practice.getType())
                    || ObjectiveConfigType.NEW_READ_RECITE.equals(practice.getType())
                    || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(practice.getType())
                    || ObjectiveConfigType.DUBBING.equals(practice.getType())
                    || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(practice.getType())
                    || ObjectiveConfigType.LEVEL_READINGS.equals(practice.getType())
                    || ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(practice.getType())
                    || ObjectiveConfigType.ORAL_COMMUNICATION.equals(practice.getType())
                    || ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(practice.getType())) {
                return true;
            } else if (ObjectiveConfigType.KEY_POINTS.equals(practice.getType()) || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(practice.getType())) {
                count += practice.getApps().stream()
                        .filter(Objects::nonNull)
                        .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                        .mapToInt(app -> app.getQuestions().size())
                        .sum();
            } else {
                count += practice.getQuestions().size();
            }
        }
        return count >= 3;
    }
}
