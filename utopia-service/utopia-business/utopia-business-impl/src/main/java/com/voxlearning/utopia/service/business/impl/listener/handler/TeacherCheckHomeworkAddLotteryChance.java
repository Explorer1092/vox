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

/**
 * @author zhangbin
 * @since 2017/2/28 21:41
 */
@Named
public class TeacherCheckHomeworkAddLotteryChance implements BusinessEventHandler {
    @Inject private BusinessCacheSystem businessCacheSystem;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public BusinessEventType getEventType() {
        return BusinessEventType.TEACHER_CHECK_HOMEWORK_ADD_LOTTERY_CHANCE;
    }

    @Override
    public void handle(BusinessEvent event) {
        if (event == null || event.getAttributes() == null) {
            return;
        }
        long teacherId = SafeConverter.toLong(event.getAttributes().get("teacherId"));
        if (teacherId == 0) {
            return;
        }
        if (event.getTimestamp() == 0) {
            event.setTimestamp(System.currentTimeMillis());
        }
        long timestamp = event.getTimestamp();

        // 主副账号处理
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId != null) {
            teacherId = mainTeacherId;
        }

        // 按照原来代码的逻辑，每天就一次机会
        String key = CacheKeyGenerator.generateCacheKey("TeacherCheckHomeworkAddLotteryChance",
                new String[]{"teacherId", "day"},
                new Object[]{teacherId, DayRange.newInstance(timestamp).toString()});

        int expiration = DateUtils.getCurrentToDayEndSecond() + 86400 * 7;
        Cache cache = businessCacheSystem.CBS.persistence;
        long resulting = SafeConverter.toLong(cache.incr(key, 1, 1, expiration), -1);
        if (resulting < 0) {
            return;
        }
        if (resulting == 1) {
            campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.TEACHER_LOTTERY, teacherId, 10);
        }
    }
}
