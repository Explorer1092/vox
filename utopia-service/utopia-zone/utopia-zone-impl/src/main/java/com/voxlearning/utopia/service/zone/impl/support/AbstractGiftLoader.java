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

package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.zone.api.GiftLoader;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

abstract public class AbstractGiftLoader extends SpringContainerSupport implements GiftLoader {

    @Override
    public Page<GiftHistory> loadSenderGiftHistories(Long senderId, Pageable request) {
        if (senderId == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        if (request == null || request.getPageNumber() < 0 || request.getPageSize() <= 0) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<Long> ids = new LinkedList<>();
        ids.addAll(getSenderGiftHistoryIds(senderId));

        return toGiftHistoryPage(ids, request);
    }

    @Override
    public Page<GiftHistory> loadReceiverGiftHistories(Long receiverId, Pageable request) {
        if (receiverId == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        if (request == null || request.getPageNumber() < 0 || request.getPageSize() <= 0) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<Long> ids = new LinkedList<>();
        ids.addAll(getReceiverGiftHistoryIds(receiverId));

        return toGiftHistoryPage(ids, request);
    }

    @Override
    public int loadNoThanksGiftCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<Long> ids = new LinkedList<>();
        ids.addAll(getReceiverGiftHistoryIds(userId));

        Map<Long, GiftHistory> histories = loadGiftHistories(ids);
        if (MapUtils.isEmpty(histories)) {
            return 0;
        }
        return (int) histories.values().stream().filter(g -> !SafeConverter.toBoolean(g.getIsThanks())).count();
    }

    private Page<GiftHistory> toGiftHistoryPage(List<Long> ids, Pageable request) {
        int page = request.getPageNumber();
        int size = request.getPageSize();
        int skip = page * size;
        long total = ids.size();

        List<Long> candidates = new LinkedList<>();
        for (int index = skip; index < ids.size() && index < skip + size; index++) {
            candidates.add(ids.get(index));
        }

        if (candidates.isEmpty()) {
            return new PageImpl<>(Collections.<GiftHistory>emptyList(), request, total);
        }

        List<GiftHistory> content = new LinkedList<>();
        content.addAll(loadGiftHistories(candidates).values());

        return new PageImpl<>(content, request, total);
    }
}
