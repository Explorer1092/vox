package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.math.BigDecimal;

/**
 * @author Ruib
 * @since 2016/10/17
 */
@Named
public class QR_CaqlculateReward extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {

    @Override
    public void execute(QuizResultContext context) {
        int i = 20;
        if (StringUtils.equals(context.getUnitId(), UtopiaAfentiConstants.CURRENT_QUIZ)) i = 10;

        int integral = new BigDecimal(i).multiply(new BigDecimal(context.getScore()))
                .divide(new BigDecimal(100), 0, BigDecimal.ROUND_DOWN).intValue();
        context.setIntegral(integral);
    }
}
