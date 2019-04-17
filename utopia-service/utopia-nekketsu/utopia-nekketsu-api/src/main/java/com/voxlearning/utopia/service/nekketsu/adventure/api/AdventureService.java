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

package com.voxlearning.utopia.service.nekketsu.adventure.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 沃克大冒险Service
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 14:16
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AdventureService extends IPingable {

    MapMessage createUserAdventure(Long userId, Long bookId, List<String> words, Integer clazzLevel);

    MapMessage stageMisson(Long userId, Long bookId, Integer stageOrder, Integer appOrder, String fileName,
                           Integer count, Integer classLevel, Boolean trial);

    MapMessage stageCrownReward(Long userId, Integer stageOrder, Boolean shared);

    MapMessage openStage(Long userId, Integer stageOrder, Boolean shared);

    MapMessage changeBook(Long userId, Long bookId, List<String> words, Integer clazzLevel);

    MapMessage grantGift(String id);

    MapMessage grantAchievement(Long userId, AchievementType achievementType);

    MapMessage addStageWords(Long userId, Long bookId, Integer stageOrder, List<String> words);

    MapMessage openNextStageGroup(Long userId);

    MapMessage addSystemApp(SystemApp systemApp);

    MapMessage deleteSystemApp(Long id);

    MapMessage changeSystemAppValid(Long id);

    MapMessage exchangePkEquipment(Long userId, Integer diamondCount, String equipmentOriginalId);

    MapMessage receiveFreeBeans(Long userId, Long bookId, Integer stageOrderId);

}
