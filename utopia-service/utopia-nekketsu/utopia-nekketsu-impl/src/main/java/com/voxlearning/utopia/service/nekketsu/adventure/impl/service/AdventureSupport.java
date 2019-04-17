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

package com.voxlearning.utopia.service.nekketsu.adventure.impl.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.ListJockey;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.lang.util.Unique;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AdventureConstants;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.StageAppType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.SystemAppCategory;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.*;
import com.voxlearning.utopia.service.nekketsu.adventure.impl.dao.*;
import com.voxlearning.utopia.service.nekketsu.base.cache.NekketsuCacheSystem;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 16:22
 */
public abstract class AdventureSupport extends SpringContainerSupport {

    @Inject protected NekketsuCacheSystem nekketsuCacheSystem;
    @Inject protected UserAdventureDao userAdventureDao;
    @Inject protected BookStagesDao bookStagesDao;
    @Inject protected GiftDao giftDao;
    protected CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
    @Inject protected SystemAppDao systemAppDao;
    @Inject protected PkEquipmentExchangeDao pkEquipmentExchangeDao;
    @Inject protected AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected PracticeLoaderClient practiceLoaderClient;
    @Inject protected AppMessageServiceClient appMessageServiceClient;
    @Inject protected ParentLoaderClient parentLoaderClient;
    @Inject protected WordStockLoaderClient wordStockLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    protected void setUserImage(UserAdventure userAdventure) {
        User user = userLoaderClient.loadUser(userAdventure.getId());
        userAdventure.setImage(getUserImageUrl(user.fetchImageUrl()));
    }

    protected BookStages initBookStages(Long userId, Long bookId, List<String> words, Integer classLevel) {
        Map<Integer, StageApp> map = randomStageApps(classLevel);
        if (null == map || 0 == map.size()) {
            return null;
        }
        int totalDiamond = map.size() * 3;
        Stage stage = Stage.newInstance(1, words, map, totalDiamond);

        Map<Integer, Stage> stageMap = new LinkedHashMap<>();
        stageMap.put(stage.getOrder(), stage);
        BookStages bookStages = new BookStages();
        Date current = new Date();
        bookStages.setCreateTime(current);
        bookStages.setUpdateTime(current);
        bookStages.setId(BookStages.generateId(userId, bookId));
        bookStages.setBookId(bookId);
        bookStages.setCurrentStage(1);
        bookStages.setOpenedStage(AdventureConstants.DEFAULT_OPEN_STAGE);
        bookStages.setUserId(userId);
        bookStages.setStages(stageMap);
        return bookStages;
    }

    protected List<BeyondClassmate> findBeyondClassmates(Long userId, Long clazzId, int currentUserStageOrder) {
        List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);

        Map<Long, UserAdventure> userAdventureMap = userAdventureDao.loads(userMap.keySet());
        Map<String, BookStages> bookStagesMap = bookStagesDao.loads(UserAdventure.bookStagesIdList(new LinkedList<>(userAdventureMap.values())));
        bookStagesMap.remove(userAdventureMap.get(userId).getBookStagesId());
        UserAdventure tempUserAdventure = userAdventureMap.get(userId);
        List<BeyondClassmate> beyondClassmateList = new ArrayList<>();
        for (Map.Entry<String, BookStages> entry : bookStagesMap.entrySet()) {
            if (Objects.equals(entry.getValue().getBookId(), tempUserAdventure.getBookId()) &&
                    entry.getValue().getCurrentStage() < currentUserStageOrder) {
                User user = userMap.get(entry.getValue().getUserId());
                if (beyondClassmateList.size() < 3) {
                    if (Objects.equals(user.getId(), userId)) {
                        continue;
                    }
                    String img = getUserImageUrl(user.fetchImageUrl());
                    beyondClassmateList.add(BeyondClassmate.newInstance(user.getId(), user.getProfile().getRealname(), img));
                } else {
                    break;
                }
            }
        }
        return beyondClassmateList;
    }

    protected Map<Integer, StageApp> randomStageApps(Integer classLevel) {
        boolean withSpelling = true;
//        if (4 <= classLevel && classLevel <= 6) {
//            withSpelling = true;
//        }
        Map<Integer, StageApp> apps = new LinkedHashMap<>();


        List<SystemApp> systemAppList = systemAppDao.findAllSystemApps().stream()
                .filter(t -> SafeConverter.toBoolean(t.getValid()))
                .collect(Collectors.toList());

        ListJockey jockey = ListJockey.newInstance(SystemApp.class, systemAppList);
        jockey.key("category").group();

        SystemApp[] appArray = new SystemApp[1];
        boolean flag = false;
        for (Iterator<Unique> ite = jockey.uniqueSet().iterator(); ite.hasNext(); ) {
            Unique unique = ite.next();
            if (unique.toString().contains(SystemAppCategory.RECOGNITION.name())) {
                RandomUtils.randomPickFew(jockey.find(unique).getSources(), 1, appArray);
                StageApp stageApp = StageApp.newInstance(appArray[0].getId().intValue(), 1, appArray[0].getFileName(),
                        appArray[0].getPracticeName(), StageAppType.BASE, appArray[0].getSize());
                stageApp.setOpen(true);
                apps.put(1, stageApp);
            } else if (unique.toString().contains(SystemAppCategory.LISTENING.name())) {
                RandomUtils.randomPickFew(jockey.find(unique).getSources(), 1, appArray);
                apps.put(2, StageApp.newInstance(appArray[0].getId().intValue(), 2, appArray[0].getFileName(),
                        appArray[0].getPracticeName(), StageAppType.BASE, appArray[0].getSize()));
            } else if (unique.toString().contains(SystemAppCategory.FIGURE.name())) {
                RandomUtils.randomPickFew(jockey.find(unique).getSources(), 1, appArray);
                apps.put(3, StageApp.newInstance(appArray[0].getId().intValue(), 3, appArray[0].getFileName(),
                        appArray[0].getPracticeName(), StageAppType.BASE, appArray[0].getSize()));
            } else if (withSpelling && unique.toString().contains(SystemAppCategory.SPELLING.name())) {
                RandomUtils.randomPickFew(jockey.find(unique).getSources(), 1, appArray);
                apps.put(4, StageApp.newInstance(appArray[0].getId().intValue(), 4, appArray[0].getFileName(),
                        appArray[0].getPracticeName(), StageAppType.BASE, appArray[0].getSize()));
                flag = true;
            } else if (unique.toString().contains(SystemAppCategory.REPEAT.name())) {
                RandomUtils.randomPickFew(jockey.find(unique).getSources(), 1, appArray);
                if (flag) {
                    apps.put(5, StageApp.newInstance(appArray[0].getId().intValue(), 5, appArray[0].getFileName(),
                            appArray[0].getPracticeName(), StageAppType.EXTENTION, appArray[0].getSize()));
                } else {
                    apps.put(4, StageApp.newInstance(appArray[0].getId().intValue(), 4, appArray[0].getFileName(),
                            appArray[0].getPracticeName(), StageAppType.EXTENTION, appArray[0].getSize()));
                }
            }
        }

        List<Integer> list = new LinkedList<>(apps.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        Map<Integer, StageApp> newApps = new LinkedHashMap<>();
        for (Integer order : list) {
            newApps.put(order, apps.get(order));
        }
        return newApps;
    }

    protected void setNewFlag(Long userId, MapMessage response) {
        response.add("newGift", nekketsuCacheSystem.CBS.flushable.load(//读取新礼物标识的缓存
                AdventureConstants.NEW_GIFT_CACHE_KEY_PREFIX + userId));
        response.add("newAchievement", nekketsuCacheSystem.CBS.flushable.load(//读取新成就标识的缓存
                AdventureConstants.NEW_ACHIEVEMENT_CACHE_KEY_PREFIX + userId));
    }

    private String getUserImageUrl(String userImageUrl) {
        if (StringUtils.isBlank(userImageUrl)) {
            return commonConfiguration.getDefaultImageUrl();
        } else {
            return CdnConfig.getAvatarDomain().getValue() + "/gridfs/" + userImageUrl;
        }
    }
}
