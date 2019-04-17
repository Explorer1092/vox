package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_CalculateReward extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    private static final Map<Integer, Integer> STAR_INTEGRAL_MAP = new HashMap<>();

    static {
        STAR_INTEGRAL_MAP.put(0, 0);
        STAR_INTEGRAL_MAP.put(1, 10);
        STAR_INTEGRAL_MAP.put(2, 10);
        STAR_INTEGRAL_MAP.put(3, 10);
    }

    @Override
    public void execute(ReviewResultContext context) {
        // 当前星星数量
        int currentStar = context.getStat() == null ? 0 : context.getStat().getStar();

        int amount = 0;
        for (int i = currentStar + 1; i <= context.getStar(); i++) {
            amount += STAR_INTEGRAL_MAP.get(i);
        }
        context.setSilver(amount);

        if (context.getStar() < 3) return;

        //随机15-25个
        int bonus = RandomUtils.nextInt(15, 25);
        context.setBonus(bonus);
    }
}
