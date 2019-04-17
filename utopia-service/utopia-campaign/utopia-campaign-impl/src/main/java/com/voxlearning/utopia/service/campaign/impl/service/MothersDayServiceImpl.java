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

package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.entity.activity.ActivityMothersDayCard;
import com.voxlearning.utopia.entity.activity.ActivityMothersDayData;
import com.voxlearning.utopia.service.campaign.api.MothersDayService;
import com.voxlearning.utopia.service.campaign.impl.dao.ActivityMothersDayCardPersistence;
import com.voxlearning.utopia.service.campaign.impl.dao.ActivityMothersDayDataPersistence;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.utopia.temp.MothersDayActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType.ParentMothersDayCardNotice;

@Named("com.voxlearning.utopia.service.campaign.impl.service.MothersDayServiceImpl")
@ExposeService(interfaceClass = MothersDayService.class)
public class MothersDayServiceImpl extends SpringContainerSupport implements MothersDayService {

    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;

    @Inject private ActivityMothersDayCardPersistence activityMothersDayCardPersistence;
    @Inject private ActivityMothersDayDataPersistence activityMothersDayDataPersistence;

    private UtopiaCache unflushable;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.unflushable = CacheSystem.CBS.getCache("unflushable");
    }

    @Override
    public AlpsFuture<MapMessage> getMothersDayCard(User student, Boolean dataIncluded) {
        if (student == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage());
        }
        ActivityMothersDayCard card = activityMothersDayCardPersistence.load(student.getId());
        if (card == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage());
        }
        if (Boolean.TRUE.equals(dataIncluded)) {
            ActivityMothersDayData data = activityMothersDayDataPersistence.load(student.getId());
            return new ValueWrapperFuture<>(MapMessage.successMessage().add("card", card).add("data", data));
        }
        return new ValueWrapperFuture<>(MapMessage.successMessage().add("card", card));
    }

    @Override
    public AlpsFuture<MapMessage> giveMothersDayCardAsGift(final User student, String image, String voice) {
        if (student == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage("请您先登录").add("error", "NEED_LOGIN"));
        }
        if (!MothersDayActivity.isInTeachersDayPeriod()) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage("活动已经结束").add("error", "OUT_DATE"));
        }

        // 查找学生是否已经制作过卡片，如果没有就制作并发送，如果有就发送
        ActivityMothersDayCard card;
        boolean canAdd = false;
        try {
            if (StringUtils.isBlank(voice)) {
                return new ValueWrapperFuture<>(MapMessage.errorMessage("请您你上传录音").add("error", "MAKE_CARD_FAILED"));
            }
            card = activityMothersDayCardPersistence.load(student.getId());
            if (card == null) {
                card = new ActivityMothersDayCard();
                card.setStudentId(student.getId());
                card.setImage(image);
                card.setVoice(voice);
                card.setSended(false);
                card.setShared(false);
                activityMothersDayCardPersistence.insert(card);
                canAdd = true;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new ValueWrapperFuture<>(MapMessage.errorMessage("制作贺卡失败").add("error", "MAKE_CARD_FAILED"));
        }

        // 给学生加学豆
        if (canAdd) {
            try {
                String key = "MAKE_MOTHERS_DAY_CARD:STUDENTID:" + student.getId();
                if (unflushable.load(key) == null) {
                    IntegralHistory integralHistory = new IntegralHistory(student.getId(), IntegralType.制作了母亲节贺卡, 5);
                    integralHistory.setComment("制作母亲节贺卡获得学豆");
                    if (userIntegralServiceClient.getUserIntegralService().changeIntegral(student, integralHistory).isSuccess()) {
                        unflushable.add(key, (int) (MothersDayActivity.getMothersDayEndDate().getTime() / 1000), "dummy");
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        // 如果该贺卡已经被发送，就不能再发了
        if (card.getSended()) {
            return new ValueWrapperFuture<>(MapMessage.successMessage("发送成功"));
        }

        // 查找学生是否有家绑定了微信，如果有发送贺卡
        try {
            boolean canSend = true;
            final Map<Long, List<UserWechatRef>> wechats;
            List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
            if (CollectionUtils.isEmpty(parents)) {
                canSend = false;
            }
            List<Long> parentIds = new ArrayList<>();
            for (StudentParent parent : parents) {
                parentIds.add(parent.getParentUser().getId());
            }
            wechats = wechatLoaderClient.loadUserWechatRefs(parentIds, WechatType.PARENT);
            if (MapUtils.isEmpty(wechats)) {
                canSend = false;
            }

            if (!canSend) {
                return new ValueWrapperFuture<>(MapMessage.errorMessage("请您的家长先绑定微信").add("error", "SEND_FAILED"));
            }

            AlpsThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    sendMothersDayCardWechat(wechats, student);
                }
            });
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new ValueWrapperFuture<>(MapMessage.errorMessage("发送贺卡失败").add("error", "SEND_FAILED"));
        }

        return new ValueWrapperFuture<>(MapMessage.successMessage("发送成功"));
    }

    @Override
    public AlpsFuture<MapMessage> shareMothersDayCard(Long studentId) {
        ActivityMothersDayCard card = activityMothersDayCardPersistence.load(studentId);
        if (card == null) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage());
        }
        if (card.getShared()) {
            return new ValueWrapperFuture<>(MapMessage.errorMessage());
        }
        if (activityMothersDayCardPersistence.updateShared(studentId)) {
            String key = "SHARE_MOTHERS_DAY_CARD:STUDENTID:" + studentId;
            if (unflushable.load(key) == null) {
                IntegralHistory integralHistory = new IntegralHistory(studentId, IntegralType.分享了母亲节贺卡, 5);
                integralHistory.setComment("分享母亲节贺卡获得学豆");
                if (userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                    unflushable.add(key, (int) (MothersDayActivity.getMothersDayEndDate().getTime() / 1000), "dummy");
                }
            }
            return new ValueWrapperFuture<>(MapMessage.successMessage());
        }
        return new ValueWrapperFuture<>(MapMessage.errorMessage());
    }

    @Override
    public AlpsFuture<Boolean> updateMothersDayCardSended(Long studentId) {
        if (studentId == null || studentId <= 0) {
            return new ValueWrapperFuture<>(false);
        }
        activityMothersDayCardPersistence.updateSended(studentId);
        return new ValueWrapperFuture<>(true);
    }

    private void sendMothersDayCardWechat(Map<Long, List<UserWechatRef>> wechats, User student) {
        Map<Long, List<String>> user_openId_map = new HashMap<>();
        for (Long userId : wechats.keySet()) {
            for (UserWechatRef ref : wechats.get(userId)) {
                if (user_openId_map.containsKey(userId)) {
                    user_openId_map.get(userId).add(ref.getOpenId());
                } else {
                    List<String> openIds = new ArrayList<>();
                    openIds.add(ref.getOpenId());
                    user_openId_map.put(userId, openIds);
                }
            }
        }
        Map<String, Object> extensionInfo = MiscUtils.m("studentId", student.getId(), "studentname", student.fetchRealname());
        wechatServiceClient.processWechatNotice(ParentMothersDayCardNotice, user_openId_map, extensionInfo);
        activityMothersDayCardPersistence.updateSended(student.getId());
    }
}
