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
import com.voxlearning.utopia.service.nekketsu.adventure.entity.BookStages;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.UserAdventure;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 沃克大冒险Loader
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 14:16
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AdventureLoader extends IPingable {

    MapMessage login(Long userId);

    MapMessage getStageUngrantGifts(Long userId);

    UserAdventure getUserAdventureByUserId(Long userId);

    public BookStages getBookStagesByUserId(Long userId);

    MapMessage getBeyondClassmates(Long userId, Long clazzId, Integer stageOrder);

    List<SystemApp> getAllSystemApps();


    MapMessage getUserStageByBookIdAndStageOrderId(Long userId, Long bookId, Integer stageOrderId);

//    MapMessage getLearningWordsReport();
}