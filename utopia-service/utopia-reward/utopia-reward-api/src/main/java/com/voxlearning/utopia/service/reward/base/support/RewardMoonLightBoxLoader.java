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

package com.voxlearning.utopia.service.reward.base.support;

import com.voxlearning.alps.spi.common.DataProvider;
import com.voxlearning.utopia.service.reward.entity.RewardMoonLightBoxHistory;
import lombok.Getter;

import java.util.*;

/**
 * Created by Summer Yang on 2015/10/30.
 */
public class RewardMoonLightBoxLoader {

    @Getter private final DataProvider<Collection<Long>, Map<Long, List<RewardMoonLightBoxHistory>>> userIdDataProvider;

    public RewardMoonLightBoxLoader(DataProvider<Collection<Long>, Map<Long, List<RewardMoonLightBoxHistory>>> userIdDataProvider) {
        this.userIdDataProvider = Objects.requireNonNull(userIdDataProvider);
    }

    public Map<Long, List<RewardMoonLightBoxHistory>> loadUsersRewardMoonlightBoxHistorys(Collection<Long> userIds) {
        return userIdDataProvider.provide(userIds);
    }

    public List<RewardMoonLightBoxHistory> loadUserRewardMoonlightBoxHistorys(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<RewardMoonLightBoxHistory> result = loadUsersRewardMoonlightBoxHistorys(Collections.singleton(userId)).get(userId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

}
