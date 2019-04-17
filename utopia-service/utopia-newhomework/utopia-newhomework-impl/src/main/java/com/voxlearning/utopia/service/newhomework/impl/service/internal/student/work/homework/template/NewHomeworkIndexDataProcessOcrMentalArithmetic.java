package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Map;

@Named
public class NewHomeworkIndexDataProcessOcrMentalArithmetic extends NewHomeworkIndexDataProcessTemplate {
    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.OCR_MENTAL_ARITHMETIC;
    }

    @Override
    public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType) {
        String homeworkId = context.getHomeworkId();
        int totalQuestionCount = context.getTotalQuestionCount();
        int doTotalQuestionCount = context.getDoTotalQuestionCount();
        int undoPracticesCount = context.getUndoPracticesCount();

        NewHomeworkResultAnswer newHomeworkResultAnswer = context.getDoPractices().get(objectiveConfigType);

        totalQuestionCount++;
        if (newHomeworkResultAnswer == null || newHomeworkResultAnswer.getFinishAt() == null) {
            undoPracticesCount++;
        } else {
            doTotalQuestionCount++;
        }

        Map<String, Object> practiceInfo = MapUtils.m(
                "objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MiscUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.getFinishAt() != null
        );
        context.getPracticeInfos().add(practiceInfo);
        context.setTotalQuestionCount(totalQuestionCount);
        context.setDoTotalQuestionCount(doTotalQuestionCount);
        context.setUndoPracticesCount(undoPracticesCount);
        return context;
    }
}
