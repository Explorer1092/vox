package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.alps.core.util.MapUtils;
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

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Named
public class NewHomeworkIndexDataProcessDubbing extends NewHomeworkIndexDataProcessTemplate {
    @Override
    public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp() {
        return NewHomeworkIndexDataProcessTemp.DUBBING;
    }

    @Override
    public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType) {
        String homeworkId = context.getHomeworkId();
        int totalQuestionCount = context.getTotalQuestionCount();
        int doTotalQuestionCount = context.getDoTotalQuestionCount();
        int undoPracticesCount = context.getUndoPracticesCount();
        List<NewHomeworkApp> apps = context.getPracticeMap().get(objectiveConfigType).getApps();
        NewHomeworkResultAnswer newHomeworkResultAnswer = context.getDoPractices().get(objectiveConfigType);

        int dubbingCount = apps.size();
        int doDubbingCount = 0;

        if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                if (appAnswer.isFinished()) {
                    doDubbingCount++;
                }
            }
        }

        Map<String, Object> practiceInfo = MapUtils.m(
                "objectiveConfigType", objectiveConfigType,
                "objectiveConfigTypeName", objectiveConfigType.getValue(),
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/do.vpage", MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "middleResultUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework/type/result.vpage", MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", objectiveConfigType, "sid", context.getStudentId())),
                "doCount", doDubbingCount,
                "practiceCount", dubbingCount,
                "finished", newHomeworkResultAnswer != null && newHomeworkResultAnswer.isFinished()
        );
        context.getPracticeInfos().add(practiceInfo);
        totalQuestionCount += dubbingCount;
        doTotalQuestionCount += doDubbingCount;
        if (newHomeworkResultAnswer == null || doDubbingCount < apps.size()) {
            undoPracticesCount++;
        }
        if (doDubbingCount == apps.size() && newHomeworkResultAnswer != null && !newHomeworkResultAnswer.isFinished()) {
            context.setNeedFinish(true);
        }
        context.setTotalQuestionCount(totalQuestionCount);
        context.setDoTotalQuestionCount(doTotalQuestionCount);
        context.setUndoPracticesCount(undoPracticesCount);
        return context;
    }
}
