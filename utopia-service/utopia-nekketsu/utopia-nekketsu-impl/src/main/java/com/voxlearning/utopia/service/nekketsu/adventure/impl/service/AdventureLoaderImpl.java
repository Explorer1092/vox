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
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.nekketsu.adventure.api.AdventureLoader;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AdventureConstants;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.GiftType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.*;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 沃克大冒险LoaderImpl
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 14:18
 */
@Named
@Service(interfaceClass = AdventureLoader.class)
@ExposeService(interfaceClass = AdventureLoader.class)
public class AdventureLoaderImpl extends AdventureSupport implements AdventureLoader {

    @Inject UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public MapMessage login(Long userId) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        if (null == userAdventure) {
            return MapMessage.errorMessage().setInfo("用户从未开启过大冒险。");
        }
        setUserImage(userAdventure);
        BookStages bookStages = bookStagesDao.load(userAdventure.getBookStagesId());
        if (null == bookStages) {
            return MapMessage.errorMessage().setErrorCode("101015").setInfo("没有找到教材与关卡对应信息。");
        }

        Boolean login = nekketsuCacheSystem.CBS.flushable.load(AdventureConstants.LOGIN_CACHE_KEY_PREFIX + userId);
        if (null == login || !login) {//登录成就，每天一次
            nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.LOGIN_CACHE_KEY_PREFIX + userId, DateUtils.getCurrentToDayEndSecond(), true);
            Achievement achievement = userAdventure.getAchievements().get(AchievementType.LOGIN);
            achievement.increaseTotalCount(1);
            achievement.calculateNextLevelCount();
            if (achievement.canReceive()) {
                //增加新成就标识的缓存
                nekketsuCacheSystem.CBS.flushable.add(AdventureConstants.NEW_ACHIEVEMENT_CACHE_KEY_PREFIX + userId, DateUtils.getCurrentToDayEndSecond(), true);
            }
            userAdventureDao.updateAchievement(userId, achievement);
        }


        MapMessage response = MapMessage.successMessage().add("userAdventure", userAdventure).add("bookStages", bookStages);
        boolean receiveFbSwitch = receiveFbSwitch(userId);
        response.add("receiveFbSwitch", receiveFbSwitch);
        setNewFlag(userId, response);
        return response;
    }

    /**
     * 判断此用户是否展示领学豆消息,展示条件：
     * 1、从未支付购买过套餐
     * 2、展示套餐开关是开启的
     *
     * @param userId
     * @return
     */
    private boolean receiveFbSwitch(Long userId) {
        User user = userLoaderClient.loadUser(userId);
        if (user.isStudent()) {
            List<UserOrder> list = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.Walker.name(), userId);
            return CollectionUtils.isEmpty(list);
        }
        return false;
    }

    @Override
    public MapMessage getStageUngrantGifts(Long userId) {
        List<Gift> list = giftDao.getUngrantGifts(userId, GiftType.STAGE);
        nekketsuCacheSystem.CBS.flushable.delete(AdventureConstants.NEW_GIFT_CACHE_KEY_PREFIX + userId);//删除新礼物标识的缓存
        return MapMessage.successMessage().add("gifts", list);
    }

    public UserAdventure getUserAdventureByUserId(Long userId) {
        return userAdventureDao.load(userId);
    }

    public BookStages getBookStagesByUserId(Long userId) {
        UserAdventure userAdventure = userAdventureDao.load(userId);
        if (null == userAdventure) {
            return null;
        }
        return bookStagesDao.load(userAdventure.getBookStagesId());
    }


    public MapMessage getUserStageByBookIdAndStageOrderId(Long userId, Long bookId, Integer stageOrderId) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            logger.error("getUserStageByBookIdAndStageOrderId faild: userId={},bookId={},stageOrderId={}", userId, bookId, stageOrderId);
            return MapMessage.errorMessage().setInfo("数据不全，用户信息不存在");
        }
        BookStages bookStages = bookStagesDao.load(BookStages.generateId(userId, bookId));
        if (bookStages != null) {
            Stage stage = bookStages.getStages().get(stageOrderId);
            if (stage != null) {
                List<String> words = stage.getWords();
                Map<String, String> wordsMap = new HashMap<>();
                for (String wordMid : words) {
                    String cols[] = wordMid.split("#");
                    if (cols.length != 2) {
                        continue;
                    }
                    List<WordStock> list = wordStockLoaderClient.loadWordStocksByEntext(cols[1]);
                    if (list.size() > 0) {
                        wordsMap.put(cols[1], list.get(0).getCnText());
                    } else {
                        wordsMap.put(cols[1], "查询失败");
                    }

                }

                MapMessage message = MapMessage.successMessage().set("words", wordsMap).set("wordNum", words.size());
                message.set("stuName", user.getProfile().getRealname());

                //计算学生剩余多少试用关卡
                UserAdventure userAdventure = getUserAdventureByUserId(userId);
                if (userAdventure == null) {
                    return MapMessage.errorMessage().setInfo("数据不全，用户闯关信息不完整");
                }
                int surplusStageNum = AdventureConstants.TRIAL_COUNT - userAdventure.getTrialCount();
                message.set("surplusStageNum", surplusStageNum);
                return message;
            } else {
                logger.error("getUserStageByBookIdAndStageOrderId faild: Stage is null ,userId={},bookId={},stageOrder={}", userId, bookId, stageOrderId);
                return MapMessage.errorMessage().setInfo("关卡数据为空");
            }
        } else {
            logger.error("getUserStageByBookIdAndStageOrderId faild: BookStage is null ,userId={},bookId={},stageOrder={}", userId, bookId, stageOrderId);
            return MapMessage.errorMessage().setInfo("关卡绑定数据为空");
        }

    }


    public MapMessage getBeyondClassmates(Long userId, Long clazzId, Integer stageOrder) {
        List<BeyondClassmate> beyondClassmates = findBeyondClassmates(userId, clazzId, stageOrder);
        MapMessage response = MapMessage.successMessage().add("beyondClassmates", beyondClassmates);
        setNewFlag(userId, response);
        return response;
    }


    /**
     * ***********************for crm***************************
     */
    public List<SystemApp> getAllSystemApps() {
        return systemAppDao.findAllSystemApps();
    }

    public PracticeType getPracticeTypeEntityById(Long id) {
        return practiceLoaderClient.loadPractice(id);
    }
}
