package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

/**
 * @author xuesong.zhang
 * @since 2017/7/10
 */
abstract public class LiveCastHomeworkResultUpdateTemplate extends NewHomeworkSpringBean {

    // 只会用到一小部分
    abstract public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp();

    abstract public void processLiveCastHomeworkContext(LiveCastHomeworkResultContext context);

    abstract public void checkLiveCastHomeworkAppFinish(LiveCastHomeworkResultContext context);
}
