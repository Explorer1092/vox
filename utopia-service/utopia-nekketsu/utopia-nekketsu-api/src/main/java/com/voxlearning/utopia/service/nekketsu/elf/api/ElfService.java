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

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ElfService extends IPingable {

    MapMessage finishAnimate(long userId);

    MapMessage unsetLevelTabNew(long userId, String levelId);

    MapMessage startReading(long userId, String bookId, final Set<String> paidLevels);

    MapMessage finishFirstReading(long userId, ElfUserRecord elfUserRecord, String bookId, Date fnTime);

    MapMessage finishGainedReading(long userId, ElfUserRecord elfUserRecord, String bookId, Date fnTime);

    MapMessage savePlant(long userId, String levelId, String plantId);

    MapMessage composePlant(long userId, String levelId, String plantId);

    MapMessage unsetAchvBubble(long userId);

    MapMessage unsetGiftBubble(long userId);

    MapMessage exchangeGift(long userId, ElfMyGift gift);

    MapMessage exchangeAchv(long userId, ElfAchievementType elfAchievementType, ElfMyAchievementMap elfMyAchievementMap);

    MapMessage importPlantDef(Collection<ElfPlantDef> plantDefs);

    MapMessage importLevel(Collection<ElfLevelDef> levelDefs);

    MapMessage importBookDef(Collection<ElfBookDef> bookDefs);
}
