package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.math.BigDecimal;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class TQR_CalculateScore extends SpringContainerSupport implements IAfentiTask<TermQuizResultContext> {

    @Override
    public void execute(TermQuizResultContext context) {
        int score = new BigDecimal(context.getQrm().values().stream().filter(r -> r.getRightNum() >= 1).count())
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(context.getQrm().size()), 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        context.setScore(score);
    }
}
