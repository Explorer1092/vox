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

package com.voxlearning.utopia.service.campaign.impl.lottery;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.impl.loader.CampaignLoaderImpl;
import com.voxlearning.utopia.service.campaign.impl.service.CampaignServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by XiaoPeng.Yang on 14-11-4.
 */
abstract public class AbstractCampaignWrapper extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject protected CampaignCacheSystem campaignCacheSystem;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected UserIntegralServiceClient userIntegralServiceClient;
    @Inject protected WechatLoaderClient wechatLoaderClient;
    @Inject protected AmbassadorServiceClient ambassadorServiceClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;

    @Inject protected CampaignLoaderImpl campaignLoader;
    @Inject protected CampaignServiceImpl campaignService;
    @Inject protected EmailServiceClient emailServiceClient;
    @Inject protected ParentLoaderClient parentLoaderClient;

    public abstract MapMessage doLottery(CampaignType campaignType, User user, LotteryClientType clientType);

    public abstract Long addLotteryChance(Long userId, int delta);

    public abstract int getLotteryFreeChance(Long userId);

    /**
     * crm 保存活动奖品配置时会调用, 各活动可自行重写
     */
    public MapMessage validateCampaignLottery(CampaignLottery campaignLottery) {
        try {
            Validate.notEmpty(campaignLottery.getAwardLevelName(), "奖项不能为空");
            Validate.notEmpty(campaignLottery.getAwardName(), "奖品不能为空");
            Validate.notNull(campaignLottery.getAwardRate(), "中奖率不能为空");
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    protected CampaignLottery drawLottery(Integer campaignId) {
        return drawLottery(campaignId, cl -> true);
    }

    protected CampaignLottery drawLottery(Integer campaignId, Predicate<CampaignLottery> filter) {
        AtomicInteger totalWeight = new AtomicInteger(0);
        List<CampaignLottery> campaignLotteries = campaignLoader.findCampaignLotteries(campaignId)
                .stream()
                .filter(filter)
                .peek(cl -> totalWeight.addAndGet(cl.getAwardRate()))
                .collect(Collectors.toList());

        Integer randomResult = RandomUtils.nextInt(totalWeight.get());
        Integer lotteryStart = 0;
        for (CampaignLottery lottery : campaignLotteries) {
            Integer lotteryEnd = lotteryStart + lottery.getAwardRate();
            if (randomResult >= lotteryStart && randomResult < lotteryEnd) {
                return lottery;
            }
            lotteryStart = lotteryEnd;
        }

        return null;
    }

    protected CampaignLottery getLastLottery(Integer campaignId) {
        List<CampaignLottery> campaignLotteries = campaignLoader.findCampaignLotteries(campaignId);
        if (CollectionUtils.isEmpty(campaignLotteries)) {
            return null;
        }
        return campaignLotteries.get(campaignLotteries.size() - 1);
    }

    protected void addRecentCampaignLotteryResultBig(User user, int recentLotterySize, CampaignLottery lottery, Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"recentCampaignLotteriesBig"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.persistence.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = new ArrayList<>();
        }
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("userName", user.getProfile().getRealname());
        lotteryResult.put("awardName", lottery.getAwardName());
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(user.getId()).getUninterruptibly();
        ExRegion exregion = raikouSystem.loadRegion(school.getRegionCode());

        lotteryResult.put("schoolName", school.getCname());
        lotteryResult.put("cityName", exregion.getCityName());
        lotteryResult.put("lotteryDate", DateUtils.dateToString(new Date(), "MM") + "月" + DateUtils.dateToString(new Date(), "dd") + "日");
        lotteryResult.put("datetime", new Date());

        recentCampaignLotteries.add(0, lotteryResult);

        List<Map<String, Object>> recentLotteries = new ArrayList<>();
        for (Map<String, Object> item : recentCampaignLotteries) {
            recentLotteries.add(item);
            if (recentLotteries.size() == recentLotterySize) {
                break;
            }
        }
        Boolean ret = campaignCacheSystem.CBS.persistence.set(key, 7 * 24 * 60 * 60, recentLotteries);
        if (!Boolean.TRUE.equals(ret)) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }

    protected void addRecentCampaignLotteryResultBig(User user, CampaignLottery lottery, Integer campaignId) {
        addRecentCampaignLotteryResultBig(user, 10, lottery, campaignId);
    }

    protected void addRecentCampaignLotteryResultBig(User user, CampaignLottery lottery, Integer campaignId, int expire) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"campaignLotteriesBigForTime"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.persistence.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = new ArrayList<>();
        }
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("userId", user.getId());
        lotteryResult.put("userName", user.getProfile().getRealname());
        lotteryResult.put("awardName", lottery.getAwardName());
        lotteryResult.put("lotteryDate", DateUtils.dateToString(new Date(), "MM") + "月" + DateUtils.dateToString(new Date(), "dd") + "日");

        ExRegion exRegion = userLoaderClient.loadUserRegion(user);
        String provinceName = exRegion == null || exRegion.getProvinceName() == null ? "" : exRegion.getProvinceName();
        lotteryResult.put("cityName", provinceName);

        SimpleDateFormat format = new SimpleDateFormat("MM.dd HH:mm");
        String dateString = format.format(new Date());
        lotteryResult.put("date", dateString);

        recentCampaignLotteries.add(0, lotteryResult);

        List<Map<String, Object>> recentLotteries = new ArrayList<>();
        for (Map<String, Object> item : recentCampaignLotteries) {
            recentLotteries.add(item);
            if (recentLotteries.size() == 10) {
                break;
            }
        }
        Boolean ret = campaignCacheSystem.CBS.persistence.set(key, expire, recentLotteries);
        if (!Boolean.TRUE.equals(ret)) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }

    protected void addCampaignLotteryResultBig(User user, CampaignLottery lottery, Integer campaignId, int expire) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"campaignLotteriesBig"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.persistence.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = new ArrayList<>();
        }
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("userName", getShowName(user));
        lotteryResult.put("awardName", lottery.getAwardName());
        School school = asyncUserServiceClient.getAsyncUserService()
                .loadUserSchool(user)
                .getUninterruptibly();
        if (school != null) {
            lotteryResult.put("schoolName", school.getCname());
        }
        lotteryResult.put("lotteryDate", DateUtils.dateToString(new Date(), "MM") + "月" + DateUtils.dateToString(new Date(), "dd") + "日");
        lotteryResult.put("datetime", new Date());

        recentCampaignLotteries.add(0, lotteryResult);

        Boolean ret = campaignCacheSystem.CBS.persistence.set(key, expire, recentCampaignLotteries);
        if (!Boolean.TRUE.equals(ret)) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }

    private String getShowName(User user) {
        String userName = user.fetchRealname();
        if (user.fetchUserType() == UserType.PARENT) {
            // 获取孩子的名字第一个字 + 称呼
            List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(user.getId());
            if (CollectionUtils.isNotEmpty(refList)) {
                StudentParentRef ref = refList.get(0);
                User stu = userLoaderClient.loadUser(ref.getStudentId());
                String firstName = "杨";
                if (stu != null && StringUtils.isNotBlank(stu.fetchRealname())) {
                    firstName = StringUtils.substring(stu.fetchRealname(), 0, 1);
                }
                userName = firstName + "**" + ref.getCallName();
            }
        }
        return userName;
    }

    protected void addRecentCampaignLotteryResult(User user, CampaignLottery lottery, Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"recentCampaignLotteries"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.flushable.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = new ArrayList<>();
        }
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("userName", getShowName(user));
        lotteryResult.put("awardName", lottery.getAwardName());

        School school = asyncUserServiceClient.getAsyncUserService()
                .loadUserSchool(user)
                .getUninterruptibly();
        if (school != null) {
            lotteryResult.put("schoolName", school.getCname());
        }

        recentCampaignLotteries.add(0, lotteryResult);

        List<Map<String, Object>> recentLotteries = new ArrayList<>();
        for (Map<String, Object> item : recentCampaignLotteries) {
            recentLotteries.add(item);
            if (recentLotteries.size() == 9) {
                break;
            }
        }
        if (!Boolean.TRUE.equals(campaignCacheSystem.CBS.flushable.set(key, 24 * 60 * 60, recentLotteries))) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }

    protected void addRecentCampaignLotteryForWeek(User user, CampaignLottery lottery, Integer campaignId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"CampaignWeek"},
                new Object[]{campaignId});
        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.flushable.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = new ArrayList<>();
        }
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("userId", user.getId());
        lotteryResult.put("userName", user.getProfile().getRealname());
        lotteryResult.put("awardName", lottery.getAwardName());

        School school = asyncUserServiceClient.getAsyncUserService()
                .loadUserSchool(user)
                .getUninterruptibly();
        if (school != null) {
            lotteryResult.put("schoolName", school.getCname());
        }

        recentCampaignLotteries.add(0, lotteryResult);

        List<Map<String, Object>> recentLotteries = new ArrayList<>();
        for (Map<String, Object> item : recentCampaignLotteries) {
            recentLotteries.add(item);
            if (recentLotteries.size() == 10) {
                break;
            }
        }
        if (!Boolean.TRUE.equals(campaignCacheSystem.CBS.flushable.set(key, 7 * 24 * 60 * 60, recentLotteries))) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }

    protected void addRecentCampaignLotteryResultForStudent(User user, CampaignLottery lottery, Integer campaignId, Long clazzId) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"CAMPID", "CID"},
                new Object[]{campaignId, clazzId});
        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.flushable.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = new ArrayList<>();
        }
        Map<String, Object> lotteryResult = new HashMap<>();
        lotteryResult.put("userName", user.getProfile().getRealname());
        lotteryResult.put("awardName", lottery.getAwardName());

        School school = asyncUserServiceClient.getAsyncUserService()
                .loadUserSchool(user)
                .getUninterruptibly();
        if (school != null) {
            lotteryResult.put("schoolName", school.getCname());
        }

        recentCampaignLotteries.add(0, lotteryResult);

        List<Map<String, Object>> recentLotteries = new ArrayList<>();
        for (Map<String, Object> item : recentCampaignLotteries) {
            recentLotteries.add(item);
            if (recentLotteries.size() == 10) {
                break;
            }
        }
        if (!Boolean.TRUE.equals(campaignCacheSystem.CBS.flushable.set(key, 7 * 24 * 60 * 60, recentLotteries))) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }
}
