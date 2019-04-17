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
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfAchievementType;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfMyAchievementMap;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
@UtopiaCacheSupport(ElfMyAchievementMap.class)
public class ElfAchvDao extends StaticMongoDao<ElfMyAchievementMap, Long> {

    @Override
    protected void calculateCacheDimensions(ElfMyAchievementMap source, Collection<String> dimensions) {
        dimensions.add(source.cacheKeyFromCondition(source));
    }

    public ElfMyAchievementMap load(long userId) {
        ElfMyAchievementMap fromDb = super.load(userId);
        if (null != fromDb) {
            return fromDb;
        }
        ElfMyAchievementMap newInstance = ElfMyAchievementMap.getDefault(userId);
        if (null != insert(newInstance)) {
            return newInstance;
        }
        return null;
    }

    public ElfMyAchievementMap batchSetExchangable(long userId, Map<ElfAchievementType, Integer> updateMap) {
//        Filter filter = filterBuilder.build().and("_id").is(userId);
//        Update update = updateBuilder.build();
        ElfMyAchievementMap ret = null;
        for (Map.Entry<ElfAchievementType, Integer> entry : updateMap.entrySet()) {
//            filter = filter.and("achievementMap." + entry.getKey().name() + ".stage").is(entry.getValue());
//            update.set("achievementMap." + entry.getKey().name() + ".exchangable", true);
            ret = setExchangable(userId, entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public ElfMyAchievementMap setExchangable(long userId, ElfAchievementType elfAchievementType, int stage) {
        Filter filter = filterBuilder.build().and("_id").is(userId);
        Update update = updateBuilder.build();
        filter = filter.and("achievementMap." + elfAchievementType.name() + ".stage").is(stage);
        update.set("achievementMap." + elfAchievementType.name() + ".exchangable", true);
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }


    public ElfMyAchievementMap setExchanged(long userId, ElfAchievementType elfAchievementType, int curStage, int nextStage, boolean nextStageExchangable) {
        Filter filter = filterBuilder.build().and("_id").is(userId);
        Update update = updateBuilder.build();
        String field1 = "achievementMap." + elfAchievementType.name() + ".exchangable";
        String field2 = "achievementMap." + elfAchievementType.name() + ".stage";
        filter = filter.and(field1).is(true).and(field2).is(curStage);

        update.set(field1, nextStageExchangable)
                .set(field2, nextStage);
        if (__update_OTF(new Find(filter), update) > 0) {
            getCache().delete(cacheKeyFromId(userId));
            return __load_OTF(userId);
        }
        return null;
    }
}
