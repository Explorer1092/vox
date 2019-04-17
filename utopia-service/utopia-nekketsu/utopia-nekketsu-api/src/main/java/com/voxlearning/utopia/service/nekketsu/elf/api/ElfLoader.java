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

package com.voxlearning.utopia.service.nekketsu.elf.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.nekketsu.elf.entity.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface ElfLoader extends IPingable {
    MapMessage initInfo(long userId);

    MapMessage getLevelPlant(long userId);

    MapMessage loadLevelBookWithRecord(long userId, String levelId, boolean paidUser);

    ElfUserRecord loadUserRecord(long userId);

    ElfMyGiftList loadGiftList(long userId);

    ElfMyAchievementMap loadAchv(long userId);

    MapMessage loadAchvForDisplay(long userId);

    ElfBubble loadBubble(long userId);

}
