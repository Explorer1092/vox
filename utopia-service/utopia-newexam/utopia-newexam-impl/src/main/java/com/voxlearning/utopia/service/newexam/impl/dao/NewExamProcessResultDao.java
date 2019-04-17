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

package com.voxlearning.utopia.service.newexam.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Named
@UtopiaCacheSupport(NewExamProcessResult.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class NewExamProcessResultDao extends DynamicMongoDao<NewExamProcessResult, String> {

    @Override
    protected String calculateDatabase(String template, NewExamProcessResult entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, NewExamProcessResult entity) {
        RangeableId rangeableId = RangeableId.parse(entity.getId());
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.M).toString());
    }

    @Override
    protected void calculateCacheDimensions(NewExamProcessResult source, Collection<String> dimensions) {
        dimensions.add(NewExamProcessResult.ck_id(source.getId()));
    }

    /**
     * 批改
     * @param id
     * @param score
     * @return boolean
     */
    public NewExamProcessResult correctNewExam(String id, Double score, List<Double> subScore) {

        Date currentDate = new Date();
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);

        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();
        update.set("correctScore", score);
        update.set("correctAt", currentDate);
        update.set("updateAt", currentDate);
        if (CollectionUtils.isNotEmpty(subScore)) {
            update.set("correctSubScore", subScore);
        }

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamProcessResult modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamProcessResult.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);
        }
        return modified;
    }
}
