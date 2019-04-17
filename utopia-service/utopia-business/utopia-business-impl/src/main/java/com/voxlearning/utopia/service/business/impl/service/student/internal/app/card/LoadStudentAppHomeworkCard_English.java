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

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataContext;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.ENGLISH;

/**
 * 学生App英语作业卡
 * Created by Shuai Huan on 2016/1/14.
 */
@Named
public class LoadStudentAppHomeworkCard_English extends AbstractStudentAppIndexDataLoader {

    public StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {

        NewHomework.Location location = context.__englishNormalHomeworkLocations.stream()
                .filter(t -> t.getClazzGroupId() == 0 || context.__groupIds.contains(t.getClazzGroupId()))
                .filter(t -> !t.isChecked())
                .filter(t -> t.getEndTime() > context.timestamp)
                .filter(t -> t.getStartTime() != 0 && t.getStartTime() <= context.timestamp)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);

        if (location == null) return context;

        context.__ignoreMakeUpEnglishHomework = true;
        NewHomework mock = new NewHomework();
        mock.setId(location.getId());
        mock.setCreateAt(new Date(location.getCreateTime()));

        Set<Long> finishedStudentIds = Collections.emptySet();
        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
        if (accomplishment != null && accomplishment.getDetails() != null) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }
        if (finishedStudentIds.contains(context.getStudent().getId())) return context;

        Map<String, Object> card = generateHomeworkCard(context, mock.getId());

        if (card != null) {
            context.__homeworkCards.add(card);
        }
        return context;
    }

    private Map<String, Object> generateHomeworkCard(StudentIndexDataContext context, String homeworkId) {
        Long studentId = context.getStudent().getId();
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();

        int practiceCount = newHomework.getPractices().size();
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        int finishPracticeCount = 0;
        if (newHomeworkResult != null && newHomeworkResult.getPractices() != null) {
            finishPracticeCount = newHomeworkResult.getPractices().values().stream().filter(nhr -> nhr.getFinishAt() != null).collect(Collectors.toList()).size();
        }

        if (finishPracticeCount >= practiceCount) {
            return null;
        }


        List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
        resultMap.put("homeworkId", homeworkId);
        resultMap.put("endDate", newHomework.getEndTime());
        resultMap.put("finishCount", finishPracticeCount);
        resultMap.put("homeworkCount", practiceCount);
        resultMap.put("homeworkType", ENGLISH);
        resultMap.put("desc", "英语作业");
        resultMap.put("makeup", false);
        resultMap.put("subject", newHomework.getSubject().name());
        resultMap.put("types", types);
        resultMap.put("startComment", finishPracticeCount>0 ? "继续作业" : "开始作业");
        return resultMap;
    }
}
