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

import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourRegionRank;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */
@Named
@UtopiaCacheSupport(ParkourRegionRank.class)
public class ParkourRegionRankDao extends StaticMongoDao<ParkourRegionRank, String> {
    @Override
    protected void calculateCacheDimensions(ParkourRegionRank source, Collection<String> dimensions) {
        dimensions.add(ParkourRegionRank.ck_id(source.getId()));
    }

    @Override
    protected void preprocessEntity(ParkourRegionRank entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    @Override
    public String insert(ParkourRegionRank entity) {
        ensureTimestampTouched(entity);
        preprocessEntity(entity);
        ensureIdNotNull(entity);

        Bson filter = filterFromId(entity.getId());
        BsonDocument replacement = transform(entity);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndReplace(filter, replacement, options);
        ParkourRegionRank saved = transform(document);

        String key = ParkourRegionRank.ck_id(saved.getId());
        getCache().safeAdd(key, entityCacheExpirationInSeconds(), saved);

        return saved.getId();
    }

    @Override
    public void inserts(Collection<ParkourRegionRank> entities) {
        entities.forEach(this::insert);
    }

    public Map<Integer, ParkourRegionRank> getsRegionRankBatch(List<Integer> regionCodeList, int stageId) {
        if (CollectionUtils.isEmpty(regionCodeList)) {
            return Collections.emptyMap();
        }

        Set<String> ids = regionCodeList.stream()
                .map(t -> t + "_" + stageId)
                .collect(Collectors.toSet());

        return loads(ids).values().stream()
                .collect(Collectors.toMap(t -> {
                    String id = t.getId();
                    String s = StringUtils.substringBefore(id, "_");
                    return SafeConverter.toInt(s);
                }, t -> t));
    }

}
