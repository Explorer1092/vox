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

package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.business.impl.listener.BusinessEventHandler;
import com.voxlearning.utopia.service.business.impl.service.MiscServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TeacherTermBeginAddLotteryChance implements BusinessEventHandler {

    @Inject private BusinessCacheSystem businessCacheSystem;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public BusinessEventType getEventType() {
        return BusinessEventType.TEACHER_TERM_BEGIN_ADD_LOTTERY_CHANCE;
    }

    @Override
    public void handle(BusinessEvent event) {
        if (event == null) return;
        if (event.getTimestamp() == 0) event.setTimestamp(System.currentTimeMillis());
        long timestamp = event.getTimestamp();
        if (event.getAttributes() == null) return;

        long teacherId = SafeConverter.toLong(event.getAttributes().get("teacherId"));
        if (teacherId == 0) return;
        int freeChance = SafeConverter.toInt(event.getAttributes().get("freeChance"));
        if (freeChance == 0) return;

        // 主副账号处理
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId != null) {
            teacherId = mainTeacherId;
        }

        String key = CacheKeyGenerator.generateCacheKey("TeacherTermBeginAddLotteryChance",
                new String[]{"teacherId", "day"},
                new Object[]{teacherId, DayRange.newInstance(timestamp).toString()});

        int expiration = DateUtils.getCurrentToDayEndSecond() + 86400;
        Cache cache = businessCacheSystem.CBS.persistence;
        long resulting = SafeConverter.toLong(cache.incr(key, 1, 1, expiration), -1);
        if (resulting < 0) {
            return;
        }
        if (resulting == 1) {
            campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN, teacherId, freeChance);
        }
    }
}
