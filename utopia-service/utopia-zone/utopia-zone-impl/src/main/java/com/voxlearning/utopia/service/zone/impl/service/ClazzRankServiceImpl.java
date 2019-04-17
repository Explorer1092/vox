/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankHistory;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankRewardHistory;
import com.voxlearning.utopia.service.zone.api.ClazzRankService;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzRankHistoryPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzRankRewardHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.zone.impl.service.ClazzRankServiceImpl")
@ExposeService(interfaceClass = ClazzRankService.class)
public class ClazzRankServiceImpl extends SpringContainerSupport implements ClazzRankService {

    @Inject private ClazzRankHistoryPersistence clazzRankHistoryPersistence;
    @Inject private ClazzRankRewardHistoryPersistence clazzRankRewardHistoryPersistence;

    @Override
    public AlpsFuture<List<ClazzRankHistory>> findClazzRankHistoriesByClazzId(Long clazzId) {
        if (clazzId == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<ClazzRankHistory> histories = clazzRankHistoryPersistence.findByClazzId(clazzId);
        return new ValueWrapperFuture<>(histories);
    }

    @Override
    public AlpsFuture<List<ClazzRankHistory>> findClazzRankHistoriesByMonth(String month) {
        if (month == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<ClazzRankHistory> histories = clazzRankHistoryPersistence.findByMonth(month);
        return new ValueWrapperFuture<>(histories);
    }

    @Override
    public AlpsFuture<ClazzRankRewardHistory> insertClazzRankRewardHistory(ClazzRankRewardHistory history) {
        if (history == null) {
            return ValueWrapperFuture.nullInst();
        }
        clazzRankRewardHistoryPersistence.insert(history);
        return new ValueWrapperFuture<>(history);
    }

    @Override
    public AlpsFuture<List<ClazzRankRewardHistory>> findClazzRankRewardHistoriesByUserId(Long userId) {
        if (userId == null) {
            return ValueWrapperFuture.emptyList();
        }
        List<ClazzRankRewardHistory> histories = clazzRankRewardHistoryPersistence.findByUserId(userId);
        return new ValueWrapperFuture<>(histories);
    }
}
