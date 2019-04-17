package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VacationHomeworkResultUpdate_Exam extends VacationHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.EXAM;
    }

    @Override
    public void processVacationHomeworkContext(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        Map<String, VacationHomeworkProcessResult> processResultMap = context.getProcessResult();
        for (VacationHomeworkProcessResult vhpr : processResultMap.values()) {
            vacationHomeworkResultDao.doHomework(
                    location,
                    vhpr.getObjectiveConfigType(),
                    vhpr.getQuestionId(),
                    vhpr.getId());
        }
    }

    @Override
    public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context) {

    }
}
