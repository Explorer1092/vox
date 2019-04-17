package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 14-7-15.
 * 此枚举的值 请不要随意更改 如更改，请知会相关人员，CRM中奖品管理用到了这个枚举值，
 * 改了的话，会受影响 奖品中的类型属性也存了这个值，
 * <p>
 * 最好还是别改了。
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardProductType {
    JPZX_SHIWU("实物"),
    JPZX_TIYAN("体验"),
    JPZX_PRESENT("爱心捐赠")
    ;

    @Getter
    private final String description;

    public static RewardProductType parse(String name) {
        RewardProductType type;
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
