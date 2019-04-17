package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成作业巩固卡片
 */
@Named
public class LoadStudentAppCorrectionHomeworkCard extends AbstractStudentAppIndexDataLoader {

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        // 查出最近两个月所有已检查&&包含讲练测作业形式&&已推荐巩固的作业id
        //英语
        List<NewHomework.Location> englishHomeworkLocations = context.__englishNormalHomeworkLocations.stream()
                .filter(t -> context.__groupIds.contains(t.getClazzGroupId()))
                .filter(NewHomework.Location::isChecked)
                .filter(NewHomework.Location::isIncludeIntelligentTeaching)
                .filter(NewHomework.Location::isRemindCorrection)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());
        //数学
        List<NewHomework.Location> mathHomeworkLocations = context.__mathNormalHomeworkLocations.stream()
                .filter(t -> context.__groupIds.contains(t.getClazzGroupId()))
                .filter(NewHomework.Location::isChecked)
                .filter(NewHomework.Location::isIncludeIntelligentTeaching)
                .filter(NewHomework.Location::isRemindCorrection)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());

        List<NewHomework.Location> locations = new ArrayList<>(englishHomeworkLocations);
        locations.addAll(mathHomeworkLocations);
        List<String> homeworkIds = locations.stream()
                .map(NewHomework.Location::getId)
                .collect(Collectors.toList());

        Map<String, SelfStudyAccomplishment> selfStudyAccomplishmentMap = selfStudyHomeworkLoaderClient.loadSelfStudyAccomplishment(homeworkIds);

        if (MapUtils.isEmpty(selfStudyAccomplishmentMap)) {
            return context;
        }

        Map<String, Object> englishCard = generateCorrectionRemindHomeworkCard(englishHomeworkLocations, selfStudyAccomplishmentMap, context);
        if (englishCard != null) {
            context.__correctionHomeworkCards.add(englishCard);
        }

        Map<String, Object> mathCard = generateCorrectionRemindHomeworkCard(mathHomeworkLocations, selfStudyAccomplishmentMap, context);
        if (mathCard != null) {
            context.__correctionHomeworkCards.add(mathCard);
        }

        return context;
    }

    private Map<String, Object> generateCorrectionRemindHomeworkCard(List<NewHomework.Location> locations, Map<String, SelfStudyAccomplishment> selfStudyAccomplishmentMap, StudentAppIndexDataContext context) {
        Long studentId = context.getStudent().getId();
        String selfStudyHomeworkId = null;
        NewHomework.Location homeworkLocation = null;
        if (CollectionUtils.isNotEmpty(locations)) {
            for (NewHomework.Location location : locations) {
                SelfStudyAccomplishment selfStudyAccomplishment = selfStudyAccomplishmentMap.get(location.getId());
                if (selfStudyAccomplishment != null && MapUtils.isNotEmpty(selfStudyAccomplishment.getDetails())) {
                    SelfStudyAccomplishment.Detail detail = selfStudyAccomplishment.getDetails().get(studentId);
                    // 找到第一个未完成的订正任务
                    if (detail != null && detail.getSelfStudyHomeworkId() != null && detail.getFinishAt() == null) {
                        selfStudyHomeworkId = detail.getSelfStudyHomeworkId();
                        homeworkLocation = location;
                        break;
                    }
                }
            }
        }
        if (selfStudyHomeworkId != null) {
            SelfStudyHomework selfStudyHomework = selfStudyHomeworkLoaderClient.loadSelfStudyHomework(selfStudyHomeworkId);
            if (selfStudyHomework == null) {
                return null;
            }
            int practiceCount = selfStudyHomework.getPractices().size();
            Subject subject = homeworkLocation.getSubject();
            Map<String, Object> cardMap = new HashMap<>();
            String endDateStr = "请学习" + DateUtils.dateToString(new Date(homeworkLocation.getCreateTime()), "MM-dd") + "作业的课程";
            List<String> types = selfStudyHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            cardMap.put("homeworkId", selfStudyHomeworkId);
            cardMap.put("homeworkCount", practiceCount);
            cardMap.put("homeworkType", Subject.ENGLISH == subject ? HomeworkType.ENGLISH : HomeworkType.MATH);
            cardMap.put("desc", subject.getValue() + "作业巩固");
            cardMap.put("endDate", selfStudyHomework.getEndTime());
            cardMap.put("endDateStr", endDateStr);
            cardMap.put("makeup", false);
            cardMap.put("subject", subject.name());
            cardMap.put("types", types);
            cardMap.put("startComment", "开始作业");
            cardMap.put("params", JsonUtils.toJson(MapUtils.m("selfStudyUrl", "/student/selfstudy/homework/index.api?homeworkId=" + selfStudyHomeworkId)));
            cardMap.put("initParams", JsonUtils.toJson(MapUtils.m("selfStudyUrl", "/student/selfstudy/homework/index.api?homeworkId=" + selfStudyHomeworkId)));
            cardMap.put("url", "/resources/apps/hwh5/homework-apps/student-app-exam/v2.5.0/student-amend/index.vhtml");
            return cardMap;
        }
        return null;
    }
}
