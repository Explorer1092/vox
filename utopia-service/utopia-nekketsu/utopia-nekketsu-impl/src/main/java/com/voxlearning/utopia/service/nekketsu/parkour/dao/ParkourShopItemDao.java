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
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourShopItem;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2014/8/27.
 */
@Named
@UtopiaCacheSupport(ParkourShopItem.class)
public class ParkourShopItemDao extends StaticMongoDao<ParkourShopItem, Integer> {

    @Override
    protected void calculateCacheDimensions(ParkourShopItem source, Collection<String> dimensions) {
        dimensions.add(ParkourShopItem.ck_all());
    }

    @Override
    protected void preprocessEntity(ParkourShopItem entity) {
        super.preprocessEntity(entity);
        if (entity.getCoinPrice() == null) entity.setCoinPrice(0);
    }

    public void replaceAll(List<ParkourShopItem> itemList) {
        createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .deleteMany(new BsonDocument());
        int idCount = 1;
        for (ParkourShopItem it : itemList) {
            it.setId(idCount++);
        }
        inserts(itemList);
    }

    @UtopiaCacheable(key = "ALL")
    public List<ParkourShopItem> loadWithCache() {
        return loadWithoutCache();
    }

    public List<ParkourShopItem> loadWithoutCache() {
        return __find_OTF(ReadPreference.primary()).stream()
                .sorted((o1, o2) -> Integer.compare(o1.getId(), o2.getId()))
                .collect(Collectors.toList());
    }
}
