/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.api.newversion;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.entity.RewardWishOrder;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.product.crm.UpSertTagMapper;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181218")
@ServiceTimeout(timeout = 1, unit = TimeUnit.MINUTES)
public interface NewRewardService extends IPingable {
    MapMessage addRewardProduct(
            RewardProduct rewardProduct,
            List<ProductCategoryRef> productCategoryRefList,
            List<ProductSetRef> productSetRefList,
            List<ProductTagRef> productTagRefList,
            List<Map<String, Object>> skus);

    ProductCategory upsertCategory(ProductCategory productCategory);

    ProductSet upsertSet(ProductSet upsertSet);

    Boolean deleteCategoryById(Long id);

    Boolean deleteSetById(Long id);

    MapMessage upsertTag(UpSertTagMapper upSertTagMapper);

    Boolean deleteTagById(Long id);

    Integer deleteProductTagRefByTagId(Long tagId);

    MapMessage createRewardOrder(final User user,
                                 final RewardProductDetail productDetail,
                                 final RewardSku sku,
                                 final int quantity,
                                 final RewardWishOrder wishOrder,
                                 RewardOrder.Source source);

    MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source, TeacherCouponEntity coupon);

    MapMessage sendTeachingResourceMsg(User user, Long productId);

}
