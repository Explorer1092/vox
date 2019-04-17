/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineListenPaper;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
@UtopiaCacheSupport(OfflineListenPaper.class)
public class OfflineListenPaperDao extends StaticMongoDao<OfflineListenPaper, String> {
    @Override
    protected void calculateCacheDimensions(OfflineListenPaper offlineListenPaper, Collection<String> collection) {
        collection.add(OfflineListenPaper.cacheKeyFromId(offlineListenPaper.getId()));
    }

    public void updateFileIdsById(String id, final List<String> fileIds, final String title, final Integer totalTime, final String comment, Long userId) {
        if(StringUtils.isBlank(id) || CollectionUtils.isEmpty(fileIds) || userId == null){
            logger.warn("set fileIds value is empty,paperId:{},fileIds:{},userId:{},ingore update operate",id,fileIds,userId);
            return;
        }
        if(StringUtils.isBlank(title)){
            logger.warn("set title value is empty,paperId:{},title:{},ingore update operate",id,title);
            return;
        }
        if(SafeConverter.toInt(totalTime) <= 0){
            logger.warn("set totalTime value is empty,paperId:{},totalTime:{},ingore update operate",id,totalTime);
            return;
        }

        final String updatorId = ConversionUtils.toString(userId);
        Find find = Find.find(filterBuilder.where("_id").is(new ObjectId(id)));
        Update update = updateBuilder.build().set("fileIds", fileIds)
                                             .set("title", title)
                                             .set("totalTime", totalTime)
                                             .set("comment", comment)
                                             .set("utId", updatorId).currentDate("ut");
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER)
                .upsert(false);
        BsonDocument modified = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);
        OfflineListenPaper paper = transform(modified);
        if (paper != null) {
            final Date date = paper.getUpdateTimestamp();
            String cacheKey = CacheKeyGenerator.generateCacheKey(OfflineListenPaper.class, id);
            getCache().getCacheObjectModifier().modify(cacheKey, entityCacheExpirationInSeconds(), 3, new ChangeCacheObject<OfflineListenPaper>() {
                @Override
                public OfflineListenPaper changeCacheObject(OfflineListenPaper currentValue) {
                    currentValue.setFileIds(fileIds);
                    currentValue.setTitle(title);
                    currentValue.setTotalTime(totalTime);
                    currentValue.setComment(comment);
                    currentValue.setUpdatorId(updatorId);
                    currentValue.setUpdateTimestamp(date);
                    return currentValue;
                }
            });
        }
    }
}
