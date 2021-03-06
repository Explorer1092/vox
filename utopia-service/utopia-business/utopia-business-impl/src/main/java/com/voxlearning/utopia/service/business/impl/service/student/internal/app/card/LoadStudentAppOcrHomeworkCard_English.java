package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.ENGLISH;

/**
 * @Description: 学生APP-英语纸质作业卡片
 * @author: Mr_VanGogh
 * @date: 2019/2/22 下午4:43
 */
@Named
public class LoadStudentAppOcrHomeworkCard_English extends AbstractStudentAppIndexDataLoader {

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        NewHomework.Location location = context.__englishOcrHomeworkLocations.stream()
                .filter(n -> n.getClazzGroupId() == 0 || context.__groupIds.contains(n.getClazzGroupId()))
                .filter(n -> !n.isChecked())
                .filter(n -> n.getEndTime() > context.timestamp)
                .filter(n -> n.getStartTime() != 0 && n.getStartTime() <= context.timestamp)
                .sorted(((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime())))
                .findFirst()
                .orElse(null);
        if (location == null) {
            return context;
        }

        context.__ignoreMakeUpOcrEnglishHomework = true;

        //完成学生
        Set<Long> finishedStudentIds = Collections.emptySet();
        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
        if (accomplishment != null && accomplishment.getDetails() != null) {
            finishedStudentIds = accomplishment.getDetails().keySet()
                    .stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }

        if (finishedStudentIds.contains(context.getStudent().getId())) {
            return context;
        }

        Map<String, Object> card = generateHomeworkCard(location.getId());
        if (MapUtils.isNotEmpty(card)) {
            context.__homeworkCards.add(card);
        }

        return context;
    }

    private Map<String, Object> generateHomeworkCard(String homeworkId) {
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (newHomework == null) {
            logger.warn("Find empty practice homework, homeworkId:" + homeworkId);
            return null;
        }

        Map<String, Object> resultMap = new HashMap<>();
        List<String> types = newHomework.getPractices()
                .stream()
                .map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name())
                .collect(Collectors.toList());
        resultMap.put("homeworkId", homeworkId);
        resultMap.put("endDate", newHomework.getEndTime());
        resultMap.put("homeworkType", ENGLISH);
        resultMap.put("desc", "英语纸质作业");
        resultMap.put("makeup", false);
        resultMap.put("subject", newHomework.getSubject().name());
        resultMap.put("types", types);
        resultMap.put("startComment", "开始提交");

        return resultMap;
    }
}
