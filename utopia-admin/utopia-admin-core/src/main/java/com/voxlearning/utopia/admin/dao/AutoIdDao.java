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

package com.voxlearning.utopia.admin.dao;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.admin.persist.entity.AutoId;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by Shuai Huan on 2014/5/8.
 */
@Named
public class AutoIdDao extends StaticMongoDao<AutoId, String> {

    @Override
    protected void calculateCacheDimensions(AutoId source, Collection<String> dimensions) {
    }

    public Long generateId(String name) {
        Objects.requireNonNull(name);

        Bson filter = filterFromId(name);
        Bson update = updateBuilder.build().inc("value", 1).toBsonDocument();

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER)
                .upsert(true);

        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter, update, options);

        AutoId inst = transform(document);
        return inst.getValue();
    }
}
