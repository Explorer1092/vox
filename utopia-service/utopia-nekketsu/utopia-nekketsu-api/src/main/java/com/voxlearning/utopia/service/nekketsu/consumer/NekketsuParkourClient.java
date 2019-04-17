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

package com.voxlearning.utopia.service.nekketsu.consumer;

import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.nekketsu.parkour.api.NekketsuParkourService;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.*;
import com.voxlearning.utopia.service.nekketsu.parkour.net.messages.SaveGameResultRequest;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.PrizeDetailLogin;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.response.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class NekketsuParkourClient {

    @Getter
    @ImportService(interfaceClass = NekketsuParkourService.class)
    private NekketsuParkourService remoteReference;

    public MapMessage initInfo(StudentDetail studentDetail, Gender gender, boolean isPaidUser) {
        return remoteReference.initInfo(studentDetail, gender, isPaidUser);
    }

    public GetStageInfoResponse getStageInfo(StudentDetail studentDetail, int stageId) {
        return remoteReference.getStageInfo(studentDetail, stageId);
    }

    public SaveGameResultResponse saveNpcGameResult(StudentDetail studentDetail, SaveGameResultRequest req, boolean isPaidUser) {
        return remoteReference.saveNpcGameResult(studentDetail, req, isPaidUser);
    }

    public SaveGameResultResponse saveLostOrNonNpcGameResult(StudentDetail studentDetail, SaveGameResultRequest req, boolean isPaidUser) {
        return remoteReference.saveLostOrNonNpcGameResult(studentDetail, req, isPaidUser);
    }

    public GetLoginRewardListResponse getLoginRewardList(long userId, Gender gender) {
        return remoteReference.getLoginRewardList(userId, gender);
    }

    public ExchangeLoginRewardResponse exchangeLoginReward(long userId, String id, PrizeDetailLogin detail, Gender gender) {
        return remoteReference.exchangeLoginReward(userId, id, detail, gender);
    }

    public ParkourRole loadRole(long userId) {
        return remoteReference.loadRole(userId);
    }

    public List<LevelSpeedInfo> saveLevelSpeed(List<LevelSpeedInfo> toSave) {
        return remoteReference.saveLevelSpeed(toSave);
    }

    public List<LevelSpeedInfo> loadLevelSpeed() {
        return remoteReference.loadLevelSpeed();
    }

    public LoginPrizeMonthConf saveThisMonth(LoginPrizeMonthConf conf) {
        return remoteReference.saveThisMonth(conf);
    }

    public LoginPrizeMonthConf saveNextMonth(LoginPrizeMonthConf conf) {
        return remoteReference.saveNextMonth(conf);
    }

    public Map<String, LoginPrizeMonthConf> loadLoginPrizeConf() {
        return remoteReference.loadLoginPrizeConf();
    }

    public ParkourStage saveStage(ParkourStage stage) {
        return remoteReference.saveStage(stage);
    }

    public List<ParkourStage> listAllStage() {
        return remoteReference.listAllStage();
    }

    public List<ParkourStage> replaceAll(List<ParkourStage> toSave) {
        return remoteReference.replaceAll(toSave);
    }

    public List<ParkourShopItem> loadShopItemCrm() {
        return remoteReference.loadShopItemCrm();
    }

    public List<ParkourShopItem> loadShopItemWithCache() {
        return remoteReference.loadShopItemWithCache();
    }

    public void saveShopItem(List<ParkourShopItem> shopItem) {
        remoteReference.saveShopItem(shopItem);
    }

    public ParkourRole modifyCoin(long userId, int add, String detail) {
        return remoteReference.modifyCoin(userId, add, detail);
    }

    public GetAllStagePuzzleListResponse getAllStagePuzzleList(long userId) {
        return remoteReference.getAllStagePuzzleList(userId);
    }
}
