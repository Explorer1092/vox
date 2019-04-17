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

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.LoginPrizeMonthConf;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */
@Named
@UtopiaCacheSupport(LoginPrizeMonthConf.class)
public class LoginPrizeMonthConfDao extends StaticMongoDao<LoginPrizeMonthConf, Integer> {

    @Override
    protected void calculateCacheDimensions(LoginPrizeMonthConf source, Collection<String> dimensions) {
        dimensions.add(LoginPrizeMonthConf.ck_id(source.getId()));
    }

    @Override
    protected void preprocessEntity(LoginPrizeMonthConf entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    /**
     * 获取当前月的奖励设置信息。如果当前月没有设置，则取最新的一条返回
     */
    public LoginPrizeMonthConf findConfForNow() {
        int id = SafeConverter.toInt(MonthRange.current().toString());
        return findById(id);
    }

    public LoginPrizeMonthConf findById(int id) {
        String key = LoginPrizeMonthConf.ck_id(id);
        LoginPrizeMonthConf conf = getCache().load(key);
        if (conf != null) {
            return conf;
        }

        Filter filter = filterBuilder.where("_id").lte(id);
        Find find = Find.find(filter)
                .with(new Sort(Sort.Direction.DESC, "_id")).limit(1);

        conf = __find_OTF(find, ReadPreference.primary()).stream().findFirst().orElse(null);
        if (conf == null) {
            return null;
        }
        if (Objects.equals(conf.getId(), id)) {
            getCache().safeAdd(key, entityCacheExpirationInSeconds(), conf);
            return conf;
        }

        //是较老月份的配置。需要将此配置存为新的月份
        conf.setId(id);
        conf.setCreateTime(new Date());
        conf.setUpdateTime(conf.getCreateTime());
        insert(conf);
        return conf;
    }

    public Map<String, LoginPrizeMonthConf> findThisAndNextMonth() {
        Map<String, LoginPrizeMonthConf> rtn = new HashMap<>();
        LoginPrizeMonthConf thisMonth = this.findConfForNow();
        rtn.put("thisMonth", this.findConfForNow());

        int next = SafeConverter.toInt(MonthRange.current().next().toString());
        LoginPrizeMonthConf nextMonth = load(next);
        if (null != nextMonth) {
            rtn.put("nextMonth", nextMonth);
        } else {
            rtn.put("nextMonth", saveNextMonth(thisMonth));
        }
        return rtn;
    }

    public List<LoginPrizeMonthConf> findLatestMulti(int count) {
        Set<Integer> ids = new LinkedHashSet<>();
        MonthRange range = MonthRange.current();
        for (int i = 0; i < count; i++) {
            int id = SafeConverter.toInt(range.toString());
            ids.add(id);
            range = range.previous();
        }
        return loads(ids).values().stream().sorted().collect(Collectors.toList());
    }

    public LoginPrizeMonthConf saveThisMonth(LoginPrizeMonthConf conf) {
        int id = SafeConverter.toInt(MonthRange.current().toString());
        conf.setId(id);
        ensureTimestampTouched(conf);
        conf.initializeIfNecessary();

        Bson filter = filterFromId(id);
        BsonDocument replacement = transform(conf);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndReplace(filter, replacement, options);
        LoginPrizeMonthConf saved = transform(document);
        if (saved != null) {
            String key = LoginPrizeMonthConf.ck_id(id);
            getCache().safeAdd(key, entityCacheExpirationInSeconds(), saved);
        }
        return saved;
    }

    public LoginPrizeMonthConf saveNextMonth(LoginPrizeMonthConf conf) {
        int id = SafeConverter.toInt(MonthRange.current().next().toString());
        conf.setId(id);
        ensureTimestampTouched(conf);
        conf.initializeIfNecessary();

        Bson filter = filterFromId(id);
        BsonDocument replacement = transform(conf);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndReplace(filter, replacement, options);
        LoginPrizeMonthConf saved = transform(document);
        if (saved != null) {
            String key = LoginPrizeMonthConf.ck_id(id);
            getCache().safeAdd(key, entityCacheExpirationInSeconds(), saved);
        }
        return saved;
    }
}
