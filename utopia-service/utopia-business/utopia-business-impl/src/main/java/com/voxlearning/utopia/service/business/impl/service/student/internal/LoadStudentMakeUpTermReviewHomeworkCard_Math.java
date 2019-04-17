package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.MATH;

/**
 * Created by tanguohong on 2017/4/28.
 */
@Named
public class LoadStudentMakeUpTermReviewHomeworkCard_Math extends AbstractStudentIndexDataLoader {
    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        if (context.__ignoreMakeUpMathTermReviewHomework) {
            return context;
        }

        NewHomework.Location location = context.__mathTermReviewHomeworkLocations.stream()
                .filter(t -> t.getClazzGroupId() == 0 || context.__groupIds.contains(t.getClazzGroupId()))
                .filter(t -> t.getStartTime() != 0 && t.getStartTime() <= context.timestamp)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);
        if (location == null) {
            return context;
        }

        Set<Long> finishedStudentIds = new HashSet<>();
        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
        if (accomplishment != null && accomplishment.getDetails() != null) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }

        if (!finishedStudentIds.contains(context.getStudent().getId())) {
            Map<String, Object> mathhwMap = new HashMap<>();
            mathhwMap.put("homeworkId", location.getId());
            mathhwMap.put("homeworkType", MATH);
            context.__makeUpHomeworkCards.add(mathhwMap);
        }

        return context;
    }
}
