package com.voxlearning.utopia.service.reward.constant;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author songtao
 * @since 2018/4/28
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RewardCouponResource {
    ZUOYE("自有"),
    DUIBA("兑吧");
    @Getter
    private final String description;

    public static RewardCouponResource parse(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
