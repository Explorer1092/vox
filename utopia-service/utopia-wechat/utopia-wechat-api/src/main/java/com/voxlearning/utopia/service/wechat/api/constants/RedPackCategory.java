package com.voxlearning.utopia.service.wechat.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaopeng.yang on 2015/6/5.
 * 红包业务类型
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RedPackCategory {
    AMBASSADOR_LOTTERY(1, "校园大使微信抽奖红包", "恭喜您，获得抽奖大红包"),
    AMBASSADOR_BACK_FLOW(2, "校园大使回流活动", "恭喜您，获得现金大放送活动红包"),
    AMBASSADOR_DOWNLINE_REWARD(3, "校园大使回流活动", "恭喜您，获得线下活动奖励红包"),
    TEACHER_TERM_BEGIN_LOTTERY(4, "老师开学大礼包抽奖", "恭喜您，获得抽奖大红包");

    private final int type;
    private final String description;
    private final String wishingText;


    public static Map<Integer, RedPackCategory> toMap() {
        Map<Integer, RedPackCategory> map = new HashMap<>();
        for (RedPackCategory category : values()) {
            map.put(category.type, category);
        }
        return map;
    }

    public static RedPackCategory get(Integer type) {
        return toMap().get(type);
    }
}
