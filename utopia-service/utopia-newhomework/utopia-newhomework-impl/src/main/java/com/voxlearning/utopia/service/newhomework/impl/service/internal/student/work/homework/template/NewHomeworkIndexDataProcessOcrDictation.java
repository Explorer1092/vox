package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/23
 * \* Time: 5:37 PM
 * \* Description:纸质听写
 * \
 */
@Named
public class NewHomeworkIndexDataProcessOcrDictation extends NewHomeworkIndexDataProcessTemplate {
    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.OCR_DICTATION;
    }

    @Override
    public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType) {
        String homeworkId = context.getHomeworkId();
        int totalQuestionCount = context.getTotalQuestionCount();
        int doTotalQuestionCount = context.getDoTotalQuestionCount();
        int undoPracticesCount = context.getUndoPracticesCount();
        NewHomeworkPracticeContent newHomeworkPracticeContent = context.getPracticeMap().get(objectiveConfigType);
        NewHomeworkResultAnswer newHomeworkResultAnswer = context.getDoPractices().get(objectiveConfigType);

        int questionCount = newHomeworkPracticeContent.getQuestions().size();
        int doQuestionCount = 0;
        if (newHomeworkResultAnswer != null) {
            if ( MapUtils.isEmpty(newHomeworkResultAnswer.getAnswers())) {
                doQuestionCount = 0;
            } else {
                doQuestionCount = newHomeworkResultAnswer.getAnswers().size();
            }
        }
        boolean isFinished = newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null;
        Map<String, Object> practiceInfo = MapUtils.m(
                "objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "timeLimit", newHomeworkPracticeContent.getTimeLimit() != null ? newHomeworkPracticeContent.getTimeLimit().getTime() : 0,
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType)),
                "doCount", isFinished ? questionCount : 0,
                "questionCount", questionCount,
                "finished", isFinished);
        context.getPracticeInfos().add(practiceInfo);
        totalQuestionCount += questionCount;
        doTotalQuestionCount += doQuestionCount;
        if (!isFinished) {
            undoPracticesCount++;
        }

        // 所有题都已做完，但缺少finishAt
        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() == null
                && questionCount == doQuestionCount) {
            context.setNeedFinish(true);
        }
        context.setTotalQuestionCount(totalQuestionCount);
        context.setDoTotalQuestionCount(doTotalQuestionCount);
        context.setUndoPracticesCount(undoPracticesCount);
        return context;
    }
}
