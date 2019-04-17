package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCFH_CheckHomeworkFinished extends SpringContainerSupport implements FinishLiveCastHomeworkTask {

    @Override
    public void execute(FinishLiveCastHomeworkContext context) {
        LiveCastHomeworkResult result = context.getLiveCastHomeworkResult();
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        for (ObjectiveConfigType type : context.getLiveCastHomework().findPracticeContents().keySet()) {
            if (Objects.equals(objectiveConfigType, type)) continue; // 当前练习类型肯定是已经完成了的
            NewHomeworkResultAnswer answer = result.getPractices().get(type);
            if (answer == null)
                return; // 这个类型还没有答题记录
            if (!answer.isFinished())
                return; // 未完成返回
        }
        context.setHomeworkFinished(true);
    }
}
