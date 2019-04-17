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
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.zone.api.ZoneLikeService;
import com.voxlearning.utopia.service.zone.impl.support.DailyLikeOrCommentCacheManager;
import com.voxlearning.utopia.service.zone.impl.support.DailyNewLikeOrCommentReceivedCacheManager;
import com.voxlearning.utopia.service.zone.impl.support.LikedCountCacheManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneLikeServiceImpl")
@ExposeService(interfaceClass = ZoneLikeService.class)
public class ZoneLikeServiceImpl implements ZoneLikeService {

    @Inject private DailyLikeOrCommentCacheManager dailyLikeOrCommentCacheManager;
    @Inject private DailyNewLikeOrCommentReceivedCacheManager dailyNewLikeOrCommentReceivedCacheManager;
    @Inject private LikedCountCacheManager likedCountCacheManager;

    @Override
    public AlpsFuture<Boolean> increaseLikedCount(Long userId) {
        likedCountCacheManager.increase(userId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public Map<Long, Long> loadLikedCounts(Collection<Long> userIds) {
        return likedCountCacheManager.loadLikedCounts(userIds);
    }

    @Override
    public AlpsFuture<Boolean> record(Long studentId) {
        dailyLikeOrCommentCacheManager.record(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> sent(Long studentId) {
        boolean ret = dailyLikeOrCommentCacheManager.sent(studentId);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> show(Long studentId) {
        boolean ret = dailyNewLikeOrCommentReceivedCacheManager.show(studentId);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> turnOn(Long studentId) {
        dailyNewLikeOrCommentReceivedCacheManager.turnOn(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> turnOff(Long studentId) {
        dailyNewLikeOrCommentReceivedCacheManager.turnOff(studentId);
        return new ValueWrapperFuture<>(true);
    }
}
