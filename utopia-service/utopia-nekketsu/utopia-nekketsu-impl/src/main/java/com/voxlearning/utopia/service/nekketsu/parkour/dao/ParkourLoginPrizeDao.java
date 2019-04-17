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

package com.voxlearning.utopia.service.nekketsu.parkour.dao;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.LoginPrizeDetail;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourLoginPrize;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2014/8/27.
 */
@Named
@UtopiaCacheSupport(ParkourLoginPrize.class)
public class ParkourLoginPrizeDao extends StaticMongoDao<ParkourLoginPrize, String> {

    @Override
    protected void calculateCacheDimensions(ParkourLoginPrize source, Collection<String> dimensions) {
        dimensions.add(ParkourLoginPrize.ck_id(source.getId()));
    }

    @Override
    protected void preprocessEntity(ParkourLoginPrize entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    public List<ParkourLoginPrize> findLastMonths(long userId, int months) {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < months; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1 * i);
            String id = userId + "_" + FastDateFormat.getInstance("yyyy_MM").format(cal.getTime());
            ids.add(id);
        }
        return loads(ids).values().stream().sorted().collect(Collectors.toList());
    }

    public ParkourLoginPrize findAndInsertThisMonthPrizeOnNonExist(long userId) {
        Calendar cal = Calendar.getInstance();
        String id = userId + "_" + FastDateFormat.getInstance("yyyy_MM").format(cal.getTime());

        ParkourLoginPrize neonatal = new ParkourLoginPrize();
        neonatal.setId(id);
        neonatal.setCreateTime(cal.getTime());
        neonatal.setUpdateTime(cal.getTime());
        neonatal.setUserId(userId);
        neonatal.setLatestLoginDay(0);
        neonatal.setPrizeDetailList(new LinkedList<>());

        return loadIfPresentElseInsert(id, neonatal);
    }

    public ParkourLoginPrize pushPrize(String id, LoginPrizeDetail loginPrizeDetail) {
        Calendar cal = Calendar.getInstance();

        Filter filter = filterBuilder.where("_id").is(id)
                .and("latestLoginDay").lt(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        Update update = updateBuilder.build().addToSet("prizeDetailList", loginPrizeDetail);
        update.set("latestLoginDay", cal.get(Calendar.DAY_OF_MONTH));

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter.toBsonDocument(), update.toBsonDocument(), options);

        ParkourLoginPrize rtn = transform(document);
        if (rtn != null) {
            String key = ParkourLoginPrize.ck_id(id);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    currentValue -> rtn);
        }
        return rtn;
    }

    public ParkourLoginPrize setPrizeExchanged(String id, LoginPrizeDetail loginPrizeDetail) {
        Filter filter = filterBuilder.where("_id").is(id)
                .and("prizeDetailList").is(loginPrizeDetail);
        Update update = updateBuilder.build().set("prizeDetailList.$.exchanged", true);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter.toBsonDocument(), update.toBsonDocument(), options);

        ParkourLoginPrize rtn = transform(document);
        if (rtn != null) {
            String key = ParkourLoginPrize.ck_id(id);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    currentValue -> rtn);
        }
        return rtn;
    }

    public ParkourLoginPrize updateLatestLoginDay(String id) {
        Calendar cal = Calendar.getInstance();
        Update update = updateBuilder.build().set("latestLoginDay", cal.get(Calendar.DAY_OF_MONTH));
        return update(id, update);
    }
}
