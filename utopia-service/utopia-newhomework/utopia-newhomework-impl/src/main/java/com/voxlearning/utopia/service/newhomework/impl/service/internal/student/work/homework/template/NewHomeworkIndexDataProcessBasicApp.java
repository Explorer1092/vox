package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class NewHomeworkIndexDataProcessBasicApp extends NewHomeworkIndexDataProcessTemplate {
    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.BASIC_APP;
    }

    @Override
    public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType) {
        String homeworkId = context.getHomeworkId();
        int totalQuestionCount = context.getTotalQuestionCount();
        int doTotalQuestionCount = context.getDoTotalQuestionCount();
        int undoPracticesCount = context.getUndoPracticesCount();
        List<NewHomeworkApp> apps = context.getPracticeMap().get(objectiveConfigType).getApps();
        NewHomeworkResultAnswer newHomeworkResultAnswer = context.getDoPractices().get(objectiveConfigType);

        List<String> doHomeworkUrls = new ArrayList<>();
        List<String> finishedUrls = new ArrayList<>();
        List<String> unFinishedUrls = new ArrayList<>();
        List<Map<String, Object>> appStatus = new ArrayList<>();
        int questionCount = 0;
        apps = apps.stream().sorted((c1, c2) -> {
            Integer r1 = practiceLoaderClient.loadPractice(SafeConverter.toLong(c1.getPracticeId())).getCategoryRank();
            Integer r2 = practiceLoaderClient.loadPractice(SafeConverter.toLong(c2.getPracticeId())).getCategoryRank();
            return Integer.compare(r1, r2);
        }).collect(Collectors.toList());
        Map<String, NewHomeworkResultAppAnswer> answerMap = newHomeworkResultAnswer != null
                ? newHomeworkResultAnswer.getAppAnswers() : Collections.EMPTY_MAP;
        for (NewHomeworkApp newHomeworkApp : apps) {
            questionCount += newHomeworkApp.getQuestions().size();
            String key = StringUtils.join(Arrays.asList(newHomeworkApp.getCategoryId(), newHomeworkApp.getLessonId()), "-");
            if (answerMap.keySet().contains(key)) {
                finishedUrls.add(UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "lessonId", newHomeworkApp.getLessonId(), "categoryId", newHomeworkApp.getCategoryId(), "practiceId", newHomeworkApp.getPracticeId(), "sid", context.getStudentId())));
            } else {
                unFinishedUrls.add(UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "lessonId", newHomeworkApp.getLessonId(), "categoryId", newHomeworkApp.getCategoryId(), "practiceId", newHomeworkApp.getPracticeId(), "sid", context.getStudentId())));
            }
            PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(newHomeworkApp.getPracticeId()));
            String categoryName = practiceType != null ? practiceType.getCategoryName() : "";
            String appKey = newHomeworkApp.getCategoryId() + "-" + newHomeworkApp.getLessonId();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = answerMap.get(appKey);
            boolean finished = newHomeworkResultAppAnswer != null && newHomeworkResultAppAnswer.isFinished();
            appStatus.add(MapUtils.m("appName", categoryName, "questionCount", newHomeworkApp.getQuestions().size(), "finished", finished));
        }
        doHomeworkUrls.addAll(finishedUrls);
        doHomeworkUrls.addAll(unFinishedUrls);
        int appCount = apps.size();
        int doAppCount = newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null ? newHomeworkResultAnswer.getAppAnswers().size() : 0;
        Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "doHomeworkUrls", doHomeworkUrls,
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "doCount", doAppCount,
                "practiceCount", appCount,
                "questionCount", questionCount,
                "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null,
                "appStatus", appStatus);
        context.getPracticeInfos().add(practiceInfo);
        totalQuestionCount += appCount;
        doTotalQuestionCount += doAppCount;
        if (newHomeworkResultAnswer == null || doAppCount < apps.size()) {
            undoPracticesCount++;
        }
        if (doAppCount == apps.size() && newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null) {
            context.setNeedFinish(true);
        }
        context.setTotalQuestionCount(totalQuestionCount);
        context.setDoTotalQuestionCount(doTotalQuestionCount);
        context.setUndoPracticesCount(undoPracticesCount);
        return context;
    }
}
