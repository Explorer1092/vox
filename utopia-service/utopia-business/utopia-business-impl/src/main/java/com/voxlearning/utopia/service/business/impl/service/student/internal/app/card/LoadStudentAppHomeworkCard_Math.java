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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataContext;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.MATH;

/**
 * 学生App数学作业卡（新体系）
 * Created by Shuai Huan on 2016/1/20.
 */
@Named
public class LoadStudentAppHomeworkCard_Math extends AbstractStudentAppIndexDataLoader {

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {

        // 小于1.9.0版本的客户端，不显示数学作业卡
        // 1.9.0之后的版本才走了作业卡的配置体系
        // 目前数学作业只在2.1.0以上版本才可以做作业。其他版本的作业卡会提示升级
        if (VersionUtil.compareVersion(context.ver, "1.9.0") < 0) {
            return context;
        }

        NewHomework.Location location = context.__mathNormalHomeworkLocations.stream()
                .filter(t -> context.__groupIds.contains(t.getClazzGroupId()))
                .filter(t -> !t.isChecked())
                .filter(t -> t.getEndTime() > context.timestamp)
                .filter(t -> t.getStartTime() <= context.timestamp)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);

        if (location == null) return context;
        context.__ignoreMakeUpMathHomework = true;

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

        // 当存在未检查的未过期的未完成的已经开始的数学作业时才可能显示数学作业卡
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

        List<NewHomeworkPracticeContent> practiceContents = newHomework.getPractices();
        if (CollectionUtils.isEmpty(practiceContents)) {
            logger.warn("Find empty practice homework, homeworkId:" + homeworkId);
            return null;
        }
        int practiceCount = practiceContents.size();
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        int finishPracticeCount = 0;
        if (newHomeworkResult != null && newHomeworkResult.getPractices() != null) {
            finishPracticeCount = newHomeworkResult.getPractices().values().stream().filter(nhr -> nhr.getFinishAt() != null).collect(Collectors.toList()).size();
        }

        if (finishPracticeCount >= practiceCount) {
            return null;
        }
        List<String> types = practiceContents.stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
        resultMap.put("homeworkId", homeworkId);
        resultMap.put("endDate", newHomework.getEndTime());
        resultMap.put("finishCount", finishPracticeCount);
        resultMap.put("homeworkCount", practiceCount);
        resultMap.put("homeworkType", MATH);
        resultMap.put("desc", "数学作业");
        resultMap.put("makeup", false);
        resultMap.put("types", types);
        resultMap.put("startComment", finishPracticeCount>0 ? "继续作业" : "开始作业");

        return resultMap;
    }
}
