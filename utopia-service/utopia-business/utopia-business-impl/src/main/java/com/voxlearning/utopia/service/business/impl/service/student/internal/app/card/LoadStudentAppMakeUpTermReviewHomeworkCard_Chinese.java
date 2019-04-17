package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.CHINESE;

/**
 * @author guoqiang.li
 * @since 2017/11/24
 */
@Named
public class LoadStudentAppMakeUpTermReviewHomeworkCard_Chinese extends AbstractStudentAppIndexDataLoader {
    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        if (context.__ignoreMakeUpChineseTermReviewHomework) {
            return context;
        }
        NewHomework.Location location = context.__chineseTermReviewHomeworkLocations.stream()
                .filter(t -> t.getClazzGroupId() == 0 || context.__groupIds.contains(t.getClazzGroupId()))
                .filter(t -> t.getStartTime() != 0 && t.getStartTime() <= context.timestamp)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);
        if (location == null) {
            return context;
        }

        Set<Long> finishedStudentIds = Collections.emptySet();
        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
        if (accomplishment != null && accomplishment.getDetails() != null) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }

        if (!finishedStudentIds.contains(context.getStudent().getId())) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(location.getId());
            List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("homeworkId", location.getId());
            resultMap.put("homeworkType", CHINESE);
            resultMap.put("endDate", location.getEndTime());
            resultMap.put("desc", "语文期末复习补做");
            resultMap.put("makeup", true);
            resultMap.put("types", types);
            resultMap.put("startComment", "开始作业");
            context.__makeUpHomeworkCards.add(resultMap);
        }

        return context;
    }
}
