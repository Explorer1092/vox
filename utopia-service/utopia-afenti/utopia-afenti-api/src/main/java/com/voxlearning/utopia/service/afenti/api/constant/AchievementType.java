package com.voxlearning.utopia.service.afenti.api.constant;

/**
 * 阿分题成就类型
 *
 * @author peng.zhang.a
 * @since 16-7-21
 */
public enum AchievementType {
    LOGIN(8, new int[]{3, 7, 14, 30, 60, 90, 120, 300}),      //登陆达人
    @Deprecated INVITATION(6, new int[]{1, 3, 5, 10, 15, 20}), //邀请达人
    STUDY_POINT(8, new int[]{5, 10, 20, 40, 80, 120, 200, 400}); //知识点学霸成就

    final public Integer levelNum;      //等级总数
    final public int[] cumulativeNum;   //每个等级需要的累计数量

    AchievementType(Integer levelNum, int[] cumulativeNum) {
        this.levelNum = levelNum;
        this.cumulativeNum = cumulativeNum;
    }

    public static AchievementType of(String type) {
        try {
            return AchievementType.valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前等级需要的数量
     * 超出等级默认为0
     */
    public int getCumulativeNum(int level) {
        return level >= 1 && level <= levelNum ? cumulativeNum[level - 1] : 0;
    }

    //是否最大级别
    public boolean isMaxLevel(int level) {
        return level == levelNum;
    }
}
