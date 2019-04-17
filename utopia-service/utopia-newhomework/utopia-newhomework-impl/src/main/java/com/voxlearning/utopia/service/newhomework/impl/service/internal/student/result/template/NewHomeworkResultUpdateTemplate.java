package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

/**
 * @author guohong.tan
 * @since 2016/11/23
 */
abstract public class NewHomeworkResultUpdateTemplate extends NewHomeworkSpringBean {

    abstract public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp();

    abstract public void processHomeworkContent(HomeworkResultContext context);

    abstract public void checkNewHomeworkAppFinish(HomeworkResultContext context);
}
