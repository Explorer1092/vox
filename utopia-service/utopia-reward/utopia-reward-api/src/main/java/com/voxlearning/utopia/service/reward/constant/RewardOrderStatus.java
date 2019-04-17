package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardOrderStatus {
    SUBMIT("提交","兑换成功"),
    @Deprecated
    IN_AUDIT("审核","审核中"),
    PREPARE("配货","配货中"),
    @Deprecated
    SORTING("分拣","分拣中"),
    EXCEPTION("用户信息异常", "异常"),
    DELIVER("发货","已发货"),
    FAILED("失败","失败");
    //EXCHANGED("已兑换","已兑换"),// 针对虚拟类型商品，已兑换，未使用
    //USED("已使用","已使用");// 针对虚拟类型商品，已经使用

    @Getter
    private final String description;

    @Getter
    private final String desc;//老师app用的一个状态的描述.

    public static RewardOrderStatus parse(String name){
        RewardOrderStatus status;
        try{
            status = valueOf(name);
        }catch(Exception e){
            return RewardOrderStatus.EXCEPTION;
        }

        return status;
    }
}
