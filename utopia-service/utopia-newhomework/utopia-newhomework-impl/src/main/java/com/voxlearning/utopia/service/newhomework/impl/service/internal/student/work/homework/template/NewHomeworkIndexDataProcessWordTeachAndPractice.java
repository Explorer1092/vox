package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/11/29 下午2:59
 */
@Named
public class NewHomeworkIndexDataProcessWordTeachAndPractice extends NewHomeworkIndexDataProcessTemplate {

    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.WORD_TEACH_AND_PRACTICE;
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
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions())) {
                questionCount += newHomeworkApp.getWordExerciseQuestions().size();
                totalQuestionCount += newHomeworkApp.getWordExerciseQuestions().size();
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions())) {
                for (ImageTextRhymeHomework imageTextRhymeHomework : newHomeworkApp.getImageTextRhymeQuestions()) {
                    if (CollectionUtils.isEmpty(imageTextRhymeHomework.getChapterQuestions())) {
                        continue;
                    }
                    questionCount += imageTextRhymeHomework.getChapterQuestions().size();
                    totalQuestionCount += imageTextRhymeHomework.getChapterQuestions().size();
                }
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds())) {
                questionCount += newHomeworkApp.getChineseCharacterCultureCourseIds().size();
                totalQuestionCount += newHomeworkApp.getChineseCharacterCultureCourseIds().size();
            }
        }
        int practiceCount = apps.size();
        int doQuestionCount = 0;
        int doCount = 0;
        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                    doQuestionCount += appAnswer.getAnswers().size();
                    doTotalQuestionCount += appAnswer.getAnswers().size();
                }
                if (MapUtils.isNotEmpty(appAnswer.getImageTextRhymeAnswers())) {
                    doQuestionCount += appAnswer.getImageTextRhymeAnswers().size();
                    doTotalQuestionCount += appAnswer.getImageTextRhymeAnswers().size();
                }
                if (MapUtils.isNotEmpty(appAnswer.getChineseCourses())) {
                    doQuestionCount += appAnswer.getChineseCourses().size();
                    doTotalQuestionCount += appAnswer.getChineseCourses().size();
                }
                if (appAnswer.isFinished()) {
                    doCount++;
                }
            }
        }

        Map<String, Object> practiceInfo = MiscUtils.m("objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "doCount", doCount,
                "doQuestionCount", doQuestionCount,
                "practiceCount", practiceCount,
                "questionCount", questionCount,
                "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
        context.getPracticeInfos().add(practiceInfo);

        if (newHomeworkResultAnswer == null || doQuestionCount < questionCount) {
            undoPracticesCount++;
        }

        context.setTotalQuestionCount(totalQuestionCount);
        context.setDoTotalQuestionCount(doTotalQuestionCount);
        context.setUndoPracticesCount(undoPracticesCount);
        return context;
    }
}
