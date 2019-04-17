package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 14-7-30.
 */
@Named
@Slf4j
@NoArgsConstructor
public class CouponTemplateManager {
    private final Map<CouponProductionName, CouponTemplate> templates = new HashMap<>();

    public void register(CouponTemplate template) {
        templates.put(template.getProductionName(), template);
    }

    public CouponTemplate get(CouponProductionName couponProductionName) {
        return templates.get(couponProductionName);
    }
}
