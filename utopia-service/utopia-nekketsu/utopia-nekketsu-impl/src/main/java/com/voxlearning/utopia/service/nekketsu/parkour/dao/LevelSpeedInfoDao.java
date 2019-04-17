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

package com.voxlearning.utopia.service.nekketsu.parkour.dao;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.LevelSpeedInfo;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */
@Named
@UtopiaCacheSupport(LevelSpeedInfo.class)
public class LevelSpeedInfoDao extends StaticMongoDao<LevelSpeedInfo, Integer> {

    @Override
    protected void calculateCacheDimensions(LevelSpeedInfo source, Collection<String> dimensions) {
        dimensions.add(LevelSpeedInfo.ck_all());
    }

    @Override
    protected void preprocessEntity(LevelSpeedInfo entity) {
        super.preprocessEntity(entity);
        if (entity.getSpeed() == null) entity.setSpeed(0);
        if (entity.getExp() == null) entity.setExp(0);
    }

    @UtopiaCacheable(key = "ALL")
    public List<LevelSpeedInfo> findAll() {
        return __find_OTF(ReadPreference.primary());
    }

    public List<LevelSpeedInfo> replaceAll(List<LevelSpeedInfo> toSave) {
        createMongoConnection().collection.withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteMany(new BsonDocument());
        inserts(toSave);
        return toSave;
    }
}
