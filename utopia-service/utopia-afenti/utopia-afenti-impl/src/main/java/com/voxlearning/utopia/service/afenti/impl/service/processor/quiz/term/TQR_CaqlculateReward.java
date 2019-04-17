package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class TQR_CaqlculateReward extends SpringContainerSupport implements IAfentiTask<TermQuizResultContext> {

    @Override
    public void execute(TermQuizResultContext context) {
        context.setIntegral(1);
    }
}
