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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.zone.api.ZoneConfigService;
import com.voxlearning.utopia.service.zone.api.constant.GiftCategory;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import lombok.Getter;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class ZoneConfigServiceClient {

    @Getter
    @ImportService(interfaceClass = ZoneConfigService.class)
    private ZoneConfigService remoteReference;

    @Inject private UserMoodServiceClient userMoodServiceClient;
    @Inject private ZoneGiftServiceClient zoneGiftServiceClient;
    @Inject private ZoneProductServiceClient zoneProductServiceClient;

    public ClazzZoneProduct getClazzZoneProductFromBuffer(Long productId) {
        return zoneProductServiceClient.getClazzZoneProductBuffer().load(productId);
    }

    public Map<Long, Gift> getGiftsFromBuffer(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<Long, Gift> all = zoneGiftServiceClient.getGiftBuffer().loadAll();
        Map<Long, Gift> result = new LinkedHashMap<>();
        for (Long id : ids) {
            Gift gift = all.get(id);
            if (gift != null && !gift.isDisabledTrue()) {
                result.put(gift.getId(), gift);
            }
        }
        return result;
    }

    public Page<Gift> getStudentAvailableGiftsFromBuffer(GiftCategory category,
                                                         Pageable request,
                                                         Boolean needPay) {
        if (category == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        if (request == null || request.getPageNumber() < 0 || request.getPageSize() <= 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        Collection<Gift> allList = zoneGiftServiceClient.getGiftBuffer().loadAll().values();
        List<Gift> list = allList.stream()
                .filter(g -> !g.isDisabledTrue())
                .filter(Gift::isAvailableForStudent)
                .filter(g -> g.getGiftCategory() == category).collect(Collectors.toList());
        if (needPay) {
            list = list.stream().filter(Gift::isNeedPay).collect(Collectors.toList());
        }
        list = list.stream().sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchCreateTimestamp())).collect(Collectors.toList());
        return toGiftPage(list, request);
    }

    public Page<Gift> getTeacherAvailableGiftsFromBuffer(GiftCategory category, Pageable request) {
        if (category == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        if (request == null || request.getPageNumber() < 0 || request.getPageSize() <= 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        List<Gift> list = zoneGiftServiceClient.getGiftBuffer().loadAll().values().stream()
                .filter(Objects::nonNull)
                .filter(e -> !e.isDisabledTrue())
                .filter(Gift::isAvailableForTeacher)
                .filter(e -> e.getGiftCategory() == category)
                .sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchCreateTimestamp()))
                .collect(Collectors.toList());
        return toGiftPage(list, request);
    }

    public List<UserMood> getUserMoodListFromBuffer() {
        return userMoodServiceClient.getUserMoodBuffer().dump().getUserMoodList();
    }

    public UserMood getUserMoodFromBuffer(Long id) {
        return userMoodServiceClient.getUserMoodBuffer().load(id);
    }

    private Page<Gift> toGiftPage(List<Gift> gifts, Pageable request) {
        int page = request.getPageNumber();
        int size = request.getPageSize();
        int skip = page * size;
        long total = gifts.size();
        List<Gift> candidates = new LinkedList<>();
        for (int index = skip; index < gifts.size() && index < skip + size; index++) {
            candidates.add(gifts.get(index));
        }
        return new PageImpl<>(candidates, request, total);
    }
}
