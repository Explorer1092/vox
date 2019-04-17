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

package com.voxlearning.utopia.service.nekketsu.parkour.api;

import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.*;
import com.voxlearning.utopia.service.nekketsu.parkour.net.messages.SaveGameResultRequest;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.PrizeDetailLogin;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sadi.Wan on 2014/8/15.
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface NekketsuParkourService extends IPingable {

    MapMessage initInfo(StudentDetail studentDetail, Gender gender, boolean isPaidUser);

    GetStageInfoResponse getStageInfo(StudentDetail studentDetail, int stageId);

    SaveGameResultResponse saveNpcGameResult(StudentDetail studentDetail, SaveGameResultRequest req, boolean isPaidUser);

    SaveGameResultResponse saveLostOrNonNpcGameResult(StudentDetail studentDetail, SaveGameResultRequest req, boolean isPaidUser);

    GetLoginRewardListResponse getLoginRewardList(long userId, Gender gender);

    ExchangeLoginRewardResponse exchangeLoginReward(long userId, String id, PrizeDetailLogin detail, Gender gender);

    ParkourRole loadRole(long userId);

    /**
     * 每级对应的经验，配速
     *
     * @param toSave
     * @return
     */
    List<LevelSpeedInfo> saveLevelSpeed(List<LevelSpeedInfo> toSave);

    List<LevelSpeedInfo> loadLevelSpeed();

    /**
     * 存储每月登录累进奖励
     *
     * @param conf
     * @return
     */
    LoginPrizeMonthConf saveThisMonth(LoginPrizeMonthConf conf);

    LoginPrizeMonthConf saveNextMonth(LoginPrizeMonthConf conf);

    Map<String, LoginPrizeMonthConf> loadLoginPrizeConf();

    ParkourStage saveStage(ParkourStage stage);

    List<ParkourStage> listAllStage();

    List<ParkourStage> replaceAll(List<ParkourStage> toSave);

    List<ParkourShopItem> loadShopItemCrm();

    List<ParkourShopItem> loadShopItemWithCache();

    void saveShopItem(List<ParkourShopItem> shopItem);

    ParkourRole modifyCoin(long userId, int add, String detail);

    GetAllStagePuzzleListResponse getAllStagePuzzleList(long userId);

    boolean setUserSpValid(long userId);

    Map<Integer, List<String>> getStageWordMap(List<Integer> stageIdList);
}
