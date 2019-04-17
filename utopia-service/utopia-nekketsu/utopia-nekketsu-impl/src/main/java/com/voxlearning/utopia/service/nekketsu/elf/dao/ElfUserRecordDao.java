/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.elf.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfUserRecord;
import com.voxlearning.utopia.service.nekketsu.elf.entity.UserBookRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
@UtopiaCacheSupport(ElfUserRecord.class)
public class ElfUserRecordDao extends StaticMongoDao<ElfUserRecord, Long> {

    @Override
    protected void calculateCacheDimensions(ElfUserRecord source, Collection<String> dimensions) {
        dimensions.add(source.cacheKeyFromCondition(source));
    }

    public ElfUserRecord load(long userId) {
        ElfUserRecord fromDb = super.load(userId);
        if (null != fromDb) {
            return fromDb;
        }
        ElfUserRecord newInstance = ElfUserRecord.getDefault(userId);
        if (null != insert(newInstance)) {
            return newInstance;
        }
        return null;
    }

    public ElfUserRecord updateLoginTime(long userId, boolean incLoginDay) {
        Update update = updateBuilder.build();
        update.set("latestLoginTime", new Date());
        if (incLoginDay) {
            update.inc("loginDayCount", 1);
        }
        return update(userId, update);
    }

    public ElfUserRecord startFirstReading(long userId, UserBookRecord userBookRecord, Date fnTime) {
        String fieldKey = "bookRecordMap." + userBookRecord.getBookId();
        Filter filter = filterBuilder.build().orOperator(filterBuilder.build().and(fieldKey).exists(false),
                filterBuilder.build().and(fieldKey + ".sunGained").is(false));
        Filter finalFilter = filterBuilder.build().andOperator(filterBuilder.build().and("_id").is(userId), filter);
        Update update = updateBuilder.build();
        update.set(fieldKey, userBookRecord).set("readingTimers." + userBookRecord.getBookId() + ".rdingBid", userBookRecord.getBookId())
                .set("readingTimers." + userBookRecord.getBookId() + ".fnTime", fnTime);
        if (__update_OTF(new Find(finalFilter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }

    public ElfUserRecord finishFirstReading(long userId, String bookId, String plantId, Date fnTime) {
        String fieldKey = "bookRecordMap." + bookId;
        Filter filter = filterBuilder.build().and("_id").is(userId)
                .and(fieldKey + ".sunGained").is(false)
                .and("readingTimers." + bookId + ".rdingBid").is(bookId)
                .and("readingTimers." + bookId + ".fnTime").lte(fnTime);
        Update update = updateBuilder.build();
        update.set(fieldKey + ".sunGained", true)
                .set(fieldKey + ".sgTime", fnTime)
                .set("readingTimers." + bookId + ".rdingBid", "")
                .inc("plantCounter." + plantId, 1);
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }

    public ElfUserRecord startGainedReading(long userId, String bookId, Date fnTime) {
        Filter filter = filterBuilder.build().and("_id").is(userId).and("bookRecordMap." + bookId + ".sunGained").is(true);
        Update update = updateBuilder.build();
        update.set("readingTimers." + bookId + ".rdingBid", bookId)
                .set("readingTimers." + bookId + ".fnTime", fnTime);
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }

    public ElfUserRecord finishGainedReading(long userId, String bookId, Date fnTime) {
        String fieldKey = "bookRecordMap." + bookId;
        Filter filter = filterBuilder.build().and("_id").is(userId).and(fieldKey + ".sunGained").is(true)
                .and("readingTimers." + bookId + ".rdingBid").is(bookId)
                .and("readingTimers." + bookId + ".fnTime").lte(fnTime);
        Update update = updateBuilder.build();
        update.set("readingTimers." + bookId + ".rdingBid", "");
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }

    public ElfUserRecord insertSavePlant(long userId, String savePlantId, Map<String, Integer> materialMap) {
        String field1 = "plantCounter." + savePlantId;
        Filter filter = filterBuilder.build().and("_id").is(userId).and(field1).exists(false);
        Update update = updateBuilder.build().set(field1, 1);
        for (Map.Entry<String, Integer> entry : materialMap.entrySet()) {
            String field2 = "plantCounter." + entry.getKey();
            filter = filter.and(field2).gte(entry.getValue());
            update.inc(field2, -entry.getValue());
        }
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }

    public ElfUserRecord incComposePlant(long userId, String savePlantId, Map<String, Integer> materialMap) {
        String field1 = "plantCounter." + savePlantId;
        Filter filter = filterBuilder.build().and("_id").is(userId).and(field1).exists(true);
        Update update = updateBuilder.build().inc(field1, 1);
        for (Map.Entry<String, Integer> entry : materialMap.entrySet()) {
            String field2 = "plantCounter." + entry.getKey();
            filter = filter.and(field2).gte(entry.getValue());
            update.inc(field2, -entry.getValue());
        }
        update.inc("composeCount", 1);
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }
}
