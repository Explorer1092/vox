package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticPrecision;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class NewHomeworkIndexDataProcessExam extends NewHomeworkIndexDataProcessTemplate {
    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.EXAM;
    }

    @Override
    public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType) {
        String homeworkId = context.getHomeworkId();
        int totalQuestionCount = context.getTotalQuestionCount();
        int doTotalQuestionCount = context.getDoTotalQuestionCount();
        int undoPracticesCount = context.getUndoPracticesCount();
        NewHomeworkPracticeContent newHomeworkPracticeContent = context.getPracticeMap().get(objectiveConfigType);
        NewHomeworkResultAnswer newHomeworkResultAnswer = context.getDoPractices().get(objectiveConfigType);
        NewHomeworkResult newHomeworkResult = context.getNewHomeworkResult();

        String doWrongHomeworkUrl = "";
        if (ObjectiveConfigType.EXAM.equals(objectiveConfigType) && newHomeworkResult != null && newHomeworkResult.isFinished()) {
            doWrongHomeworkUrl = UrlUtils.buildUrlQuery("/flash/loader/newhomework/docorrect.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId()));
        }
        int questionCount = newHomeworkPracticeContent.getQuestions().size();
        int doQuestionCount = 0;
        if (newHomeworkResultAnswer != null) {
            if (newHomeworkResultAnswer.getAnswers() == null) {
                logger.error("NewHomeworkIndexDataProcessExam#processHomeworkIndexData newHomeworkResultAnswer.answers is null, homeworkId:{}, studentId:{}, objectiveConfigType:{}",
                        context.getHomeworkId(), context.getStudentId(), objectiveConfigType);
            } else {
                doQuestionCount = newHomeworkResultAnswer.getAnswers().size();
            }
        }

        // 过程性奖励的规则
        List<Map<String, Object>> awards = new ArrayList<>();
        int rightNum = 0;
        for (MentalArithmeticPrecision mentalArithmeticPrecision : MentalArithmeticPrecision.values()) {
            int questionNum = (int) Math.ceil(questionCount * mentalArithmeticPrecision.getPrecision() / 100);
            awards.add(MapUtils.m(
                    "rightNum", questionNum - rightNum,
                    "award", mentalArithmeticPrecision.getAward()));
            rightNum = questionNum;
        }

        Map<String, Object> practiceInfo = MapUtils.m(
                "objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "timeLimit", newHomeworkPracticeContent.getTimeLimit() != null ? newHomeworkPracticeContent.getTimeLimit().getTime() : 0,
                "mentalAward", SafeConverter.toBoolean(newHomeworkPracticeContent.getMentalAward()),
                "awards", awards,
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "doCount", doQuestionCount,
                "questionCount", questionCount,
                "doWrongHomeworkUrl", doWrongHomeworkUrl,
                "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null);
        context.getPracticeInfos().add(practiceInfo);
        totalQuestionCount += questionCount;
        doTotalQuestionCount += doQuestionCount;
        if (newHomeworkResultAnswer == null || doQuestionCount < questionCount) {
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
