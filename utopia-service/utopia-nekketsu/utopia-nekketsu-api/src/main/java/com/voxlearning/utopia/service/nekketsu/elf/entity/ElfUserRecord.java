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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sadi.Wan on 2015/2/10.
 * 用户关卡及植物获得情况
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-user-record")
@DocumentCollection(collection = "elf_user_record")
@EqualsAndHashCode(of = "userId")
@UseEqualsValidateCache
public class ElfUserRecord implements Serializable {
    private static final long serialVersionUID = -6106729419309849563L;

    @DocumentId
    private Long userId;

    private Map<String, ReadingTimer> readingTimers;
    /**
     * key:bookID
     */
    private Map<String, UserBookRecord> bookRecordMap;

    private Map<String, Integer> plantCounter;

    /**
     * 合成植物数(不含拯救);
     */
    private Integer composeCount;

    private Integer loginDayCount;

    private Date latestLoginTime;

    public ElfUserRecord() {

    }

    public static ElfUserRecord getDefault(long userId) {
        ElfUserRecord elfUserRecord = new ElfUserRecord();
        elfUserRecord.userId = userId;
        elfUserRecord.readingTimers = new LinkedHashMap<>();
        elfUserRecord.bookRecordMap = new LinkedHashMap<>();
        elfUserRecord.plantCounter = new LinkedHashMap<>();
        elfUserRecord.loginDayCount = 0;
        return elfUserRecord;
    }

    public int checkSavedPlant(Set<String> plantIdSet) {
        int rtn = 0;
        for (String plantId : plantIdSet) {
            if (plantCounter.containsKey(plantId)) {
                rtn++;
            }
        }
        return rtn;
    }

    public int getSunCount() {
        int rtn = 0;
        for (UserBookRecord userBookRecord : bookRecordMap.values()) {
            if (userBookRecord.isSunGained()) {
                rtn++;
            }
        }
        return rtn;
    }

    public String cacheKeyFromCondition(ElfUserRecord source) {
        return CacheKeyGenerator.generateCacheKey(ElfUserRecord.class, source.getUserId());
    }
}
