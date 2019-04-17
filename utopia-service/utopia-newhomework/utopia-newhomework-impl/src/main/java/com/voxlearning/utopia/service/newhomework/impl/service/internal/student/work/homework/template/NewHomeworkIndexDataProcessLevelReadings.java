package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
public class NewHomeworkIndexDataProcessLevelReadings extends NewHomeworkIndexDataProcessTemplate {
    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.LEVEL_READINGS;
    }

    @Override
    public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType) {
        String homeworkId = context.getHomeworkId();
        int totalQuestionCount = context.getTotalQuestionCount();
        int doTotalQuestionCount = context.getDoTotalQuestionCount();
        int undoPracticesCount = context.getUndoPracticesCount();
        List<NewHomeworkApp> apps = context.getPracticeMap().get(objectiveConfigType).getApps();
        NewHomeworkResultAnswer newHomeworkResultAnswer = context.getDoPractices().get(objectiveConfigType);

        int questionCount = 0;
        for (NewHomeworkApp newHomeworkApp : apps) {
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getQuestions())) {
                questionCount += newHomeworkApp.getQuestions().size();
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getOralQuestions())) {
                questionCount += newHomeworkApp.getOralQuestions().size();
            }
        }
        int readingCount = apps.size();
        int doReadingCount = 0;

        if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                if (appAnswer.isFinished()) {
                    doReadingCount++;
                }
            }
        }

        Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "doCount", doReadingCount,
                "practiceCount", readingCount,
                "questionCount", questionCount,
                "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
        context.getPracticeInfos().add(practiceInfo);
        totalQuestionCount += readingCount;
        doTotalQuestionCount += doReadingCount;
        if (newHomeworkResultAnswer == null || doReadingCount < apps.size()) {
            undoPracticesCount++;
        }
        if (doReadingCount == apps.size() && newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null) {
            context.setNeedFinish(true);
        }
        context.setTotalQuestionCount(totalQuestionCount);
        context.setDoTotalQuestionCount(doTotalQuestionCount);
        context.setUndoPracticesCount(undoPracticesCount);
        return context;
    }
}
