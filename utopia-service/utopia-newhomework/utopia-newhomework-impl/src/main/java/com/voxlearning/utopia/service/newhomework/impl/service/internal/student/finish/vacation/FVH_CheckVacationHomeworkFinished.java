package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.Objects;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_CheckVacationHomeworkFinished extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Override
    public void execute(FinishVacationHomeworkContext context) {
        VacationHomeworkResult result = context.getResult();
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        for (ObjectiveConfigType type : context.getVacationHomework().findPracticeContents().keySet()) {
            if (Objects.equals(objectiveConfigType, type)) {
                continue; // 当前练习类型肯定是已经完成了的
            }
            NewHomeworkResultAnswer answer = result.getPractices().get(type);
            if (answer == null) {
                return; // 这个类型还没有答题记录
            }
            if (!answer.isFinished()) {
                return; // 未完成返回
            }
        }
        context.setHomeworkFinished(true);
    }
}
