package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;
import com.voxlearning.utopia.service.newhomework.consumer.AbilityExamServiceLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-7
 */
@Named
public class LoadAbilityExamCard extends AbstractStudentAppIndexDataLoader {

    @Inject private AbilityExamServiceLoader abilityExamServiceLoader;

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {

        // 灰度控制
        // boolean isInBlankList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(context.getStudent(), "AbilityExam", "BlackList");
        // if (!isInBlankList && context.__enterableNewExamCards.isEmpty()) {
        if (context.__enterableNewExamCards.isEmpty()) {
            Integer classLevel = context.getStudent().getClazzLevelAsInteger();
            // 只有小学的才显示这个卡片
            if (classLevel != null && classLevel <= 6 && classLevel >= 1) {
                AbilityExamBasic abilityExamBasic = abilityExamServiceLoader.getHydraRemoteReference().loadAbilityExamBasic(String.valueOf(context.getStudent().getId()));
                if (abilityExamBasic == null || !abilityExamBasic.fetchFinished()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("homeworkId", "");
                    resultMap.put("endDateStr", "非学校作业，不强制完成哦~");
                    resultMap.put("homeworkType", "EXPAND_BASIC_REVIEW_ENGLISH");
                    resultMap.put("desc", "一起小学能力测评");
                    resultMap.put("makeup", false);
                    resultMap.put("subject", HomeworkType.MATH);
                    resultMap.put("types", Collections.singletonList("BASIC_REVIEW"));
                    resultMap.put("params", JsonUtils.toJson(MapUtils.m()));
                    resultMap.put("url", "/view/mobile/student/activity/evaluation/index.vpage");
                    resultMap.put("startComment", "自愿参加");
                    resultMap.put("endDate", DateUtils.stringToDate("2019-02-25 08:00:00"));
                    context.__basicReviewHomeworkCards.add(resultMap);
                }
            }
        }
        return context;
    }
}
