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

package com.voxlearning.utopia.service.nekketsu.elf.entity;

import com.voxlearning.alps.annotation.cache.UseEqualsValidateCache;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/26.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-achv")
@DocumentCollection(collection = "elf_achv")
@EqualsAndHashCode(of = "userId")
@UseEqualsValidateCache
public class ElfMyAchievementMap implements Serializable {
    private static final long serialVersionUID = 9012926290563745087L;
    @DocumentId
    private Long userId;
    private Map<ElfAchievementType, ElfMyAchievement> achievementMap;

    public ElfMyAchievementMap() {

    }

    public static ElfMyAchievementMap getDefault(long userId) {
        ElfMyAchievementMap elfMyAchievementMap = new ElfMyAchievementMap();
        elfMyAchievementMap.userId = userId;
        elfMyAchievementMap.achievementMap = new LinkedHashMap<>();
        for (ElfAchievementType elfAchievementType : ElfAchievementType.values()) {
            ElfMyAchievement elfMyAchievement = new ElfMyAchievement();
            elfMyAchievement.setAchievementType(elfAchievementType);
            elfMyAchievement.setExchangable(false);
            elfMyAchievement.setStage(0);
            elfMyAchievementMap.achievementMap.put(elfAchievementType, elfMyAchievement);
        }
        return elfMyAchievementMap;
    }

    public String cacheKeyFromCondition(ElfMyAchievementMap source) {
        return CacheKeyGenerator.generateCacheKey(ElfMyAchievementMap.class, source.getUserId());
    }
}
