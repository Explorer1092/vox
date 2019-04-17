package com.voxlearning.utopia.service.business.impl.utils;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

public class TeacherWeekTaskRewardCalc {

    private static final Map<Integer, CityReward> cityReward = new LinkedHashMap<>();
    private static final CityReward defaultReward = new CityReward(1.5, 5, 0.3, 10, 0.2, 20);

    static {
        cityReward.put(310100, new CityReward(0.2, 5, 0.3, 10, 1.5, 20));
        cityReward.put(410100, new CityReward(0.2, 5, 0.3, 10, 1.5, 20));
        cityReward.put(130200, new CityReward(0.2, 5, 0.3, 10, 1.5, 20));
        cityReward.put(130400, new CityReward(0.2, 5, 0.3, 10, 1.5, 20));

        cityReward.put(440300, new CityReward(0.4, 5, 0.6, 10, 0.8, 20));
        cityReward.put(610100, new CityReward(0.4, 5, 0.6, 10, 0.8, 20));
        cityReward.put(370700, new CityReward(0.4, 5, 0.6, 10, 0.8, 20));
        cityReward.put(410400, new CityReward(0.4, 5, 0.6, 10, 0.8, 20));

        cityReward.put(440100, new CityReward(0.5, 5, 0.6, 10, 0.7, 20));
        cityReward.put(120100, new CityReward(0.5, 5, 0.6, 10, 0.7, 20));
        cityReward.put(370300, new CityReward(0.5, 5, 0.6, 10, 0.7, 20));
        cityReward.put(411400, new CityReward(0.5, 5, 0.6, 10, 0.7, 20));
    }

    /**
     * 根据城市和第几次判断系数
     *
     * @param cityCode 城市 code
     * @param i        第几次布置作业 从 0 开始
     * @return
     */
    private static double getCoefficient(Integer cityCode, int i) {
        CityReward cityReward = TeacherWeekTaskRewardCalc.cityReward.getOrDefault(cityCode, defaultReward);

        if (i == 0) {
            return cityReward.getOneCoefficient();
        } else if (i == 1) {
            return cityReward.getTwoCoefficient();
        } else {
            return cityReward.getTwoCoefficient();
        }
    }

    public static Integer getIntegralNum(Integer cityCode, Integer finishNum, int i) {
        double coefficient = getCoefficient(cityCode, i);
        double v = finishNum * coefficient + random();
        return new Double(v).intValue();
    }

    public static Integer getExpNum(Integer cityCode, int i) {
        CityReward cityReward = TeacherWeekTaskRewardCalc.cityReward.getOrDefault(cityCode, defaultReward);
        if (i == 0) {
            return cityReward.getOneExp();
        } else if (i == 1) {
            return cityReward.getTwoExp();
        } else {
            return cityReward.getThreeExp();
        }
    }

    public static CityReward getCityReward(Integer cityCode) {
        return TeacherWeekTaskRewardCalc.cityReward.getOrDefault(cityCode, defaultReward);
    }

    public static boolean showIntegral(Integer cityId) {
        if (cityId == null) {
            return false;
        }
        return cityReward.containsKey(cityId);
    }

    /**
     * 80%的几率返回1, 20%的几率返回 1~10
     */
    private static int random() {
        int y = RandomUtils.nextInt(0, 1000000);

        if (y < 800000) {
            return 1;
        } else {
            return RandomUtils.nextInt(1, 11);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CityReward implements java.io.Serializable {
        private double oneCoefficient;
        private int oneExp;

        private double twoCoefficient;
        private int twoExp;

        private double threeCoefficient;
        private int threeExp;
    }

}
