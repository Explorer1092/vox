package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
abstract public class VacationHomeworkResultUpdateTemplate extends NewHomeworkSpringBean {
    abstract public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp();

    abstract public void processVacationHomeworkContext(VacationHomeworkResultContext context);

    abstract public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context);
}
