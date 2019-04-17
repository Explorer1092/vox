package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.math.BigDecimal;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class QR_CalculateScore extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {

    @Override
    public void execute(QuizResultContext context) {
        int score = new BigDecimal(context.getQrs().stream().filter(r -> r.getRightNum() >= 1).count())
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(context.getQrs().size()), 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        context.setScore(score);
    }
}
