package com.voxlearning.utopia.service.nekketsu.adventure.constant;

import lombok.Getter;

/**
 * 成就类型
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/15 14:53
 */
public enum AchievementType {

    LOGIN(1, 10, 0),  //登录成就，10学豆，5、10、20 (之后间隔10)
    DIAMOND(2, 5, 0),//钻石成就，5学豆，10、30、50（之后间隔50）
    STAGE(3, 20, 1),  //关卡成就，20学豆+1PK，5、10、20（之后间隔10）
    SHARED(4, 10, 0); //分享成就，10学豆，5、15、25（之后间隔10）

    @Getter private Integer order;

    @Getter private Integer beans;    //成就的学豆奖励

    @Getter private Integer pkVitality;   //成就的PK活力奖励

    private AchievementType(Integer order, Integer beans, Integer pkVitality) {
        this.order = order;
        this.beans = beans;
        this.pkVitality = pkVitality;
    }

}
