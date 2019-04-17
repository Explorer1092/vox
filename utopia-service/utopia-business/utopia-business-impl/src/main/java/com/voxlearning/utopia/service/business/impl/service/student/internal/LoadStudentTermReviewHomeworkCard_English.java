package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.ENGLISH;

/**
 * Created by tanguohong on 2017/4/27.
 */
@Named
public class LoadStudentTermReviewHomeworkCard_English extends AbstractStudentIndexDataLoader{
    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        NewHomework.Location location = context.__englishTermReviewHomeworkLocations.stream()
                .filter(t -> context.__groupIds.contains(t.getClazzGroupId()))
                .filter(t -> !t.isChecked())
                .filter(t -> t.getEndTime() > context.timestamp)
                .filter(t -> t.getStartTime() <= context.timestamp)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);
        if (location == null) return context;
        context.__ignoreMakeUpEnglishTermReviewHomework = true;

        NewHomework mock = new NewHomework();
        mock.setId(location.getId());
        mock.setCreateAt(new Date(location.getCreateTime()));

        Set<Long> finishedStudentIds = Collections.emptySet();
        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
        if (Objects.nonNull(accomplishment)&& MapUtils.isNotEmpty(accomplishment.getDetails())) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }

        // 当存在未检查的未过期的未完成的已经开始的数学作业时才可能显示数学作业卡
        if (finishedStudentIds.contains(context.getStudent().getId())) return context;

        Map<String, Object> card = generateHomeworkCard(context, mock.getId());
        if (Objects.nonNull(card)) {
            card.put("prize", newHomeworkLoaderClient.loadNewHomeworkPrize(location) != null);
            context.__homeworkCards.add(card);
        }
        return context;
    }

    private Map<String, Object> generateHomeworkCard(StudentIndexDataContext context, String homeworkId) {
        Long studentId = context.getStudent().getId();
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        Map<String, Object> englishhwMap = new HashMap<>();
        englishhwMap.put("homeworkId", homeworkId);
        englishhwMap.put("homeworkType", ENGLISH);
        englishhwMap.put("endDate", DateUtils.dateToString(newHomework.getEndTime(), "MM-dd HH:mm"));
        englishhwMap.put("note", newHomework.getRemark());
        int practiceCount = newHomework.getPractices().size();
        englishhwMap.put("practiceCount", practiceCount);
        englishhwMap.put("expand", true);
        List<Map<String, Object>> practices = new LinkedList<>();
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
        int finishPracticeCount = 0;
        if(Objects.nonNull(newHomeworkResult)&& MapUtils.isNotEmpty(newHomeworkResult.getPractices())){
            finishPracticeCount = newHomeworkResult.getPractices().values().stream().filter(nhr-> nhr.getFinishAt() != null).collect(Collectors.toList()).size();
        }
        if (finishPracticeCount >= practiceCount) {
            return null;
        }
        englishhwMap.put("finishPracticeCount", finishPracticeCount);
        englishhwMap.put("practices", practices);
        return englishhwMap;
    }
}
