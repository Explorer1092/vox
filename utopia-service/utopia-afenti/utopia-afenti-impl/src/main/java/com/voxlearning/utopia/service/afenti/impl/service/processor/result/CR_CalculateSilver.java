package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/22
 */
@Named
public class CR_CalculateSilver extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {


    private static final Map<Integer, Integer> STAR_INTEGRAL_MAP = new HashMap<>();

    static {
        STAR_INTEGRAL_MAP.put(0, 0);
        STAR_INTEGRAL_MAP.put(1, 10);
        STAR_INTEGRAL_MAP.put(2, 10);
        STAR_INTEGRAL_MAP.put(3, 10);
    }

    @Override
    public void execute(CastleResultContext context) {
        // 当前星星数量
        int currentStar = context.getStat() == null ? 0 : context.getStat().getStar();

        int amount = 0;
        for (int i = currentStar + 1; i <= context.getStar(); i++) {
            amount += STAR_INTEGRAL_MAP.get(i);
        }
        //预习的奖励缩小10倍
        if (context.getAfentiLearningType() == AfentiLearningType.preparation) amount = amount / 10;

        context.setSilver(amount);
    }
}
