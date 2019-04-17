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

import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.impl.dao.RewardLogisticsPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class InternalRewardLogisticsService {

    @Inject private RewardLogisticsPersistence rewardLogisticsPersistence;

    public RewardLogistics $loadRewardLogistics(Long id) {
        return rewardLogisticsPersistence.load(id);
    }

    public RewardLogistics $findRewardLogistics(Long schoolId, String month, RewardLogistics.Type type) {
        return rewardLogisticsPersistence.loadRewardLogisticsBySchoolIdAndMonthAndType(schoolId, month, type);
    }

    public List<RewardLogistics> $findRewardLogistics(Long schoolId, RewardLogistics.Type type) {
        return rewardLogisticsPersistence.loadRewardLogisticsBySchoolIdAndType(schoolId, type);
    }

    public RewardLogistics $findRewardLogistics(Long receiverId, RewardLogistics.Type type, String month) {
        return rewardLogisticsPersistence.loadByReceiverIdAndTypeAndMonth(receiverId, type, month);
    }

    public List<RewardLogistics> $findRewardLogisticsList(String currentMonth, RewardLogistics.Type type) {
        return rewardLogisticsPersistence.loadRewardLogisticsByMonthAndType(currentMonth, type);
    }

    public List<RewardLogistics> $findRewardLogisticsList(Long reciverId, RewardLogistics.Type type) {
        return rewardLogisticsPersistence.loadRewardLogisticsByReceiverAndType(reciverId, type);
    }

    public List<RewardLogistics> $findRewardLogisticsList(String month) {
        return rewardLogisticsPersistence.loadByMonth(month);
    }

    public RewardLogistics $upsertRewardLogistics(RewardLogistics document) {
        if (document == null) {
            return null;
        }
        return rewardLogisticsPersistence.upsert(document);
    }
}
