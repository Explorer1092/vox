package com.voxlearning.utopia.service.business.impl.support.order.userOrder;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.service.business.impl.support.order.OrderFilterContext;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2016/12/9.
 */
@Getter
@Setter
public class UserOrderFilterContext extends OrderFilterContext {
    private UserOrder order;
    private PaymentCallbackContext callbackContext;
    @Deprecated
    private Integer rewardPeriod;
    private Map<String, Integer> extraDays;

    public void addRewardPeriod(int addedRewardPeriod) {
        if (rewardPeriod == null) {
            rewardPeriod = addedRewardPeriod;
        } else {
            rewardPeriod += addedRewardPeriod;
        }
    }

    public void addExtraDays(String key, Integer value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }

        if (extraDays == null) {
            extraDays = new HashMap<>();
        }

        Integer newValue = value;

        if (extraDays.containsKey(key)) {
            newValue = newValue + extraDays.get(key);
        }
        
        extraDays.put(key, newValue);
    }
}
