package com.voxlearning.utopia.service.campaign.impl.service.lottery;

import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 没有"天、大、德"情况时的奖池中奖率
 */
public class CaseA implements LotteryCase {

    @Override
    public List<Lottery> getLottery() {
        List<Lottery> list = new ArrayList<>();
        list.add(new Lottery(ActivityCardEnum.de, 0.02 * base));
        list.add(new Lottery(ActivityCardEnum.tian, 0.02 * base));
        list.add(new Lottery(ActivityCardEnum.da, 0.02 * base));
        list.add(new Lottery(ActivityCardEnum.guan, 0.188 * base));
        list.add(new Lottery(ActivityCardEnum.hai, 0.188 * base));
        list.add(new Lottery(ActivityCardEnum.shen, 0.188 * base));
        list.add(new Lottery(ActivityCardEnum.zhan, 0.188 * base));
        list.add(new Lottery(ActivityCardEnum.jian, 0.188 * base));
        return list;
    }
}
