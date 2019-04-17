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
import com.voxlearning.utopia.service.nekketsu.adventure.api.AdventureLoader;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.BookStages;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.UserAdventure;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.List;

/**
 * 沃克冒险LoaderClient
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/25 10:39
 */
public class AdventureLoaderClient {
    private static final Logger logger = LoggerFactory.getLogger(AdventureLoaderClient.class);

    @Getter
    @ImportService(interfaceClass = AdventureLoader.class)
    private AdventureLoader remoteReference;

    public MapMessage login(Long userId) {
        try {
            return remoteReference.login(userId);
        } catch (Exception e) {
            logger.error("Adventure login error.", e);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage getStageUngrantGifts(Long userId) {
        try {
            return remoteReference.getStageUngrantGifts(userId);
        } catch (Exception e) {
            logger.error("Adventure getStagetUngrantGiftDetails error.", e);
            return MapMessage.errorMessage();
        }
    }

    public BookStages getBookStagesByUserId(Long userId) {
        try {
            return remoteReference.getBookStagesByUserId(userId);
        } catch (Exception e) {
            logger.error("Adventure getBookStagesByUserId error.", e);
            return null;
        }
    }

    public UserAdventure getUserAdventureByUserId(Long userId) {
        try {
            return remoteReference.getUserAdventureByUserId(userId);
        } catch (Exception e) {
            logger.error("Adventure getUserAdventureByUserId error.", e);
            return null;
        }
    }


    public MapMessage getBeyondClassmates(Long userId, Long clazzId, Integer stageOrder) {
        try {
            return remoteReference.getBeyondClassmates(userId, clazzId, stageOrder);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'getBeyondClassmates' invoke error", e);
            return MapMessage.errorMessage();
        }
    }

    public List<SystemApp> getAllSystemApps() {
        try {
            return remoteReference.getAllSystemApps();
        } catch (Exception e) {
            logger.error("AdventureService remote method 'getAllSystemApps' invoke error", e);
            return null;
        }
    }

    public MapMessage getUserStageByBookIdAndStageOrderId(Long userId, Long bookId, Integer stageOrderId) {
        try {
            return remoteReference.getUserStageByBookIdAndStageOrderId(userId, bookId, stageOrderId);
        } catch (Exception e) {
            logger.error("AdventureService remote method 'getAllSystemApps' invoke error", e);
            return null;
        }
    }
}
