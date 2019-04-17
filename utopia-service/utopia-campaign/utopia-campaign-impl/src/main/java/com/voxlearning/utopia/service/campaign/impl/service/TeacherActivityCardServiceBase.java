package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCard;
import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;
import com.voxlearning.utopia.service.campaign.api.enums.OpportunityReasonEnum;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityCardDao;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherActivityCardOrderDao;
import com.voxlearning.utopia.service.campaign.impl.service.lottery.*;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TeacherActivityCardServiceBase extends SpringContainerSupport {

    public static final Date CACHE_END_TIME = new Date(1551369599000L);                               // 2019/2/17 23:59:59 延长保存到 2019/2/28 23:59:59
    public static final Date ACTIVITY_END_TIME = new Date(1550419199000L);                            // 2019/2/17 23:59:59

    public static final String FEED_CACHE_KEY = "2018:campaign_card:exchangefeed";                       // 今天所有用户的兑换记录保留20条
    public static final String USER_FEED_CACHE_KEY = "2018:campaign_card:userfeed";                      // 用户最近的动态 保留20条
    public static final String TODAY_LIMIT_CACHE_KEY = "2018:campaign_card:opportunity:daylimit";        // 用户今天已经得到的机会
    public static final String TODAY_OPPORTUNITY_CACHE_KEY = "2018:campaign_card:opportunity";           // 用户剩余机会
    public static final String TODAY_SIGN_CACHE_KEY = "2018:campaign_card:sign";                         // 用户今天是否已签到
    public static final String TODAY_REMIND_CACHE_KEY = "2018:campaign_card:remind";                     // 用户今天是否已提醒学生
    public static final String TODAY_SHARE_PROGRESS_CACHE_KEY = "2018:campaign_card:share_progress";     // 用户今天是否已分享进度
    public static final String TODAY_LIMIT_MSG_CACHE_KEY = "2018:campaign_card:zero_msg";                // 用户今天动态中是否已插入无机会的文案

    public static final String NEXT_CARD_CACHE_KEY = "2018:campaign_card:next_card";                     // 下一张卡片是否是“大”

    public static final String DA_WEEK_STOCK_CACHE_KEY = "2018:campaign_card:week:da:count";             // 本周大字库存
    public static final String INTEGRAL_1_STOCK_CACHE_KEY = "2018:campaign_card:integral:1:count";       // 1学豆库存
    public static final String INTEGRAL_2_STOCK_CACHE_KEY = "2018:campaign_card:integral:2:count";       // 2学豆库存
    public static final String GIFT_STOCK_CACHE_KEY = "2018:campaign_card:gift:count";                   // 大礼包库存

    public static final String CARD_CACHE_KEY = "2018:campaign_card:statistics";                         // 每种类型的卡片一共出去了多少张

    @Inject
    CampaignCacheSystem cacheSystem;
    @Inject
    TeacherActivityCardDao teacherActivityCardDao;
    @Inject
    TeacherActivityCardOrderDao teacherActivityCardOrderDao;
    @Inject
    TeacherLoaderClient teacherLoaderClient;
    @Inject
    TeacherActivityCardServiceBase turnOverCardLotteryService;
    @Inject
    UserIntegralServiceClient userIntegralServiceClient;
    @Inject
    SmsServiceClient smsServiceClient;
    @Inject
    UserLoaderClient userLoaderClient;
    @Inject
    SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    public LotteryCase caseA = new CaseA();
    public LotteryCase caseB = new CaseB();
    public LotteryCase caseC = new CaseC();
    public LotteryCase caseD = new CaseD();

    ActivityCardEnum draw(TeacherActivityCard card) throws DrawException {
        ActivityCardEnum activityCardEnum = inlineDraw(card);
        statisticsData(activityCardEnum);
        return activityCardEnum;
    }

    private ActivityCardEnum inlineDraw(TeacherActivityCard card) throws DrawException {
        boolean contentTian = false;
        boolean contentDa = false;
        boolean contentDe = false;

        for (TeacherActivityCard.Card itemCard : card.getCards()) {
            if (Objects.equals(itemCard.getType(), ActivityCardEnum.tian.name())) contentTian = true;
            if (Objects.equals(itemCard.getType(), ActivityCardEnum.da.name())) contentDa = true;
            if (Objects.equals(itemCard.getType(), ActivityCardEnum.de.name())) contentDe = true;
        }

        LotteryCase currentCase = null;

        if (isTrue(3, contentTian, contentDa, contentDe)) currentCase = caseD;
        if (isTrue(2, contentTian, contentDa, contentDe)) currentCase = caseC;
        if (isTrue(1, contentTian, contentDa, contentDe)) currentCase = caseB;
        if (isTrue(0, contentTian, contentDa, contentDe)) currentCase = caseA;

        assert currentCase != null;

        ActivityCardEnum result = currentCase.draw();

        if (contentDa) {
            return random();
        }

        if (result == ActivityCardEnum.da) {
            return handle(contentDa);
        } else {
            // 不包括"大"时通过后门控制, 如果递增后结果是 1 则直接返回"大"
            Long incr = cacheSystem.CBS.persistence.incr(NEXT_CARD_CACHE_KEY, 1L, 0L, genExpirationInSeconds());
            if (incr == 1) {
                subtractDaStock();
                return ActivityCardEnum.da;
            }
        }

        return result;
    }

    // 这里处理大字的限额逻辑, 比较特殊, 加个全局锁
    // 如果本周大字是否超标
    public ActivityCardEnum handle(boolean contentDa) throws DrawException {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<ActivityCardEnum>newBuilder()
                    .keyPrefix("TeacherActivityCardService:da")
                    .keys("ALL")
                    .expirationInSeconds(5 * 60) // 异常情况5分钟自己解
                    .callback(() -> lockDa(contentDa))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            throw new DrawException();
        }
    }

    private ActivityCardEnum lockDa(boolean contentDa) {
        // 如果已经有"大"或者没库存
        Long weekDa = getDaStock();
        if (contentDa || (weekDa < 1)) {
            return random();
        }
        subtractDaStock();
        return ActivityCardEnum.da;
    }

    ActivityCardEnum random() {
        List<ActivityCardEnum> list = new ArrayList<>();
        list.add(ActivityCardEnum.guan);
        list.add(ActivityCardEnum.hai);
        list.add(ActivityCardEnum.shen);
        list.add(ActivityCardEnum.zhan);
        list.add(ActivityCardEnum.tian);
        list.add(ActivityCardEnum.jian);
        int i = RandomUtils.nextInt(0, list.size() - 1);
        return list.get(i);

    }

    boolean ctrlNext() {
        cacheSystem.CBS.persistence.delete(NEXT_CARD_CACHE_KEY);
        Long incr = cacheSystem.CBS.persistence.incr(NEXT_CARD_CACHE_KEY, 0L, 0L, genExpirationInSeconds());
        return Objects.equals(incr, 0L);
    }

    int genExpirationInSeconds() {
        return SafeConverter.toInt((CACHE_END_TIME.getTime() - new Date().getTime()) / 1000);
    }

    String genTodayLimitCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TODAY_LIMIT_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    Long getTodayLimit(Long teacherId) {
        String todayLimitCacheKey = genTodayLimitCacheKey(teacherId);
        CacheObject<Long> todayLimitCache = cacheSystem.CBS.persistence.get(todayLimitCacheKey);
        if (todayLimitCache != null) {
            return SafeConverter.toLong(todayLimitCache.getValue());
        }
        return 0L;
    }

    void incrTodayLimit(Long teacherId, Long incr) {
        String cacheKey = genTodayLimitCacheKey(teacherId);
        cacheSystem.CBS.persistence.incr(cacheKey, incr, incr, DateUtils.getCurrentToDayEndSecond());
    }

    String genOpportunityCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TODAY_OPPORTUNITY_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    void incrTotal(Long teacherId, Long incr) {
        long initialValue = incr;
        if (incr < 0) initialValue = 0;
        String totalCacheKey = genOpportunityCacheKey(teacherId);
        cacheSystem.CBS.persistence.incr(totalCacheKey, incr, initialValue, genExpirationInSeconds());
    }

    int getOpportunity(Long teacherId) {
        String totalOpportunityCacheKey = genOpportunityCacheKey(teacherId);
        Object opportunity = cacheSystem.CBS.persistence.load(totalOpportunityCacheKey);
        return SafeConverter.toInt(opportunity, 0);
    }

    String genUserFeedCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(USER_FEED_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    List<String> getUserFeed(Long teacherId) {
        String cacheKey = genUserFeedCacheKey(teacherId);
        CacheObject<List<String>> userFeed = cacheSystem.CBS.persistence.get(cacheKey);
        return userFeed.containsValue() ? userFeed.getValue() : new ArrayList<>();
    }

    MapMessage sign(Long teacherId) {
        if (new Date().getTime() > ACTIVITY_END_TIME.getTime()) {
            return MapMessage.errorMessage("活动已结束");
        }
        String signCacheKey = genSignCacheKey(teacherId);
        CacheObject<Object> sign = cacheSystem.CBS.persistence.get(signCacheKey);
        if (sign.containsValue()) {
            return MapMessage.errorMessage("不可重复签到");
        }
        addOpportunity(teacherId, OpportunityReasonEnum.签到.name());
        cacheSystem.CBS.persistence.set(signCacheKey, DateUtils.getCurrentToDayEndSecond(), "1");
        return MapMessage.successMessage();
    }

    @NotNull
    String genSignCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TODAY_SIGN_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    boolean getSignStatus(Long teacherId) {
        String signCacheKey = genSignCacheKey(teacherId);
        CacheObject<Object> sign = cacheSystem.CBS.persistence.get(signCacheKey);
        return sign.containsValue();
    }

    @NotNull
    String genRemindCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TODAY_REMIND_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    boolean getRemindStatus(Long teacherId) {
        String signCacheKey = genRemindCacheKey(teacherId);
        CacheObject<Object> sign = cacheSystem.CBS.persistence.get(signCacheKey);
        return sign.containsValue();
    }

    @NotNull
    String genShareProgressCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TODAY_SHARE_PROGRESS_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    boolean getShareProgressStatus(Long teacherId) {
        String signCacheKey = genShareProgressCacheKey(teacherId);
        CacheObject<Object> sign = cacheSystem.CBS.persistence.get(signCacheKey);
        return sign.containsValue();
    }

    @NotNull
    String genTodayZeroMsgCacheKey(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TODAY_LIMIT_MSG_CACHE_KEY, new String[]{"TID"}, new Object[]{teacherId});
    }

    boolean getTodayZeroMsgStatus(Long teacherId) {
        String signCacheKey = genTodayZeroMsgCacheKey(teacherId);
        CacheObject<Object> sign = cacheSystem.CBS.persistence.get(signCacheKey);
        return sign.containsValue();
    }

    /**
     * 判断是否有n个为真的布尔值
     */
    static boolean isTrue(int trueCount, boolean... list) {
        int count = 0;
        for (boolean item : list) {
            if (item) count++;
        }
        return trueCount == count;
    }

    void subtractDaStock() {
        cacheSystem.CBS.persistence.incr(DA_WEEK_STOCK_CACHE_KEY, -1, 0, DateUtils.getCurrentToWeekEndSecond());
    }

    Long getDaStock() {
        CacheObject<Long> cache = cacheSystem.CBS.persistence.get(DA_WEEK_STOCK_CACHE_KEY);
        if (cache.containsValue()) {
            return SafeConverter.toLong(cache.getValue());
        }
        return 0L;
    }

    MapMessage setDaStock(Integer stock) {
        long count = SafeConverter.toLong(stock);
        cacheSystem.CBS.persistence.delete(DA_WEEK_STOCK_CACHE_KEY);
        cacheSystem.CBS.persistence.incr(DA_WEEK_STOCK_CACHE_KEY, count, count, DateUtils.getCurrentToWeekEndSecond());
        return MapMessage.successMessage();
    }

    MapMessage setIntegral1Stock(Integer stock) {
        long count = SafeConverter.toLong(stock);
        cacheSystem.CBS.persistence.delete(INTEGRAL_1_STOCK_CACHE_KEY);
        cacheSystem.CBS.persistence.incr(INTEGRAL_1_STOCK_CACHE_KEY, count, count, genExpirationInSeconds());
        return MapMessage.successMessage();
    }

    MapMessage setIntegral2Stock(Integer stock) {
        long count = SafeConverter.toLong(stock);
        cacheSystem.CBS.persistence.delete(INTEGRAL_2_STOCK_CACHE_KEY);
        cacheSystem.CBS.persistence.incr(INTEGRAL_2_STOCK_CACHE_KEY, count, count, genExpirationInSeconds());
        return MapMessage.successMessage();
    }

    MapMessage setGiftStock(Integer stock) {
        long count = SafeConverter.toLong(stock);
        cacheSystem.CBS.persistence.delete(GIFT_STOCK_CACHE_KEY);
        cacheSystem.CBS.persistence.incr(GIFT_STOCK_CACHE_KEY, count, count, genExpirationInSeconds());
        return MapMessage.successMessage();
    }

    void subIntegral1Stock() {
        cacheSystem.CBS.persistence.incr(INTEGRAL_1_STOCK_CACHE_KEY, -1L, 0, genExpirationInSeconds());
    }

    void subIntegral2Stock() {
        cacheSystem.CBS.persistence.incr(INTEGRAL_2_STOCK_CACHE_KEY, -1L, 0, genExpirationInSeconds());
    }

    void subGiftStock() {
        cacheSystem.CBS.persistence.incr(GIFT_STOCK_CACHE_KEY, -1L, 0, genExpirationInSeconds());
    }

    Long getIntegral1Stock() {
        CacheObject<Object> cache = cacheSystem.CBS.persistence.get(INTEGRAL_1_STOCK_CACHE_KEY);
        if (!cache.containsValue()) {
            return 0L;
        }
        return SafeConverter.toLong(cache.getValue());
    }

    Long getIntegral2Stock() {
        CacheObject<Object> cache = cacheSystem.CBS.persistence.get(INTEGRAL_2_STOCK_CACHE_KEY);
        if (!cache.containsValue()) {
            return 0L;
        }
        return SafeConverter.toLong(cache.getValue());
    }

    Long getGiftStock() {
        CacheObject<Object> cache = cacheSystem.CBS.persistence.get(GIFT_STOCK_CACHE_KEY);
        if (!cache.containsValue()) {
            return 0L;
        }
        return SafeConverter.toLong(cache.getValue());
    }

    /**
     * 统计每种类型的卡片出去了多少张
     */
    private void statisticsData(ActivityCardEnum activityCardEnum) {
        String cache = CacheKeyGenerator.generateCacheKey(CARD_CACHE_KEY, new String[]{"TID"}, new Object[]{activityCardEnum.name()});
        cacheSystem.CBS.persistence.incr(cache, 1, 1, genExpirationInSeconds());
    }

    MapMessage getStatisticsData() {
        MapMessage mapMessage = MapMessage.successMessage();
        for (ActivityCardEnum value : ActivityCardEnum.values()) {
            String cache = CacheKeyGenerator.generateCacheKey(CARD_CACHE_KEY, new String[]{"TID"}, new Object[]{value.name()});
            CacheObject<Object> objectCacheObject = cacheSystem.CBS.persistence.get(cache);
            if (objectCacheObject.containsValue()) {
                mapMessage.put(value.getDesc(), SafeConverter.toLong(objectCacheObject.getValue()));
            } else {
                mapMessage.put(value.getDesc(), 0L);
            }
        }
        mapMessage.put("daStock", getDaStock());
        mapMessage.add("giftStock", getGiftStock());
        mapMessage.add("integral1Stock", getIntegral1Stock());
        mapMessage.add("integral2Stock", getIntegral2Stock());
        return mapMessage;
    }

    void addUserFeed(Long teacherId, String feed) {
        String cacheKey = genUserFeedCacheKey(teacherId);
        CacheObject<List<String>> cache = cacheSystem.CBS.persistence.get(cacheKey);

        List<String> feedList = new ArrayList<>();
        if (cache.containsValue()) {
            feedList = cache.getValue();
        }

        feedList.add(0, feed);
        List<String> subList = feedList.subList(0, Math.min(feedList.size(), 20));
        cacheSystem.CBS.persistence.set(cacheKey, genExpirationInSeconds(), new ArrayList<>(subList));
    }


    MapMessage addOpportunity(Long teacherId, String opportunityReason) {
        try {
            if (new Date().getTime() > ACTIVITY_END_TIME.getTime()) {
                return MapMessage.errorMessage("活动已结束");
            }
            return retryAddOpportunity(teacherId, opportunityReason, 3);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    MapMessage retryAddOpportunity(Long teacherId, String opportunityReason, int maxRetry) throws Exception {
        try {
            if (maxRetry <= 0) {
                return MapMessage.errorMessage();
            }
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherActivityCardService:lockAddOpportunity")
                    .keys(teacherId)
                    .callback(() -> inlineAddOpportunity(teacherId, opportunityReason))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            Thread.sleep(800);
            return retryAddOpportunity(teacherId, opportunityReason, maxRetry - 1);
        }
    }

    MapMessage inlineAddOpportunity(Long teacherId, String opportunityReason) {
        OpportunityReasonEnum type = OpportunityReasonEnum.safeValueOf(opportunityReason);
        if (type == null) {
            logger.info("未知的类型");
            return MapMessage.successMessage();
        }

        // 没布置假期作业什么都不加
        TeacherActivityCard activityCard = teacherActivityCardDao.load(teacherId);
        if ((type != OpportunityReasonEnum.布置假期作业) && (!activityCard.getAssign())) {
            return MapMessage.successMessage("还未布置假期作业");
        }

        // 限制次数的重复完成也不加
        if (type == OpportunityReasonEnum.布置假期作业) {
            if (activityCard.getAssign()) {
                return MapMessage.successMessage("已经布置过假期作业");
            }
        } else if (type == OpportunityReasonEnum.提醒学生) {
            boolean status = getRemindStatus(teacherId);
            if (status) {
                return MapMessage.successMessage("今日已提醒");
            }
        } else if (type == OpportunityReasonEnum.分享班级进度) {
            boolean status = getShareProgressStatus(teacherId);
            if (status) {
                return MapMessage.successMessage("今日已分享");
            }
        }

        // 今日已得机会次数
        long todayLimitLong = getTodayLimit(teacherId);

        if (todayLimitLong >= 15) {
            boolean status = getTodayZeroMsgStatus(teacherId);
            if (!status) {
                addUserFeed(teacherId, "今日获得翻阅次数已到达上限，完成任务将不会获得额外次数");
                cacheSystem.CBS.persistence.set(genTodayZeroMsgCacheKey(teacherId), DateUtils.getCurrentToDayEndSecond(), "1");
            }
            return MapMessage.successMessage("机会超限");
        }

        long incr = type.getCount();

        // 处理 14+2 的情况, 这种不能超过15的限制,只能 +1
        long added = todayLimitLong + type.getCount();
        if (added > 15) {
            incr = 15 - todayLimitLong;
            logger.info("teacherId:{}, reason:{} incr:{}", teacherId, opportunityReason, incr);
        }

        incrTodayLimit(teacherId, incr);
        incrTotal(teacherId, incr);

        addUserFeed(teacherId, String.format("通过%s获得了%s次翻阅格言的机会", type.getName(), incr));

        // 这两个一天只能累加一次机会
        if (type == OpportunityReasonEnum.布置假期作业) {
            activityCard.setAssign(true);
            activityCard.setAssignDate(new Date());
            teacherActivityCardDao.upsert(activityCard);
        } else if (type == OpportunityReasonEnum.提醒学生) {
            cacheSystem.CBS.persistence.set(genRemindCacheKey(teacherId), DateUtils.getCurrentToDayEndSecond(), "1");
        } else if (type == OpportunityReasonEnum.分享班级进度) {
            cacheSystem.CBS.persistence.set(genShareProgressCacheKey(teacherId), DateUtils.getCurrentToDayEndSecond(), "1");
        }

        return MapMessage.successMessage();
    }
}
