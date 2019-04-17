package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherVocationLottery;
import com.voxlearning.utopia.service.campaign.impl.dao.CampaignLotteryDao;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.campaign.impl.service.TeacherActivityServiceImpl;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 2017 老师 寒假 抽奖
 *
 * @author haitian.gan
 */
@Named
public class TeacherVocationHomeworkWrapper extends AbstractCampaignWrapper {

    private static final int AWARD_D = 4;
    private static final int AWARD_E = 5;
    private static final int MAX_SHARE_LOTTERY_TIME = 40;

    @Inject private RaikouSystem raikouSystem;

    @Inject private TeacherActivityServiceImpl tchActServiceCli;
    @Inject private CampaignLotteryDao campaignLotteryDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;

    @Override
    public MapMessage doLottery(CampaignType campaignType, User user, LotteryClientType clientType) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherVocationLottery:doLottery")
                    .keys(user.getId())
                    .callback(() -> internalDoLottery(campaignType, user, clientType))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("操作太快，请稍候再试!");
        }
    }

    private MapMessage internalDoLottery(CampaignType campaignType, User user, LotteryClientType clientType) {
        if (getLotteryFreeChance(user.getId()) <= 0)
            return MapMessage.errorMessage("对不起，抽奖次数已用完!");

        CampaignLottery result = drawLottery(campaignType.getId());
        if (result == null)
            return MapMessage.successMessage();

        if (result.getRemainAwardNum() <= 0) {
            result = getLastLottery(campaignType.getId());
        }

        long clId = result.getId();
        // 更新剩余的次数
        if (result.getRemainAwardNum() > 0) {
            // 大奖次数的更新要加锁，防止奖励被领多了
            if (result.getAwardId() <= AWARD_D) {
                try {
                    AtomicCallbackBuilderFactory.getInstance()
                            .newBuilder()
                            .keyPrefix("TeacherVocationLottery:updateRemainNum")
                            .keys(result.getCampaignId(), result.getAwardId())
                            .callback(() -> campaignLotteryDao.incRemainAwardNum(clId, -1))
                            .build()
                            .execute();
                } catch (CannotAcquireLockException e) {
                    // 如果同时有两个人
                    result = getLastLottery(campaignType.getId());
                }
            } else
                campaignLotteryDao.incRemainAwardNum(clId, -1);
        }

        // 大奖记录
        if (result.getAwardId() <= AWARD_D) {
            int totalWonNum = campaignLoader.findCampaignLotteryBigHistories(campaignType.getId(), result.getAwardId()).size();
            // 看过往的中大奖记录，中过的也不给
            boolean wonBefore = campaignLoader.findCampaignLotteryBigHistoriesUnderUser(user.getId()).size() > 0;
            if (totalWonNum > result.getTotalAwardLimit() || wonBefore) {
                result = getLastLottery(campaignType.getId());
            } else {
                CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
                bigHistory.setCampaignId(campaignType.getId());
                bigHistory.setAwardId(result.getAwardId());
                bigHistory.setUserId(user.getId());
                bigHistory.setCreateDatetime(new Date());

                addBigHistoryToCache(result, bigHistory);
                campaignService.$insertCampaignLotteryBigHistory(bigHistory);
            }
        }

        // 五等奖给加学豆
        if (result.getAwardId() == AWARD_E) {
            IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.TEACHER_VOCATION_LOTTERY_REWARD, 10);
            integralHistory.setComment("假期作业抽奖奖励");
            MapMessage incIntegralMsg = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
            if (!incIntegralMsg.isSuccess()) {
                return MapMessage.errorMessage("领取失败!增加园丁豆失败!" + incIntegralMsg.getInfo());
            }
        }

        if (result.getAwardId() <= AWARD_E) {
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(campaignType.getId());
            userLotteryHistory.setAwardId(SafeConverter.toInt(result.getAwardId()));
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);
        }

        // 更新抽奖剩余次数
        TeacherVocationLottery lotteryRecord = tchActServiceCli.loadTeacherVocationLottery(user.getId());
        if (lotteryRecord.getFluidTime() > 0) {
            tchActServiceCli.incTVLRecordFields(user.getId(), MapUtils.m("FLUID_TIME", -1));
        } else {
            tchActServiceCli.incTVLRecordFields(user.getId(), MapUtils.m("FIXED_TIME", -1));
        }

        MapMessage resultMsg = MapMessage.successMessage();
        resultMsg.put("campaignId", result.getCampaignId());
        resultMsg.put("awardId", result.getAwardId());
        resultMsg.put("leftChance", getLotteryFreeChance(user.getId()));

        return resultMsg;
    }

    private void addBigHistoryToCache(CampaignLottery lottery, CampaignLotteryBigHistory bigHistory) {
        String key = CacheKeyGenerator.generateCacheKey("YACampaignLotteryHistory",
                new String[]{"campaignLotteriesBigForTime"},
                new Object[]{lottery.getCampaignId()});

        int campaignId = lottery.getCampaignId();

        Map<Integer, CampaignLottery> lotteryMap = campaignLoader.findCampaignLotteries(campaignId)
                .stream()
                .collect(Collectors.toMap(l -> l.getAwardId(), l -> l));

        Function<CampaignLotteryBigHistory, Map<String, Object>> bigHistoryMapper = (bh) -> {
            CampaignLottery l = lotteryMap.get(bh.getAwardId());
            if (l == null) return null;

            Map<String, Object> map = new HashMap<>();

            TeacherDetail td = teacherLoaderClient.loadTeacherDetail(bh.getUserId());
            ExRegion region = raikouSystem.loadRegion(td.getCityCode());

            String regionName = "";
            if (region != null)
                regionName = region.getCityName();

            String teacherName = td.fetchRealname();
            if (!teacherName.endsWith("老师"))
                teacherName = teacherName.substring(0, 1) + "老师";

            map.put("userId", bh.getUserId());
            map.put("userName", regionName + teacherName);
            map.put("awardName", l.getAwardName());
            map.put("awardId", l.getAwardId());
            map.put("date", DateUtils.dateToString(bh.getCreateDatetime(), "MM月dd日"));
            map.put("time", DateUtils.dateToString(bh.getCreateDatetime(), "HH:mm"));

            return map;
        };

        List<Map<String, Object>> recentCampaignLotteries = campaignCacheSystem.CBS.persistence.load(key);
        if (recentCampaignLotteries == null) {
            recentCampaignLotteries = campaignLoader.findCampaignLotteryBigHistories(lottery.getCampaignId())
                    .stream()
                    .map(bigHistoryMapper)
                    .collect(Collectors.toList());
        }

        Map<String, Object> newRecord = bigHistoryMapper.apply(bigHistory);
        recentCampaignLotteries.add(0, newRecord);

        int expireSeconds = (int) DateUtils.minuteDiff(CampaignType.SUMMER_VOCATION_LOTTERY_2018.getExpiredTime(), new Date()) * 60;
        Boolean ret = campaignCacheSystem.CBS.persistence.set(key, expireSeconds, recentCampaignLotteries);
        if (!Boolean.TRUE.equals(ret)) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", key, recentCampaignLotteries);
        }
    }

    @Override
    public Long addLotteryChance(Long userId, int delta) {
        TeacherVocationLottery lotteryRecord = tchActServiceCli.loadTeacherVocationLottery(userId);
        if (lotteryRecord == null) {
            lotteryRecord = new TeacherVocationLottery();
            lotteryRecord.setTeacherId(userId);
            lotteryRecord.setFluidTime(0);
            lotteryRecord.setFixedTime(0);
        }

        checkAndUpdateChance(userId, lotteryRecord);

        if (lotteryRecord.getFixedTime() >= MAX_SHARE_LOTTERY_TIME)
            return (long) lotteryRecord.getFixedTime();

        tchActServiceCli.incTVLRecordFields(userId, MapUtils.m("FIXED_TIME", delta));
        return (long) (lotteryRecord.calTotalTime() + delta);
    }

    @Override
    public int getLotteryFreeChance(Long userId) {

        TeacherVocationLottery lotteryRecord = tchActServiceCli.loadTeacherVocationLottery(userId);
        // 没记录说明没布置过,也没有分享记录
        if (lotteryRecord == null)
            return 0;

        // 检查并自动刷新各类型的抽奖次数
        checkAndUpdateChance(userId, lotteryRecord);

        return lotteryRecord.getFixedTime() + lotteryRecord.getFluidTime();
    }

    private MapMessage checkAndUpdateChance(Long userId, TeacherVocationLottery record) {
        Date todayStart = DayRange.current().getStartDate();
        Date activeTime = record.getActiveTime();
        if (activeTime == null || activeTime.before(todayStart)) {
            try {
                return AtomicCallbackBuilderFactory.getInstance()
                        .<MapMessage>newBuilder()
                        .keyPrefix("TeacherVocationLottery:getFreeChance")
                        .keys(userId)
                        .callback(() -> internalGetLotteryChance(record, todayStart))
                        .build()
                        .execute();
            } catch (CannotAcquireLockException e) {
                // nothing to do...
            }
        }

        return MapMessage.successMessage();
    }

    private MapMessage internalGetLotteryChance(TeacherVocationLottery lotteryRecord, Date activeTime) {
        if (lotteryRecord.getAssignTime() != null && lotteryRecord.getAssignTime() >= 1) {
            lotteryRecord.setActiveTime(activeTime);
            lotteryRecord.setFluidTime(3);
        }
        // 如果已经删光了作业，没有机会的情况，就不走下面的更新了。
        else if (lotteryRecord.isNoChance()) {
            return MapMessage.successMessage();
        } else {
            lotteryRecord.setFluidTime(0);
        }

        // 原来这个也是能过期的，理解错了
        lotteryRecord.setFixedTime(0);
        return tchActServiceCli.updateTeacherVocationLottery(lotteryRecord);
    }
}
