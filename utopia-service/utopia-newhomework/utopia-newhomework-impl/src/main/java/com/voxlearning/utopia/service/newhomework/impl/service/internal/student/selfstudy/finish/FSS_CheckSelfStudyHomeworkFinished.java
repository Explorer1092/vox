package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
public class FSS_CheckSelfStudyHomeworkFinished extends SpringContainerSupport implements FinishSelfStudyHomeworkTask {
    @Override
    public void execute(FinishSelfStudyHomeworkContext context) {
        SelfStudyHomeworkResult homeworkResult = context.getSelfStudyHomeworkResult();
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        for (ObjectiveConfigType type : context.getSelfStudyHomework().findPracticeContents().keySet()) {
            if (Objects.equals(objectiveConfigType, type)) continue; // 当前练习类型肯定是已经完成了的
            LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> practices = homeworkResult.getPractices();
            if (MapUtils.isEmpty(practices) || practices.get(type) == null || !practices.get(type).isFinished()) {
                return; // 这个类型未完成
            }
        }
        context.setHomeworkFinished(true);
    }
}
