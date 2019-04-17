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

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Gift loader interface definitions.
 *
 * @author Xiaohai Zhang
 * @since Feb 26, 2015
 */
@ServiceVersion(version = "2016.09.02")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface GiftLoader extends IPingable {

    @Idempotent
    Map<Long, GiftHistory> loadGiftHistories(Collection<Long> ids);

    @Idempotent
    Collection<Long> getSenderGiftHistoryIds(Long senderId);

    @Idempotent
    Collection<Long> getReceiverGiftHistoryIds(Long receiverId);

    @Idempotent
    Page<GiftHistory> loadSenderGiftHistories(Long senderId, Pageable request);

    @Idempotent
    Page<GiftHistory> loadReceiverGiftHistories(Long receiverId, Pageable request);

    @Idempotent
    int loadNoThanksGiftCount(Long userId);
}
