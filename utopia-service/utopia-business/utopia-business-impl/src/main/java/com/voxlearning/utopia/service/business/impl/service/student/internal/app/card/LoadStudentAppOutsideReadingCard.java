/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课外阅读任务卡片
 * @author majianxin
 */
@Named
public class LoadStudentAppOutsideReadingCard extends AbstractStudentAppIndexDataLoader {

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {

        List<OutsideReading> unFinishedOutsideReadings = outsideReadingLoaderClient.loadUnFinishedOutsideReadings(context.getStudent().getId());
        if (CollectionUtils.isNotEmpty(unFinishedOutsideReadings)) {
            Map<String, Object> card = generateCard(unFinishedOutsideReadings.get(0));
            context.__outsideReadingCards.add(card);
        }

        return context;
    }

    private Map<String, Object> generateCard(OutsideReading reading) {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("readingId", reading.getId());
        String desc = "课外阅读";
        if (reading.isTerminated()) {
            desc = "课外阅读补做";
        }
        cardMap.put("desc", desc);
        cardMap.put("endDate", reading.getEndTime().getTime());
        cardMap.put("subject", reading.getSubject());
        cardMap.put("startComment", "开始作业");
        cardMap.put("homeworkType", "EXPAND_BASIC_REVIEW_MATH");
        cardMap.put("types", Collections.singletonList("BASIC_REVIEW"));

        cardMap.put("url", NewHomeworkConstants.STUDENT_OUTSIDE_READING_BOOKSHELF_URL);
        return cardMap;
    }
}
