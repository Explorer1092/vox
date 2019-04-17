package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/9/12
 */
@Named
public class CR_CalculateBonusSilver extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Override
    public void execute(CastleResultContext context) {
        if (context.getStar() < 3) return;
        //预习的随机1-2个，城堡随机15-25个
        int bonus = context.getAfentiLearningType() == AfentiLearningType.preparation ? RandomUtils.nextInt(1, 2) : RandomUtils.nextInt(15, 25);
        context.setBonus(bonus);
    }
}
