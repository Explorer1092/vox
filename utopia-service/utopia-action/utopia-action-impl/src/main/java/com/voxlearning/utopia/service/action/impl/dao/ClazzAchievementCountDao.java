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

package com.voxlearning.utopia.service.action.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.action.api.document.ClazzAchievementCount;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import org.bson.BsonDocument;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.action.impl.dao.ClazzAchievementCountDao")
public class ClazzAchievementCountDao extends StaticCacheDimensionDocumentMongoDao<ClazzAchievementCount, String> {

    public ClazzAchievementCount increase(Long clazzId, AchievementType type, Integer level) {
        String id = clazzId + "-" + type.name() + "-" + level;
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().inc("count", 1);
        BsonDocument f = criteriaTranslator.translate(criteria);
        BsonDocument u = updateTranslator.translate(update);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        BsonDocument modified = createMongoConnection().collection.findOneAndUpdate(f, u, options);
        return convertBsonDocument(modified);
    }
}
