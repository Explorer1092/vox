package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;

import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCFH_CheckDuplicated extends SpringContainerSupport implements FinishLiveCastHomeworkTask {
    @Override
    public void execute(FinishLiveCastHomeworkContext context) {
    }
}
