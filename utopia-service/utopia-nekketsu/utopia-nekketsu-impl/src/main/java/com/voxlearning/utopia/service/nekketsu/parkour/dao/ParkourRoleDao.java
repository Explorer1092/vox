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
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourRole;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */
@Named
@UtopiaCacheSupport(ParkourRole.class)
public class ParkourRoleDao extends StaticMongoDao<ParkourRole, Long> {

    @Override
    protected void calculateCacheDimensions(ParkourRole source, Collection<String> dimensions) {
        dimensions.add(ParkourRole.ck_id(source.getRoleId()));
    }

    @Override
    protected void preprocessEntity(ParkourRole entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    @Override
    public ParkourRole load(Long id) {
        if (id == null) {
            return null;
        }
        Date current = new Date();
        ParkourRole role = new ParkourRole();
        role.setRoleId(id);
        role.setCreateTime(current);
        role.setUpdateTime(current);
        role.setOpenStage(1);
        role.setLevel(1);
        role.setExp(1);
        role.initializeIfNecessary();
        return loadIfPresentElseInsert(id, role);
    }

    // FIXME: =================================================================
    // FIXME: 牺牲维护性带来的灵活性不要也罢
    // FIXME: =================================================================

    public ParkourRole updateFields(long userId, Map<String, KeyValuePair<String, ? extends Serializable>> fieldMap) {
        Update update = updateBuilder.build();
        for (Map.Entry<String, KeyValuePair<String, ? extends Serializable>> entry : fieldMap.entrySet()) {
            try {
                Method mtd;
                if ("inc".equals(entry.getValue().getKey())) {
                    mtd = Update.class.getMethod(entry.getValue().getKey(), String.class, Number.class);
                } else {
                    mtd = Update.class.getMethod(entry.getValue().getKey(), String.class, Object.class);
                }

                mtd.invoke(update, entry.getKey(), entry.getValue().getValue());
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return update(userId, update);
    }

    public ParkourRole modifyCoin(long userId, int add) {
        Filter filter = filterBuilder.where("_id").is(userId);
        if (add < 0) {
            filter.and("coinCount").gte(-add);
        }
        Update update = updateBuilder.build();
        update.inc("coinCount", add);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter.toBsonDocument(), update.toBsonDocument(), options);
        ParkourRole rtn = transform(document);
        if (null != rtn) {
            String key = ParkourRole.ck_id(userId);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    currentValue -> rtn);
        }
        return rtn;
    }

    public ParkourRole setSpDate(long userId, Date spDate) {
        Update update = updateBuilder.update("spDate", spDate);
        return update(userId, update);
    }
}
