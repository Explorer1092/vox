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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.nekketsu.adventure.api.AdventureService;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.List;

/**
 * 沃克冒险ServiceClient
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/25 10:39
 */
public class AdventureServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AdventureServiceClient.class);

    @Getter
    @ImportService(interfaceClass = AdventureService.class)
    private AdventureService remoteReference;

    public MapMessage createUserAdventure(Long userId, Long bookId, List<String> words, Integer classLevel) {
        try {
            return remoteReference.createUserAdventure(userId, bookId, words, classLevel);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'createUserAdventure' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage stageMisson(Long userId, Long bookId, Integer stageOrder, Integer appOrder, String fileName,
                                  Integer count, Integer classLevel, Boolean trial) {
        try {
            return remoteReference.stageMisson(userId, bookId, stageOrder, appOrder, fileName, count,
                    classLevel, trial);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'stageMisson' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage stageCrownReward(Long userId, Integer stageOrder, Boolean shared) {
        try {
            return remoteReference.stageCrownReward(userId, stageOrder, shared);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'stageCrownReward' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage openStage(Long userId, Integer stageOrder, Boolean shared) {
        try {
            return remoteReference.openStage(userId, stageOrder, shared);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'openStage' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage changeBook(Long userId, Long bookId, List<String> words, Integer classLevel) {
        try {
            return remoteReference.changeBook(userId, bookId, words, classLevel);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'changeBook' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage grantGift(String id) {
        try {
            return remoteReference.grantGift(id);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'grantGift' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage grantAchievement(Long userId, AchievementType achievementType) {
        try {
            return remoteReference.grantAchievement(userId, achievementType);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'grantAchievement' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage addStageWords(Long userId, Long bookId, Integer stageOrder, List<String> words) {
        try {
            return remoteReference.addStageWords(userId, bookId, stageOrder, words);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'addStageWords' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage openNextStageGroup(Long userId) {
        try {
            return remoteReference.openNextStageGroup(userId);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'openNextStageGroup' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage deleteSystemApp(Long id) {
        try {
            return remoteReference.deleteSystemApp(id);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'deleteSystemApp' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage addSystemApp(SystemApp systemApp) {
        try {
            return remoteReference.addSystemApp(systemApp);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'addSystemApps' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage changeSystemAppValid(Long id) {
        try {
            return remoteReference.changeSystemAppValid(id);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'changeSystemAppValid' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage exchangePkEquipment(Long userId, Integer diamondCount, String equipmentOriginalId) {
        try {
            return remoteReference.exchangePkEquipment(userId, diamondCount, equipmentOriginalId);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'exchangePkEquipment' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage receiveFreeBeans(Long userId, Long bookId, Integer stageOrderId) {
        try {
            return remoteReference.receiveFreeBeans(userId, bookId, stageOrderId);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'receiveFreeBeans' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

}
