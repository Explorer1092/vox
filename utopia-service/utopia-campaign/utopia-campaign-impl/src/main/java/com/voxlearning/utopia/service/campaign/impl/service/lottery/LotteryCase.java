package com.voxlearning.utopia.service.campaign.impl.service.lottery;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;

import java.util.List;

public interface LotteryCase {
    int base = 1000000;

    List<Lottery> getLottery();

    default ActivityCardEnum draw() {
        double start = 0d;
        List<Lottery> list = getLottery();
        for (Lottery lottery : list) {
            start += lottery.getRate();
            lottery.setTarget(start);
        }

        int target = RandomUtils.nextInt(1, base);
        for (Lottery lottery : list) {
            if (target <= lottery.getTarget()) {
                return lottery.getCard();
            }
        }
        return null;
    }
}
