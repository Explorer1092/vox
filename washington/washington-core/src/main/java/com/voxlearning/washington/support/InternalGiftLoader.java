package com.voxlearning.washington.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import com.voxlearning.utopia.service.zone.api.mapper.GiftMapper;
import com.voxlearning.utopia.service.zone.client.GiftLoaderClient;
import com.voxlearning.utopia.service.zone.client.ZoneConfigServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class InternalGiftLoader extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;
    @Inject private GiftLoaderClient giftLoaderClient;
    @Inject private ZoneConfigServiceClient zoneConfigServiceClient;

    public Page<GiftMapper> loadSentGifts(Long senderId, Pageable pageable) {
        if (null == senderId || null == pageable) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<GiftMapper> result = new LinkedList<>();
        Page<GiftHistory> giftHistoryPage = giftLoaderClient.getGiftLoader().loadSenderGiftHistories(senderId, pageable);

        Collection<Long> giftIds = new LinkedHashSet<>();
        for (GiftHistory history : giftHistoryPage) {
            CollectionUtils.addNonNullElement(giftIds, history.getGiftId());
        }
        Map<Long, Gift> gifts = zoneConfigServiceClient.getGiftsFromBuffer(giftIds);

        for (GiftHistory history : giftHistoryPage.getContent()) {
            Gift gift = gifts.get(history.getGiftId());
            GiftMapper mapper = new GiftMapper();
            mapper.setDate(DateUtils.dateToString(history.getCreateDatetime(), "yyyy年MM月dd日"));
            mapper.setGiftImgUrl(gift == null ? "" : gift.getImgUrl());
            User receiver = raikouSystem.loadUser(history.getReceiverId());
            mapper.setReceiverName(receiver == null ? "" : receiver.fetchRealname());
            result.add(mapper);
        }
        return new PageImpl<>(result, pageable, giftHistoryPage.getTotalElements());
    }

    public Page<GiftMapper> loadReceivedGifts(Long receiverId, Pageable pageable) {
        if (null == receiverId || null == pageable) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<GiftMapper> result = new LinkedList<>();
        Page<GiftHistory> giftHistoryPage = giftLoaderClient.getGiftLoader().loadReceiverGiftHistories(receiverId, pageable);

        Collection<Long> giftIds = new LinkedHashSet<>();
        for (GiftHistory history : giftHistoryPage) {
            CollectionUtils.addNonNullElement(giftIds, history.getGiftId());
        }
        Map<Long, Gift> gifts = zoneConfigServiceClient.getGiftsFromBuffer(giftIds);

        Collection<Long> senderIds = new LinkedHashSet<>();
        for (GiftHistory history : giftHistoryPage.getContent()) {
            CollectionUtils.addNonNullElement(senderIds, history.getSenderId());
        }
        Map<Long, User> senders = raikouSystem.getUserLoader().loadUsersIncludeDisabled(senderIds).asMap();

        for (GiftHistory history : giftHistoryPage.getContent()) {
            User sender = senders.get(history.getSenderId());
            if (sender == null) {
                continue;
            }
            Gift gift = gifts.get(history.getGiftId());
            GiftMapper mapper = new GiftMapper();
            mapper.setGiftHistoryId(history.getId());
            mapper.setDate(DateUtils.dateToString(history.getCreateDatetime(), "yyyy年MM月dd日"));
            mapper.setPostscript(history.getPostscript());
            mapper.setGiftImgUrl(gift == null ? "" : gift.getImgUrl());
            mapper.setSenderName(sender.fetchRealname());
            mapper.setSenderId(sender.getId());
            mapper.setLatestReply(history.getLatestReply());
            mapper.setIsThanks(history.getIsThanks());
            result.add(mapper);
        }
        return new PageImpl<>(result, pageable, giftHistoryPage.getTotalElements());
    }

    public List<Map<String, Object>> loadLatestThreeGifts(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> ids = new LinkedList<>();
        ids.addAll(giftLoaderClient.getGiftLoader().getReceiverGiftHistoryIds(userId));

        List<Long> candidates = ids.size() < 3 ? ids : ids.subList(0, 3);
        candidates = new LinkedList<>(candidates);

        Map<Long, GiftHistory> histories = giftLoaderClient.getGiftLoader().loadGiftHistories(candidates);
        Collection<Long> giftIds = new LinkedHashSet<>();
        Collection<Long> senderIds = new LinkedHashSet<>();
        for (GiftHistory history : histories.values()) {
            giftIds.add(history.getGiftId());
            senderIds.add(history.getSenderId());
        }
        Map<Long, Gift> gifts = zoneConfigServiceClient.getGiftsFromBuffer(giftIds);
        Map<Long, User> userMap = raikouSystem.getUserLoader().loadUsersIncludeDisabled(senderIds).asMap();
        List<Map<String, Object>> result = new LinkedList<>();
        for (GiftHistory history : histories.values()) {
            Gift gift = gifts.get(history.getGiftId());
            User sender = userMap.get(history.getSenderId());
            if (gift == null || sender == null) {
                continue;
            }
            GiftMapper mapper = new GiftMapper();
            mapper.setGiftImgUrl(gift.getImgUrl());
            mapper.setPostscript(history.getPostscript());
            mapper.setSenderName(sender.fetchRealname());
            mapper.setSenderId(sender.getId());
            mapper.setIsThanks(history.getIsThanks());
            mapper.setGiftHistoryId(history.getId());
            result.add(mapper.toMap());
        }
        return result;
    }
}
