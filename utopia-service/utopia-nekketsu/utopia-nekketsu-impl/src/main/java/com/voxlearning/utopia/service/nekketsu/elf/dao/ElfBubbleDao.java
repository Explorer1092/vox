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
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfBubble;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
@UtopiaCacheSupport(ElfBubble.class)
public class ElfBubbleDao extends StaticMongoDao<ElfBubble, Long> {

    @Override
    protected void calculateCacheDimensions(ElfBubble source, Collection<String> dimensions) {
        dimensions.add(source.cacheKeyFromCondition(source));
    }

    public ElfBubble load(long userId) {
        ElfBubble fromDb = super.load(userId);
        if (null != fromDb) {
            return fromDb;
        }
        ElfBubble newInstance = ElfBubble.getDefault(userId);
        if (null != insert(newInstance)) {
            return newInstance;
        }
        return null;
    }

    public ElfBubble updateByField(long userId, String fieldName, Boolean value) {
        return updateByMap(userId, Collections.singletonMap(fieldName, value));
    }

    public ElfBubble updateByMap(long userId, Map<String, Boolean> fieldVal) {
        Update update = updateBuilder.build();
        for (Map.Entry<String, Boolean> entry : fieldVal.entrySet()) {
            update.set(entry.getKey(), entry.getValue());
        }
        return update(userId, update);
    }
}
