package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.VacationHomeworkResultUpdateFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.VacationHomeworkResultUpdateTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 检查应用作业类型是否完成
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_CheckBasicAppPracticeFinish extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private VacationHomeworkResultUpdateFactory vacationHomeworkResultUpdateFactory;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.KEY_POINTS.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NEW_READ_RECITE.equals(context.getObjectiveConfigType())) {
            VacationHomeworkResultUpdateTemplate template = vacationHomeworkResultUpdateFactory.getTemplate(context.getObjectiveConfigType().getNewHomeworkContentProcessTemp());
            template.checkVacationHomeworkAppFinish(context);
        }
    }
}
