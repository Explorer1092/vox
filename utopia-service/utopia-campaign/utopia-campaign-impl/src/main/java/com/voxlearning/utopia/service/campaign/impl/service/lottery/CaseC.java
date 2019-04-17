package com.voxlearning.utopia.service.campaign.impl.service.lottery;

import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 有"天、大、德"任意两个情况时的奖池中奖率
 */
public class CaseC implements LotteryCase {

    @Override
    public List<Lottery> getLottery() {
        List<Lottery> list = new ArrayList<>();
        list.add(new Lottery(ActivityCardEnum.de, 0.08 * base));
        list.add(new Lottery(ActivityCardEnum.tian, 0.08 * base));
        list.add(new Lottery(ActivityCardEnum.da, 0.08 * base));
        list.add(new Lottery(ActivityCardEnum.guan, 0.152 * base));
        list.add(new Lottery(ActivityCardEnum.hai, 0.152 * base));
        list.add(new Lottery(ActivityCardEnum.shen, 0.152 * base));
        list.add(new Lottery(ActivityCardEnum.zhan, 0.152 * base));
        list.add(new Lottery(ActivityCardEnum.jian, 0.152 * base));
        return list;
    }
}
