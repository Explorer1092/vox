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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkPrize;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;

@Named
@UtopiaCacheSupport(NewHomeworkPrize.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class NewHomeworkPrizeDao extends AlpsDynamicMongoDao<NewHomeworkPrize, String> {
    @Override
    protected String calculateDatabase(String template, NewHomeworkPrize entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, NewHomeworkPrize entity) {
        NewHomeworkPrize.ID id = entity.parseID();
        return StringUtils.formatMessage(template, id.getDay());
    }

    @Override
    protected void calculateCacheDimensions(NewHomeworkPrize source, Collection<String> dimensions) {
        dimensions.add(NewHomeworkPrize.generateCacheKey(source.getId()));
    }


    public void bingo(NewHomeworkPrize.ID id, Long studentId, String where, Integer what, Date when) {
        if (id == null || studentId == null || what == null || what <= 0 || when == null) return;

        Criteria criteria = Criteria.where("_id").is(id.toString());
        Update update = new Update();
        update.set("details." + studentId.toString() + ".when", when);
        update.set("details." + studentId.toString() + ".where", where);
        update.set("details." + studentId.toString() + ".what", what);

        UpdateOptions options = new UpdateOptions().upsert(true);
        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        UpdateResult result = updateOne(createMongoConnection(namespace), criteria, update, options);
        if(result.getUpsertedId() != null){
            String ck = NewHomeworkPrize.generateCacheKey(id.toString());
            getCache().delete(ck);
        }else if(result.getModifiedCount() > 0){
            getCache().createCacheValueModifier()
                    .key(NewHomeworkPrize.generateCacheKey(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> {
                        if(!(currentValue instanceof NewHomeworkPrize)){
                            throw new UnsupportedOperationException();
                        }
                        NewHomeworkPrize newHomeworkPrize = (NewHomeworkPrize) currentValue;
                        if(newHomeworkPrize.getDetails() == null){
                            newHomeworkPrize.setDetails(new LinkedHashMap<>());
                        }
                        NewHomeworkPrize.Detail detail = newHomeworkPrize.getDetails()
                                .computeIfAbsent(studentId.toString(), k -> new NewHomeworkPrize.Detail());
                        detail.setWhat(what);
                        detail.setWhen(when);
                        detail.setWhere(where);
                        return newHomeworkPrize;
                    }).execute();
        }
    }
}
