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

package com.voxlearning.utopia.service.nekketsu.adventure.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.nekketsu.adventure.api.AdventureService;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AdventureConstants;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.StageAppType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.*;
import com.voxlearning.utopia.service.nekketsu.base.cache.NekketsuCacheSystem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 沃克大冒险ServiceImpl
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 14:18
 * <pre>
 *                        _ooOoo_
 *                       o8888888o
 *                       88" . "88
 *                       (| -_- |)
 *                       O\  =  /O
 *                    ____/`---'\____
 *                  .'  \\|     |//  `.
 *                 /  \\|||  :  |||//  \
 *                /  _||||| -:- |||||-  \
 *                |   | \\\  -  /// |   |
 *                | \_|  ''\---/''  |   |
 *                \  .-\__  `-`  ___/-. /
 *              ___`. .'  /--.--\  `. . __
 *           ."" '<  `.___\_<|>_/___.'  >'"".
 *          | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *          \  \ `-.   \_ __\ /__ _/   .-` /  /
 *     ======'-.____`-.___\_____/___.-`____.-'======
 *                        `=---='
 *     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * </pre>
 */

@Named
@Service(interfaceClass = AdventureService.class)
@ExposeService(interfaceClass = AdventureService.class)
public class AdventureServiceImpl extends AdventureSupport implements AdventureService {

    @Inject private NekketsuCacheSystem nekketsuCacheSystem;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    public MapMessage createUserAdventure(Long userId, Long bookId, List<String> words, Integer clazzLevel) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        if (null == userAdventure) {
            userAdventure = UserAdventure.newInstance(userId, bookId, BookStages.generateId(userId, bookId));
            userAdventureDao.insert(userAdventure);
        }
        BookStages bookStages = bookStagesDao.load(userAdventure.getBookStagesId());
        if (null == bookStages) {
            try {
                bookStages = initBookStages(userId, bookId, words, clazzLevel);
                bookStagesDao.insert(bookStages);
            } catch (Exception e) {
                try {
                    userAdventureDao.delete(userId);
                } catch (Exception ignored) {
                }
                return MapMessage.errorMessage().setErrorCode("101015").setInfo("创建教材与关卡对应信息失败");
            }
        }

        userAdventure.getAchievements().get(AchievementType.LOGIN).increaseTotalCount(1);
        nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.LOGIN_CACHE_KEY_PREFIX + userId, DateUtils.getCurrentToDayEndSecond(), true);

        setUserImage(userAdventure);
        MapMessage response = MapMessage.successMessage().add("userAdventure", userAdventure).add("bookStages", bookStages);
        boolean receiveFbSwitch = receiveFbSwitch(userId);
        response.add("receiveFbSwitch", receiveFbSwitch);
        return response;
    }

    /**
     * 判断此用户是否展示领学豆消息,展示条件：
     * 1、从未支付购买过套餐
     * 2、展示套餐开关是开启的
     */
    private boolean receiveFbSwitch(Long userId) {
        User user = userLoaderClient.loadUser(userId);
        if (user.isStudent()) {
            List<UserOrder> list = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.Walker.name(), userId);
            return CollectionUtils.isEmpty(list);
        }
        return false;
    }


    /**
     * 提交报告领取学习豆
     *
     * @param userId
     * @param bookId
     * @param stageOrder
     * @return
     */
    public MapMessage receiveFreeBeans(Long userId, Long bookId, Integer stageOrder) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return MapMessage.errorMessage().set("errMsg", "用户不存在");
        }
        BookStages bookStages = bookStagesDao.load(BookStages.generateId(userId, bookId));

        Stage stage = bookStages.getStages().get(stageOrder);

        if (stage == null) {
            return MapMessage.errorMessage().set("errMsg", "不存在关卡信息");
        } else if (stage.getIsReceived() != null && stage.getIsReceived() == true) {
            return MapMessage.errorMessage().set("errMsg", "当前学豆已经被领取");
        } else if (stage.getReceiveFreeBeans() != null && stage.getReceiveFreeBeans() == false) {
            return MapMessage.errorMessage().set("errMsg", "无法领取当前学豆");
        }

        UserAdventure userAdventure = userAdventureDao.load(userId);

        //判断已经玩的关数是否超过免费关数，并且判断当天是否已经领过
        if (userAdventure != null && userAdventure.getTrialCount() <= AdventureConstants.RECEIVE_FREE_MAX_STAGE) {

            //当天只能领取一次
            if (userAdventure != null && userAdventure.getFinalReceiveFbTime() != null &&
                    DateUtils.dateToString(new Date(), "yyyy-MM-dd").equals(DateUtils.dateToString(userAdventure.getFinalReceiveFbTime(), "yyyy-MM-dd")) == true) {
                return MapMessage.errorMessage().set("errMsg", "每天只能领取一次试用学豆奖励，你今天已经领取过了，明天再来看看吧");
            }
        }

        //领取学豆并修改积分
        MapMessage receiveMessage = isReceiveFreeBeans(userId, bookId, stageOrder);
        if (receiveMessage.isSuccess()) {
            IntegralHistory integralHistory = new IntegralHistory();
            integralHistory.setUserId(userId);
            integralHistory.setIntegral(1);
            integralHistory.setIntegralType(IntegralType.WALKE_PROBATION_REWARD.getType());
            integralHistory.setComment(IntegralType.WALKE_PROBATION_REWARD.getDescription());

            int wordsNum = stage.getWords().size();

            MapMessage message = userIntegralService.changeIntegral(user, integralHistory);
            if (message.isSuccess()) {
                //修改此关卡不能领
                Boolean receiveFreeBeans = false;
                Date receiveBeansTime = new Date();
                Boolean isReceived = true;
                bookStagesDao.updateStage(userId, bookId, stageOrder, false, receiveBeansTime, true);
                userAdventureDao.updateFinalReceiveFbTime(userId, new Date());
                boolean flag = pushLearningReport(userId, wordsNum, bookId, stageOrder);
                if (flag) {
                    return MapMessage.successMessage().setInfo("领取成功，并报告提交成功");
                } else {
                    return MapMessage.successMessage().setInfo("领取成功，提交报告失败");
                }
            } else {
                return MapMessage.errorMessage().set("errMsg", "添加学习豆失败，请稍后再来");
            }

        } else {
            return receiveMessage;
        }

    }

    /**
     * 通过userId与用户闯关单词数量信息，推送学习报告
     *
     * @param userId
     * @param wordsNum
     * @return
     */
    public boolean pushLearningReport(Long userId, int wordsNum, Long bookId, Integer stageOrder) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return false;
        }

        //调用推送接口，推送学习报告
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(userId);
        if (studentParents == null || studentParents.size() == 0) {
            return false;
        }
        Set<Long> parentIdSet = new HashSet<>();
        for (StudentParent studentParent : studentParents) {
            parentIdSet.add(studentParent.getParentUser().getId());
        }

        List<Long> parentIds = new ArrayList<>(parentIdSet);

        // 文本：xxx（学生名）家长您好，您的孩子今天在《沃克大冒险》自学了x个单词，请您查看掌握情况>>

        StringBuilder contentStringBuilder = new StringBuilder();
        contentStringBuilder.append(user.getProfile().getRealname()).append("家长您好，您的孩子今天在《沃克单词冒险》自学了");
        contentStringBuilder.append(wordsNum);
        contentStringBuilder.append("个单词，请您查看掌握情况>>");

        StringBuilder linkUrl = new StringBuilder();
        linkUrl.append("/parentMobile/adventure/getLearningWordsReport.vpage?").append("userId=").append(userId);
        linkUrl.append("&bookId=").append(bookId);
        linkUrl.append("&stageOrder=").append(stageOrder);
        boolean flag = postParentMessage(parentIds, userId, contentStringBuilder.toString(),
                linkUrl.toString(), null, ParentMessageTag.资讯, ParentMessageType.REMINDER);
        if (flag == false) {
            logger.error("pushLearningReport faild: postParentMessage reuslt false,userId={},bookId={},stargeOrder={}", userId, bookId, stageOrder);
        }
        return flag;
    }

    //向家长端推送消息
    private boolean postParentMessage(List<Long> parentIds, Long studentId, String content, String linkUrl, String senderName, ParentMessageTag tag,
                                      ParentMessageType type) {

        List<AppMessage> messageList = new ArrayList<>();
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("studentId", studentId);
        extInfo.put("tag", tag == null ? "" : tag.name());
        extInfo.put("type", type == null ? "" : type.name());
        extInfo.put("senderName", senderName);
        extInfo.put("action", "");
        extInfo.put("activityType", "walkerTrialReport");
        for (Long parentId : parentIds) {
            //新消息中心
            AppMessage message = new AppMessage();
            message.setUserId(parentId);
            message.setContent(content);
            message.setLinkUrl(linkUrl);
            message.setExtInfo(extInfo);
            message.setMessageType(type.getType());
            messageList.add(message);
        }
        messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("studentId", studentId);
        extras.put("url", linkUrl);
        extras.put("tag", tag == null ? "" : tag.name());
        extras.put("s", ParentAppPushType.NOTICE.name());
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentIds, extras);
        return true;
    }

    /**
     * 闯关结果提交
     *
     * @param userId
     * @param bookId
     * @param stageOrder
     * @param appOrder
     * @param fileName
     * @param count
     * @param classLevel
     * @param trial
     * @return
     */
    public MapMessage stageMisson(Long userId, Long bookId, Integer stageOrder, Integer appOrder, String fileName,
                                  Integer count, Integer classLevel, Boolean trial) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        if (null == userAdventure) {
            return MapMessage.errorMessage();
        }
        if (!Objects.equals(userAdventure.getBookId(), bookId)) {
            return MapMessage.errorMessage().setErrorCode("101007").setInfo("传入的教材ID与当前教材不一致");
        }
        MapMessage response = MapMessage.successMessage();
        BookStages bookStages = bookStagesDao.load(BookStages.generateId(userId, bookId));
        if (bookStages.getCurrentStage() < stageOrder) {
            return response.setSuccess(false).setErrorCode("101000").setInfo("CurrentStage is : " + bookStages.getCurrentStage());
        }
        Stage stage = bookStages.getStages().get(stageOrder);
        StageApp stageApp = stage.getApps().get(appOrder);
        if (!Objects.equals(stageApp.getFileName(), fileName)) {
            return response.setSuccess(false).setErrorCode("101008").setInfo("传入游戏与数据库存储游戏不对应");
        }
        if (0 == count) {
            setUserImage(userAdventure);
            setNewFlag(userId, response);
            return response.add("userAdventure", userAdventure).add("bookStages", bookStages).add("appId", stageApp.getAppId());
        }

        if (!stageApp.getOpen()) {
            return response.setSuccess(false).setErrorCode("101009").setInfo("小游戏未开启");
        }

        if (1 == appOrder && 0 == stageApp.getDiamond() && count > 0) {
            String flag = nekketsuCacheSystem.CBS.unflushable.load(AdventureConstants.NEW_STAGE_CACHE_KEY_PREFIX + userId);
            if (StringUtils.isEmpty(flag)) {
                nekketsuCacheSystem.CBS.unflushable.add(AdventureConstants.NEW_STAGE_CACHE_KEY_PREFIX + userId,
                        DateUtils.getCurrentToDayEndSecond(),
                        userAdventure.getBookId() + "_" + stageOrder);
            } else {
                if (RuntimeMode.ge(Mode.STAGING)) {
                    return response.setSuccess(false).setErrorCode("101014").setInfo("每天只能提交一次任一关卡第一个小游戏");
                }
            }
        }

        int currentObtainDiamond = count;
        int realObtainDiamond = 0;
        List<Achievement> achievementList = new ArrayList<>();
        //关卡内小应用首次获得三星奖励1学豆，全部基础应用三星获得一个皇冠，首次获得皇冠奖励3学豆和1pk活力
        if (stageApp.getDiamond() < currentObtainDiamond) {
            realObtainDiamond = currentObtainDiamond - stageApp.getDiamond();
            stageApp.setDiamond(currentObtainDiamond);

            if (stageApp.canGrantGift()) {
                stageApp.setObtainReward(true);
                nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.NEW_GIFT_CACHE_KEY_PREFIX + userId,
                        DateUtils.getCurrentToDayEndSecond(), true);//增加新礼物标识的缓存
                Gift gift = Gift.createStageBaseAppDiamondGift(userId, bookId, stageOrder, appOrder, stageApp.getName());
                giftDao.insert(gift);
            }
        }
        Integer nextOpenAppOrder = 0;
        if (realObtainDiamond >= 1 && stageApp.getOrder() < stage.getApps().size()
                && !stage.getApps().get(stageApp.getOrder() + 1).getOpen()) {
            nextOpenAppOrder = stageApp.getOrder() + 1;
        }
        boolean openNewStage = false;

        //此处是最新的数据
        bookStages = bookStagesDao.updateStageApp(userId, bookId, stageOrder, appOrder, realObtainDiamond, stageApp.getObtainReward(), nextOpenAppOrder);
        if (stage.getApps().size() >= AdventureConstants.BaseAppCount.BASE_APP_COUNT(classLevel)
                && stage.getOrder() < AdventureConstants.MAX_STAGE) {//判断是否打开下一关卡
            boolean flag = true;
            for (StageApp app : stage.getApps().values()) {
                if (StageAppType.BASE == app.getType() && (null == app.getDiamond() || app.getDiamond() == 0)) {
                    flag = false;
                    break;
                }
            }
            flag = flag && !bookStages.getStages().containsKey(stageOrder + 1);
            if (flag && Objects.equals(bookStages.getCurrentStage(), stageOrder)) {//注意，这里初始化的关卡没有单词
                Stage newStage = Stage.newInstance(stageOrder + 1, new LinkedList<String>(),
                        randomStageApps(classLevel), stage.getTotalDiamond());
                bookStagesDao.addStage(userId, bookId, newStage);
                bookStages.getStages().put(newStage.getOrder(), newStage);

                //关卡成就
                Achievement achievement = userAdventure.getAchievements().get(AchievementType.STAGE);
                achievement.increaseTotalCount(1);
                achievement.calculateNextLevelCount();
                if (achievement.canReceive()) {
                    nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.NEW_ACHIEVEMENT_CACHE_KEY_PREFIX + userId,
                            DateUtils.getCurrentToDayEndSecond(), true);//增加新成就标识的缓存
                }
                achievementList.add(achievement);
                openNewStage = true;
            }
        }

        //初始化用户是否可以领取试用学习豆奖励
        MapMessage receiveMessage = isReceiveFreeBeans(userId, bookId, stageOrder);
        if (receiveMessage.isSuccess()) {
            Boolean receiveFreeBeans = true;
            Date receiveBeansTime = null;
            Boolean isReceived = false;
            bookStages = bookStagesDao.updateStage(userId, bookId, stageOrder, receiveFreeBeans, receiveBeansTime, isReceived);
        } else {

        }

        if (realObtainDiamond > 0) {
            //钻石成就
            Achievement achievement = userAdventure.getAchievements().get(AchievementType.DIAMOND);
            achievement.increaseTotalCount(realObtainDiamond);
            achievement.calculateNextLevelCount();
            if (achievement.canReceive()) {
                nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.NEW_ACHIEVEMENT_CACHE_KEY_PREFIX + userId,
                        DateUtils.getCurrentToDayEndSecond(), true);//增加新成就标识的缓存
            }
            achievementList.add(achievement);
        }
        if (CollectionUtils.isNotEmpty(achievementList)) {
            if (null == trial) {
                trial = false;
            }
            userAdventure = userAdventureDao.updateAchievement(userId, trial && openNewStage, realObtainDiamond, achievementList);
        }
        setUserImage(userAdventure);
        setNewFlag(userId, response);
        return response.add("userAdventure", userAdventure).add("bookStages", bookStages).add("appId", stageApp.getAppId());
    }

    /**
     * 判断当前关卡用户是否可以免费领取学习豆
     */
    private MapMessage isReceiveFreeBeans(Long userId, Long bookId, Integer stageOrder) {
        BookStages bookStages = bookStagesDao.load(BookStages.generateId(userId, bookId));
        Stage stage = bookStages.getStages().get(stageOrder);
        //关卡为空不能够显示
        if (stage == null) {
            return MapMessage.errorMessage().set("errMsg", "关卡为空");
        }
//        //没有获取全部钻石，不可以领取
        if (stage.getObtainDiamond() != stage.getTotalDiamond()) {
            return MapMessage.errorMessage().set("errMsg", "没有获取到全部钻石");
        }
        //用户已经领取,不能够再领取
        if (stage.getIsReceived() != null && stage.getIsReceived()) {
            return MapMessage.errorMessage().set("errMsg", "已经领取");
        }

        //用户已经有订单不可以继续领取
        List<UserOrder> list = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.Walker.name(), userId);
        if (list != null && list.size() > 0) {
            return MapMessage.errorMessage().set("errMsg", "你属于付费过的用户");
        }

        return MapMessage.successMessage();
    }

    /**
     * 关卡皇冠奖励、分享，分享成就，同时皇冠数加1
     *
     * @param userId     userId
     * @param stageOrder 关卡顺序
     * @param shared     是否分享
     * @return UserAdventure和BookStages
     */
    public MapMessage stageCrownReward(Long userId, Integer stageOrder, Boolean shared) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        BookStages bookStages = bookStagesDao.load(userAdventure.getBookStagesId());
        Stage stage = bookStages.getStages().get(stageOrder);

        boolean allAppFullDiamond = true;
        for (StageApp stageApp : stage.getStageAppList()) {
            if (StageAppType.BASE == stageApp.getType() &&
                    AdventureConstants.STAGEAPP_MAX_DIAMOND > stageApp.getDiamond()) {
                allAppFullDiamond = false;
                break;
            }
        }

        if (!allAppFullDiamond) {
            return MapMessage.errorMessage().setErrorCode("101011").setInfo("第“" + stage.getOrder() + "”关没有获得皇冠");
        }

        if (stage.getDecorativeCrown()) {
            return MapMessage.errorMessage().setInfo("第“" + stage.getOrder() + "”关已获得皇冠");
        }

        bookStagesDao.decorativeCrown(userId, bookStages.getBookId(), stage.getOrder());
        bookStages.getStages().get(stageOrder).setDecorativeCrown(true);
        userAdventureDao.increaseCrown(userId, 1);
        userAdventure.increaseCurrentCrown(1);
        Gift gift = Gift.createCrownGift(userId, bookStages.getBookId(), stage.getOrder());
        giftDao.insert(gift);
        //增加新礼物标识的缓存
        nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.NEW_GIFT_CACHE_KEY_PREFIX + userId, DateUtils.getCurrentToDayEndSecond(), true);
        if (shared) {
            updateSharedAchievement(userAdventure);
        }
        setUserImage(userAdventure);
        MapMessage response = MapMessage.successMessage().add("userAdventure", userAdventure).add("bookStages", bookStages);
        setNewFlag(userId, response);
        return response;
    }

    private void updateSharedAchievement(UserAdventure userAdventure) {
        Achievement achievement = userAdventure.getAchievements().get(AchievementType.SHARED);
        achievement.increaseTotalCount(1);

        //分享成就
        achievement.calculateNextLevelCount();
        if (achievement.canReceive()) {
            nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.NEW_ACHIEVEMENT_CACHE_KEY_PREFIX + userAdventure.getId(),
                    DateUtils.getCurrentToDayEndSecond(), true);//增加新成就标识的缓存
        }
        userAdventureDao.updateAchievement(userAdventure.getId(), achievement);
    }

    public MapMessage openStage(Long userId, Integer stageOrder, Boolean shared) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        BookStages bookStages = bookStagesDao.load(userAdventure.getBookStagesId());
        if (shared) {
            updateSharedAchievement(userAdventure);
        }
        MapMessage response = MapMessage.successMessage();
        if (bookStages.getStages().containsKey(stageOrder) && Objects.equals(stageOrder, (bookStages.getCurrentStage() + 1))) {
            bookStages.setCurrentStage(stageOrder);
            bookStagesDao.increaseCurrentStage(userId, userAdventure.getBookId());
            response.add("userAdventure", userAdventure).add("bookStages", bookStages);
        } else {
            response.setSuccess(false).setErrorCode("").setInfo("要开启的关卡顺序“" + stageOrder + "”非法");
        }
        setNewFlag(userId, response);
        return response;
    }

    public MapMessage changeBook(Long userId, Long bookId, List<String> words, Integer clazzLevel) {
        BookStages bookStages = bookStagesDao.load(BookStages.generateId(userId, bookId));
        Long newBookId = null;
        if (null == bookStages) {
            newBookId = bookId;
            bookStages = initBookStages(userId, bookId, words, clazzLevel);
            if (null == bookStages) {
                return MapMessage.errorMessage().setInfo("更换教材初始化小游戏失败");//FIXME
            }
            bookStagesDao.insert(bookStages);
        }
        //更换教材时，需要同时处理关卡成就信息
        UserAdventure userAdventure = userAdventureDao.changeBookStagesId(userId, bookStages.getId(), newBookId);
        setUserImage(userAdventure);
        return MapMessage.successMessage().add("userAdventure", userAdventure)
                .add("bookStages", bookStages);
    }

    public MapMessage grantGift(String id) {
        Gift gift = giftDao.grantGift(id);
        MapMessage response = processGift(gift);
        if (!response.isSuccess()) {
            logger.error("领取礼物失败");
            return response;
        }
        List<Gift> gifts = giftDao.getUngrantGifts(gift.getUserId(), gift.getType());
        userAdventureDao.increaseBeanAndPkVitalityCount(gift.getUserId(), gift.getBeanNum(), gift.getPkVitalityNum());
        return MapMessage.successMessage().add("gift", gift).add("gifts", gifts);
    }

    public MapMessage grantAchievement(Long userId, AchievementType achievementType) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        Achievement achievement = userAdventure.getAchievements().get(achievementType);
        if (achievement.canReceive()) {
            Gift gift = Gift.createAchivementGift(userId, userAdventure.getBookId(), achievementType,
                    achievement.getNextReceiveLevelCount());
            giftDao.insert(gift);
            MapMessage response = processGift(gift);
            if (!response.isSuccess()) {
                logger.error("领取成就失败");
                return response;
            }
            achievement.calculateNextReceiveLevelCount();
            nekketsuCacheSystem.CBS.flushable.delete(AdventureConstants.NEW_ACHIEVEMENT_CACHE_KEY_PREFIX + userId);
            userAdventureDao.updateNextReceiveLevelCount(userId, achievement.getNextReceiveLevelCount(), achievementType);
            setUserImage(userAdventure);
            return MapMessage.successMessage().add("gift", gift).add("userAdventure", userAdventure);
        }
        return MapMessage.errorMessage().setInfo("没有可领取的成就");
    }

    private MapMessage processGift(Gift gift) {
        if (gift.getBeanNum() > 0) {
            IntegralHistory integralHistory = new IntegralHistory(gift.getUserId(), IntegralType.沃克大冒险奇幻探险获得学豆);
            integralHistory.setIntegral(gift.getBeanNum());
            integralHistory.setComment("在沃克单词冒险之奇幻探险中，" + gift.getContent());
            return userIntegralService.changeIntegral(integralHistory);
        }
        return MapMessage.errorMessage();
    }

    public MapMessage addStageWords(Long userId, Long bookId, Integer stageOrder, List<String> words) {
        bookStagesDao.addStageWords(userId, bookId, stageOrder, words);
        return MapMessage.successMessage();
    }

    public MapMessage openNextStageGroup(Long userId) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        if (userAdventure.getCurrentCrown() < AdventureConstants.OPEN_15_STAGE_CROWN) {
            return MapMessage.errorMessage().setErrorCode("101004").setInfo("皇冠数够，目前有：" + userAdventure.getCurrentCrown());
        }

        BookStages bookStages = bookStagesDao.load(userAdventure.getBookStagesId());
        if (bookStages.getOpenedStage() > bookStages.getCurrentStage()) {
            return MapMessage.errorMessage().setErrorCode("").setInfo("门下开门，开了");
        }
        if (bookStages.getOpenedStage() >= AdventureConstants.MAX_STAGE) {
            return MapMessage.errorMessage().setErrorCode("101005").setInfo("已经打开全部关卡");
        }

        userAdventureDao.increaseCrown(userId, -9);
        userAdventure.setCurrentCrown(userAdventure.getCurrentCrown() - 9);
        bookStagesDao.increaseOpenedStage(bookStages.getId());
        bookStages.setOpenedStage(bookStages.getOpenedStage() + 15);
        return MapMessage.successMessage().add("userAdventure", userAdventure).add("bookStages", bookStages);
    }

    public MapMessage addSystemApp(SystemApp systemApp) {
        systemAppDao.insert(systemApp);
        return MapMessage.successMessage();
    }

    public MapMessage deleteSystemApp(Long id) {
        boolean ret = systemAppDao.delete(id);
        return new MapMessage().setSuccess(ret);
    }

    public MapMessage changeSystemAppValid(Long id) {
        systemAppDao.changeSystemAppValid(id);
        return MapMessage.successMessage();
    }

    public MapMessage exchangePkEquipment(Long userId, Integer diamondCount, String equipmentOriginalId) {
        UserAdventure userAdventure = userAdventureDao.decreaseCurrentDiamond(userId, diamondCount);
        PkEquipmentExchange pkEquipmentExchange = new PkEquipmentExchange();
        pkEquipmentExchange.setUserId(userId);
        pkEquipmentExchange.setEquipmentOriginalId(equipmentOriginalId);
        pkEquipmentExchange.setDiamondCount(diamondCount);
        pkEquipmentExchangeDao.insert(pkEquipmentExchange);
        return MapMessage.successMessage().add("userAdventure", userAdventure);
    }

}
