package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_LastQuestion extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    @Override
    public void execute(ReviewResultContext context) {
        if (!Boolean.TRUE.equals(context.getFinished())) context.terminateTask();
    }
}
