package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.VacationHomeworkResultUpdateFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.VacationHomeworkResultUpdateTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 更新作业中间结果表
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_UpdateVacationHomeworkResult extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private VacationHomeworkResultUpdateFactory vacationHomeworkResultUpdateFactory;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        VacationHomeworkResultUpdateTemplate template = vacationHomeworkResultUpdateFactory.getTemplate(context.getObjectiveConfigType().getNewHomeworkContentProcessTemp());
        if (template == null) {
            context.errorResponse("missing {}'s template", context.getObjectiveConfigType());
            return;
        }
        template.processVacationHomeworkContext(context);
    }
}
