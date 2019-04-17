package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.ENGLISH;

/**
 * Created by tanguohong on 2017/4/28.
 */
@Named
public class LoadStudentMakeUpTermReviewHomeworkCard_English extends AbstractStudentIndexDataLoader {
    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        if (context.__ignoreMakeUpEnglishTermReviewHomework) {
            return context;
        }
        NewHomework.Location location = context.__englishTermReviewHomeworkLocations.stream()
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
        if (Objects.nonNull(accomplishment) && MapUtils.isNotEmpty(accomplishment.getDetails())) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }
        if (!finishedStudentIds.contains(context.getStudent().getId())) {
            Map<String, Object> englishhwMap = new HashMap<>();
            englishhwMap.put("homeworkId", location.getId());
            englishhwMap.put("homeworkType", ENGLISH);
            context.__makeUpHomeworkCards.add(englishhwMap);
        }
        return context;
    }
}
