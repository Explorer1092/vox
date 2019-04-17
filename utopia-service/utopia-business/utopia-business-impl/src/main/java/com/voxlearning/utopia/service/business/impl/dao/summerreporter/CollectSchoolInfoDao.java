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

package com.voxlearning.utopia.service.business.impl.dao.summerreporter;

import com.mongodb.ReadPreference;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.utopia.service.business.api.entity.summerreport.CollectSchoolInfo;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;

/**
 * Created by jiangpeng on 16/6/14.
 */

@Named
@UtopiaCacheSupport(CollectSchoolInfo.class)
public class CollectSchoolInfoDao extends StaticMongoDao<CollectSchoolInfo,String> {

    @Override
    protected void calculateCacheDimensions(CollectSchoolInfo source, Collection<String> dimensions) {
        dimensions.add(CollectSchoolInfo.ck_id(source.getId()));
        dimensions.add(CollectSchoolInfo.ck_schoolName(source.getSchoolName()));
        dimensions.add(CollectSchoolInfo.ck_countyId(source.getCountyId()));
    }


    public Map<String,CollectSchoolInfo> findBySchoolNames(Collection<String> schoolNames){
        CacheObjectLoader.Loader<String,CollectSchoolInfo> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(CollectSchoolInfo::ck_schoolName);
        return loader.loads(CollectionUtils.toLinkedHashSet(schoolNames))
                .loadsMissed(this::__findBySchoolNames)
                .write(entityCacheExpirationInSeconds())
                .getResult();
    }

    public Map<Integer,List<CollectSchoolInfo>> findStandardByCountyId(Collection<Integer> countyIds){
        CacheObjectLoader.Loader<Integer,List<CollectSchoolInfo>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(CollectSchoolInfo::ck_countyId);
        return loader.loads(CollectionUtils.toLinkedHashSet(countyIds))
                .loadsMissed(this::__findStandardByCountyId)
                .writeAsList(entityCacheExpirationInSeconds())
                .getResult();
    }



    private Map<String, CollectSchoolInfo> __findBySchoolNames(Collection<String> schoolNames) {
        Filter filter = filterBuilder.where("school_name").in(schoolNames);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary())
                .stream().collect(Collectors.toMap(CollectSchoolInfo::getSchoolName, (p)-> p));
    }


    private Map<Integer, List<CollectSchoolInfo>> __findStandardByCountyId(Collection<Integer> countyId) {
        Filter filter = filterBuilder.where("county_id").in(countyId);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary())
                .stream().collect(Collectors.groupingBy(CollectSchoolInfo::getCountyId));
    }

    public CollectSchoolInfo loadIfPresentElseInsertBySchoolName(String schoolName, CollectSchoolInfo collectSchoolInfo) {
        if (schoolName == null || collectSchoolInfo == null) {
            return null;
        }
        Filter filter = filterBuilder.where("school_name").is(schoolName);

        Bson update = new BsonDocument("$setOnInsert", transform(collectSchoolInfo));
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER);
        BsonDocument ret = createMongoConnection().collection.findOneAndUpdate(filter.toBsonDocument(), update, options);

        return transform(ret);
    }
}
