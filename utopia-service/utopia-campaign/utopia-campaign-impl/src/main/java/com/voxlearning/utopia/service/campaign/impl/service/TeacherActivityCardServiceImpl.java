package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityCardService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCard;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCardOrder;
import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;
import com.voxlearning.utopia.service.campaign.api.mapper.ExchangeFeedMapper;
import com.voxlearning.utopia.service.campaign.impl.service.lottery.DrawException;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.reward.util.TeacherNameWrapper;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Named
@ExposeService(interfaceClass = TeacherActivityCardService.class)
public class TeacherActivityCardServiceImpl extends TeacherActivityCardServiceBase implements TeacherActivityCardService {

    @Override
    public MapMessage index(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        TeacherActivityCard activityCard = teacherActivityCardDao.load(teacherId);

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("teacherId", teacherDetail.getId());
        mapMessage.add("teacherName", teacherDetail.getProfile().getRealname());
        mapMessage.add("cardList", activityCard.converntCardCount());
        mapMessage.add("exchangedFeed", loadExchangeFeed());
        mapMessage.add("opportunity", getOpportunity(teacherId));
        mapMessage.add("userFeed", getUserFeed(teacherId));
        mapMessage.add("subject", teacherDetail.getSubject());
        mapMessage.add("todayLimit", getTodayLimit(teacherId));

        mapMessage.add("assign", activityCard.getAssign());
        mapMessage.add("remind", getRemindStatus(teacherId));                 // 今日是否已成功提醒
        mapMessage.add("share_progress", getShareProgressStatus(teacherId));  // 今日是否已成功分享进度

        boolean shouldSign = Objects.equals(activityCard.getAssign(), true)
                && activityCard.getAssignDate() != null
                && new Date().after(DateUtils.getDayEnd(activityCard.getAssignDate()));
        mapMessage.add("shouldSign", shouldSign);
        mapMessage.add("sign", getSignStatus(teacherId));

        mapMessage.add("giftStock", getGiftStock());
        mapMessage.add("integral1Stock", getIntegral1Stock());
        mapMessage.add("integral2Stock", getIntegral2Stock());

        if (RuntimeMode.lt(Mode.PRODUCTION)) {
            mapMessage.add("daStock", getDaStock());
        }
        return mapMessage;
    }

    @Override
    public List<ExchangeFeedMapper> loadExchangeFeed() {
        CacheObject<List<ExchangeFeedMapper>> curDayFeed = cacheSystem.CBS.persistence.get(FEED_CACHE_KEY);
        return curDayFeed.containsValue() ? curDayFeed.getValue() : new ArrayList<>();
    }

    @Override
    public MapMessage addOpportunity(Long teacherId, String opportunityReason) {
        return super.addOpportunity(teacherId, opportunityReason);
    }

    @Override
    public MapMessage exchange(Long teacherId, String type) {
        try {
            if (new Date().getTime() > ACTIVITY_END_TIME.getTime()) {
                return MapMessage.errorMessage("活动已结束");
            }
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherActivityCardService:exchange")
                    .expirationInSeconds(5 * 60)
                    .keys("ALL")
                    .callback(() -> lockExchange(teacherId, type))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("前方拥挤,请重试...");
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage compose(Long teacherId, String cardA, String cardB) {
        try {
            if (new Date().getTime() > ACTIVITY_END_TIME.getTime()) {
                return MapMessage.errorMessage("活动已结束");
            }
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherActivityCardService:compose")
                    .keys(teacherId)
                    .expirationInSeconds(5 * 60)
                    .callback(() -> inlineCompose(teacherId, cardA, cardB))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    private MapMessage inlineCompose(Long teacherId, String cardA, String cardB) {
        TeacherActivityCard teacherActivityCard = teacherActivityCardDao.load(teacherId);

        if (!teacherActivityCard.contentCard(ActivityCardEnum.valueOf(cardA), ActivityCardEnum.valueOf(cardB))) {
            return MapMessage.errorMessage("卡片不足");
        }

        ActivityCardEnum draw = null;
        try {
            draw = turnOverCardLotteryService.draw(teacherActivityCard);
        } catch (DrawException e) {
            return MapMessage.errorMessage("前方拥挤,请重试...");
        }

        subCard(teacherActivityCard, ActivityCardEnum.valueOf(cardA), ActivityCardEnum.valueOf(cardB));

        Integer mottoOffset = teacherActivityCard.fetchNextMottoOffset();

        // 得到一张卡
        TeacherActivityCard.Card card = new TeacherActivityCard.Card();
        card.setType(draw.name());
        card.setDisabled(false);
        card.setMottoIndex(mottoOffset);

        teacherActivityCard.getCards().add(card);
        teacherActivityCardDao.upsert(teacherActivityCard);

        addUserFeed(teacherId, String.format("通过合成卡片获得了“%s”字箴言卡片", draw.getDesc()));

        return MapMessage.successMessage().add("type", draw.name()).add("offset", mottoOffset);
    }

    @Override
    public MapMessage turnOverCard(Long teacherId) {
        try {
            if (new Date().getTime() > ACTIVITY_END_TIME.getTime()) {
                return MapMessage.errorMessage("活动已结束");
            }
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeacherActivityCardService:turnOverCard")
                    .keys(teacherId)
                    .callback(() -> inlineTurnOverCard(teacherId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage clearUserData(Long teacherId) {
        if (RuntimeMode.isProduction()) {
            return MapMessage.errorMessage();
        }

        List<String> list = new ArrayList<>();
        list.add(genTodayLimitCacheKey(teacherId));    // 用户今天限额
        list.add(genOpportunityCacheKey(teacherId));   // 用户机会
        list.add(genUserFeedCacheKey(teacherId));      // 用户动态
        list.add(genSignCacheKey(teacherId));          // 用户签到状态
        list.add(genRemindCacheKey(teacherId));        // 用户今天是否已提醒学生
        list.add(genShareProgressCacheKey(teacherId)); // 用户今天是否已分享
        cacheSystem.CBS.persistence.deletes(list);

        teacherActivityCardDao.remove(teacherId);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage clearData() {
        if (RuntimeMode.isProduction()) {
            return MapMessage.errorMessage();
        }

        List<String> list = new ArrayList<>();
        list.add(FEED_CACHE_KEY);                      // 今天所有用户的动态
        list.add(NEXT_CARD_CACHE_KEY);                 // 清理下一张卡片
        cacheSystem.CBS.persistence.deletes(list);

        // 清理统计信息
        for (ActivityCardEnum value : ActivityCardEnum.values()) {
            String cache = CacheKeyGenerator.generateCacheKey(CARD_CACHE_KEY, new String[]{"TID"}, new Object[]{value.name()});
            cacheSystem.CBS.persistence.delete(cache);
        }

        // 清理所有库存
        setDaStock(0);
        setGiftStock(0);
        setIntegral1Stock(0);
        setIntegral2Stock(0);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage ctrlNextCard() {
        Long daStock = getDaStock();
        if (daStock < 1L) {
            return MapMessage.errorMessage("本周大字库存不足...");
        }
        boolean success = turnOverCardLotteryService.ctrlNext();
        return success ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    @Override
    public MapMessage statisticsData() {
        return getStatisticsData();
    }

    /**
     * @param type  1 大字 2 1学豆 3 2学豆 4 礼包
     * @param stock 库存
     * @return
     */
    public MapMessage setStock(String type, Integer stock) {
        if (Objects.equals(type, "1")) {
            return super.setDaStock(stock);
        } else if (Objects.equals(type, "2")) {
            return super.setIntegral1Stock(stock);
        } else if (Objects.equals(type, "3")) {
            return super.setIntegral2Stock(stock);
        } else if (Objects.equals(type, "4")) {
            return super.setGiftStock(stock);
        }
        return MapMessage.errorMessage();
    }

    @Override
    public MapMessage sign(Long teacherId) {
        return super.sign(teacherId);
    }

    @Override
    public MapMessage testDraw(Long teacherId) {
        TeacherActivityCard load = teacherActivityCardDao.load(teacherId);
        ActivityCardEnum draw = null;
        try {
            draw = draw(load);
        } catch (DrawException e) {
            return MapMessage.errorMessage("前方拥挤,请重试");
        }
        return MapMessage.successMessage().add("card", draw.name());
    }

    @Override
    public MapMessage setOpportunity(Long teacherId, Long count) {
        String totalCacheKey = genOpportunityCacheKey(teacherId);
        cacheSystem.CBS.persistence.delete(totalCacheKey);
        cacheSystem.CBS.persistence.incr(totalCacheKey, count, count, genExpirationInSeconds());
        return MapMessage.successMessage();
    }

    @Override
    public List<TeacherActivityCard> loadCard(Long startId, Integer size) {
        return teacherActivityCardDao.loadCard(startId, size);
    }

    @Override
    public List<TeacherActivityCardOrder> loadCardOrder() {
        return teacherActivityCardOrderDao.loadAll();
    }

    @Override
    public MapMessage deleteCardOrder(String id) {
        teacherActivityCardOrderDao.remove(id);
        return MapMessage.successMessage();
    }

    private MapMessage inlineTurnOverCard(Long teacherId) {
        TeacherActivityCard teacherActivityCard = teacherActivityCardDao.load(teacherId);

        int opportunity = getOpportunity(teacherId);
        if (opportunity <= 0) {
            return MapMessage.errorMessage("剩余翻阅次数不足").add("code", "201");
        }

        int mottoOffset = teacherActivityCard.fetchNextMottoOffset();

        // 抽一张卡
        ActivityCardEnum draw = null;
        try {
            draw = turnOverCardLotteryService.draw(teacherActivityCard);
        } catch (DrawException e) {
            return MapMessage.errorMessage("前方拥挤,请重试...");
        }

        TeacherActivityCard.Card card = new TeacherActivityCard.Card();
        card.setType(draw.name());
        card.setDisabled(false);
        card.setMottoIndex(mottoOffset);
        card.setCreateTime(new Date());

        teacherActivityCard.setMottoOffset(mottoOffset);
        teacherActivityCard.getCards().add(card);
        teacherActivityCardDao.upsert(teacherActivityCard);

        // 减一次机会
        incrTotal(teacherId, -1L);

        addUserFeed(teacherId, String.format("通过翻阅格言获得了“%s”字箴言卡片", draw.getDesc()));
        return MapMessage.successMessage().add("type", draw.name()).add("offset", mottoOffset);
    }

    private MapMessage lockExchange(Long teacherId, String type) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        TeacherActivityCard teacherActivityCard = teacherActivityCardDao.load(teacherId);

        if (Objects.equals(type, "1")) {
            if (!teacherActivityCard.contentCard(ActivityCardEnum.zhan, ActivityCardEnum.jian)) {
                return MapMessage.errorMessage("卡片不足");
            }

            Long stock = getIntegral1Stock();
            if (stock < 1) {
                return MapMessage.errorMessage("来晚了~").add("code", 201);
            }

            subIntegral1Stock();

            subCard(teacherActivityCard, ActivityCardEnum.zhan, ActivityCardEnum.jian);

            String comment = "消耗了2张卡片兑换了1个园丁豆";
            IntegralHistory integralHistory = IntegralHistoryBuilderFactory.newBuilder(teacherId, IntegralType.TEACHER_2018_WINTERVACATION_CARD_ACTIVITY)
                    .withIntegral(10)
                    .withComment(comment)
                    .build();
            userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);

            addUserFeed(teacherId, comment);

            // 减库存
        } else if (Objects.equals(type, "2")) {
            if (!teacherActivityCard.contentCard(ActivityCardEnum.guan, ActivityCardEnum.shen, ActivityCardEnum.hai)) {
                return MapMessage.errorMessage("卡片不足");
            }

            Long stock = getIntegral2Stock();
            if (stock < 1) {
                return MapMessage.errorMessage("来晚了~").add("code", 201);
            }

            subIntegral2Stock();

            subCard(teacherActivityCard, ActivityCardEnum.guan, ActivityCardEnum.shen, ActivityCardEnum.hai);

            String comment = "消耗了3张卡片兑换了2个园丁豆";
            IntegralHistory integralHistory = IntegralHistoryBuilderFactory.newBuilder(teacherId, IntegralType.TEACHER_2018_WINTERVACATION_CARD_ACTIVITY)
                    .withIntegral(20)
                    .withComment(comment)
                    .build();
            userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);

            addUserFeed(teacherId, comment);

            // 减库存
        } else if (Objects.equals(type, "3")) {
            // 兑换 大礼包
            if (!teacherActivityCard.contentCard(
                    ActivityCardEnum.guan,
                    ActivityCardEnum.hai,
                    ActivityCardEnum.de,
                    ActivityCardEnum.shen,
                    ActivityCardEnum.zhan,
                    ActivityCardEnum.tian,
                    ActivityCardEnum.jian,
                    ActivityCardEnum.da)) {
                return MapMessage.errorMessage("卡片不足");
            }

            Long stock = getGiftStock();
            if (stock < 1) {
                return MapMessage.errorMessage("来晚了~").add("code", 201);
            }

            subGiftStock();

            subCard(
                    teacherActivityCard,
                    ActivityCardEnum.guan,
                    ActivityCardEnum.hai,
                    ActivityCardEnum.de,
                    ActivityCardEnum.shen,
                    ActivityCardEnum.zhan,
                    ActivityCardEnum.tian,
                    ActivityCardEnum.jian,
                    ActivityCardEnum.da
            );

            addUserFeed(teacherId, "消耗了8张卡片兑换了大礼包");

            TeacherActivityCardOrder activityCardOrder = new TeacherActivityCardOrder();
            activityCardOrder.setUserId(teacherId);
            teacherActivityCardOrderDao.insert(activityCardOrder);

            sendSms(teacherId);
        }

        ExchangeFeedMapper exchangeFeedMapper = new ExchangeFeedMapper();
        exchangeFeedMapper.setCTime(new Date());
        exchangeFeedMapper.setSchoolName(teacherDetail.getTeacherSchoolName());
        exchangeFeedMapper.setTeacherId(teacherId);
        exchangeFeedMapper.setTeacherName(TeacherNameWrapper.firstName(teacherDetail.getProfile().getRealname()));
        exchangeFeedMapper.setDesc("兑换成功");
        addExchangeFeed(exchangeFeedMapper);
        return MapMessage.successMessage();
    }

    private void sendSms(Long teacherId) {
        try {
            String userMobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId, "寒假作业奖品兑换提示");
            if (StringUtils.isNotEmpty(userMobile)) {
                smsServiceClient.createSmsMessage(userMobile)
                        .type(SmsType.TEACHER_TASK_REWARD_NOTIFY.name())
                        .content("恭喜您在寒假收集格言活动中成功兑换寒假格言助学礼包，客服将在7个工作日内联系您，请保持手机通话畅通，奖品会在3月10日后发放，请注意查收。")
                        .send();
            }
        } catch (Exception e) {
            logger.warn("寒假作业奖品兑换的提示短信发送失败");
        }
    }

    void subCard(TeacherActivityCard activityCard, ActivityCardEnum... enums) {
        for (ActivityCardEnum anEnum : enums) {
            activityCard.getCards().stream()
                    .filter(card -> (!card.getDisabled()) && Objects.equals(card.getType(), anEnum.name()))
                    .findFirst().ifPresent(disabledCardC -> disabledCardC.setDisabled(true));
        }
        teacherActivityCardDao.upsert(activityCard);
    }

    private void addExchangeFeed(ExchangeFeedMapper feed) {
        List<ExchangeFeedMapper> feedList = loadExchangeFeed();
        feedList.add(0, feed);
        List<ExchangeFeedMapper> subList = feedList.subList(0, Math.min(feedList.size(), 20)); // subList 不能序列化
        cacheSystem.CBS.persistence.set(FEED_CACHE_KEY, DateUtils.getCurrentToDayEndSecond(), new ArrayList<>(subList));
    }


}
