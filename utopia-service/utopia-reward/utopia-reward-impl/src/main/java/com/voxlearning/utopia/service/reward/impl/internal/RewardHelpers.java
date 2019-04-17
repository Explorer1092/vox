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

package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.reward.base.support.*;
import com.voxlearning.utopia.service.reward.impl.dao.RewardCouponDetailPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardMoonLightBoxHistoryPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardOrderPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardWishOrderPersistence;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Reward helper instances.
 *
 * @author Xiaohai Zhang
 * @since Dec 2, 2014
 */
@Deprecated
@Named
public class RewardHelpers extends SpringContainerSupport {


    @Inject private RewardCouponDetailPersistence rewardCouponDetailPersistence;
    @Inject private RewardOrderPersistence rewardOrderPersistence;
    @Inject private RewardWishOrderPersistence rewardWishOrderPersistence;
    @Inject private RewardMoonLightBoxHistoryPersistence rewardMoonLightBoxHistoryPersistence;

    @Getter private RewardOrderLoader rewardOrderLoader;
    @Getter private RewardWishOrderLoader rewardWishOrderLoader;
    @Getter private RewardCouponDetailLoader rewardCouponDetailLoader;
    @Getter private RewardProductDetailGenerator rewardProductDetailGenerator;
    @Getter private RewardMoonLightBoxLoader rewardMoonLightBoxLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        this.rewardOrderLoader = new RewardOrderLoader(
                orderIds -> rewardOrderPersistence.loads(orderIds),
                userIds -> rewardOrderPersistence.loadByUserIds(userIds));

        this.rewardMoonLightBoxLoader = new RewardMoonLightBoxLoader(rewardMoonLightBoxHistoryPersistence::loadByUserIds);

        this.rewardWishOrderLoader = new RewardWishOrderLoader(
                id -> rewardWishOrderPersistence.loads(id),
                id -> rewardWishOrderPersistence.loadByUserIds(id));

        this.rewardCouponDetailLoader = new RewardCouponDetailLoader(
                productIds -> rewardCouponDetailPersistence.loadByProductIds(productIds),
                userIds -> rewardCouponDetailPersistence.loadByUserIds(userIds));

        this.rewardProductDetailGenerator = new RewardProductDetailGenerator(
                applicationContext.getBean(RewardLoaderImpl.class)
        );
    }
}
