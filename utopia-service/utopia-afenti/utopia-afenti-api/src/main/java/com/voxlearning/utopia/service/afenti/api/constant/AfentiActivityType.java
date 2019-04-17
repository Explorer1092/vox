package com.voxlearning.utopia.service.afenti.api.constant;

/**
 * @author Ruib
 * @since 2016/8/15
 */
public enum AfentiActivityType {
    @Deprecated signIn, // 签到活动 2016年　9月1号到9月30号
    @Deprecated memberGiftPack, // 会员大礼包
    @Deprecated quiz, // 期中期末测验
    integralAddition, // 积分奖励加成
    daNianShou  //打年兽活动
    ;

    public static AfentiActivityType safeParse(String name) {
        try {
            return AfentiActivityType.valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
