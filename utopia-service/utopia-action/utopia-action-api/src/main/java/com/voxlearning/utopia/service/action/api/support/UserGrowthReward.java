package com.voxlearning.utopia.service.action.api.support;

import com.voxlearning.utopia.service.action.api.document.Privilege;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 10/8/2016
 * 成长等级升级礼包
 */
public class UserGrowthReward {
    private static final Map<Integer, RewardBag> rewardBagMap;
    public static final Integer MAX_LEVEL = 30;

    static {

        rewardBagMap = new HashMap<>();
        rewardBagMap.put(5,  new RewardBag(0, Privilege.SpecialPrivileges.阿呜.getCode()));
        rewardBagMap.put(10, new RewardBag(0, Privilege.SpecialPrivileges.阿飘.getCode()));
        rewardBagMap.put(15, new RewardBag(0, Privilege.SpecialPrivileges.阿目.getCode()));
        rewardBagMap.put(20, new RewardBag(0, Privilege.SpecialPrivileges.阿章.getCode()));
        rewardBagMap.put(25, new RewardBag(0, Privilege.SpecialPrivileges.毛斯.getCode()));
        rewardBagMap.put(30, new RewardBag(0, Privilege.SpecialPrivileges.阿龙.getCode()));
    }

    //查询等级对应奖励的学豆数量
    public static Integer getIntegral(int growthLevel) {
        if (!canReceive(growthLevel)) {
            return 0;
        }

        if (rewardBagMap.containsKey(growthLevel)) {
            return rewardBagMap.get(growthLevel).getIntegral();
        }

        return 0;
    }

    //查询等级对应奖励的头饰ID
    public static String getHeadWearCode(int growthLevel) {
        if (!canReceive(growthLevel)) {
            return null;
        }

        if (rewardBagMap.containsKey(growthLevel)) {
            return rewardBagMap.get(growthLevel).getHeadWearCode();
        }

        return null;
    }

    //判断等级是否可以领取奖励
    public static boolean canReceive(int growthLevel) {
        return 0 < growthLevel && growthLevel % 5 == 0;
    }

    //计算可以领取奖励的等级(包含用户已领取、未领取)
    public static List<Integer> getLevelsCanReceive(int growthLevel) {
        List<Integer> levels = new ArrayList<>();
        for (int i = 1; i <= growthLevel; i++) {
            if (canReceive(i)) {
                levels.add(i);
            }
        }

        return levels;
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class RewardBag {
        private final Integer integral; //学豆数量
        private final String headWearCode;//头饰特权ID
    }
}
