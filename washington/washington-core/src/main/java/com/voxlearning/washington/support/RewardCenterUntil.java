package com.voxlearning.washington.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;

import java.util.List;
import java.util.Objects;

/**
 * @author songtao
 * @since 2018/3/9
 */
public class RewardCenterUntil {

    public static boolean showProductInventory(List<RewardCategory> categories, RewardProductDetail detail) {
        if (detail == null || CollectionUtils.isEmpty(categories)) {
            return false;
        }
        RewardCategory rewardCategory = categories.stream().filter(e -> RewardProductType.JPZX_SHIWU.name().equals(e.getProductType()) || "COUPON".equals(e.getCategoryCode())).findAny().orElse(null);
        return Objects.equals(detail.getProductType(), RewardProductType.JPZX_SHIWU.name()) || rewardCategory != null;
    }
}
