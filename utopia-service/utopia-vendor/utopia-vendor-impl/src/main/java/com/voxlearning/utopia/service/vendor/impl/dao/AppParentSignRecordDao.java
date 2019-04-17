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

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.vendor.api.entity.AppParentSignRecord;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by malong on 2016/5/11.
 */
@Named
@UtopiaCacheSupport(AppParentSignRecord.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class AppParentSignRecordDao extends StaticMongoDao<AppParentSignRecord, String> {

    @Override
    protected void calculateCacheDimensions(AppParentSignRecord source, Collection<String> dimensions) {
        dimensions.add(AppParentSignRecord.generateCacheKeyById(source.getId()));
    }

    public AppParentSignRecord loadAppParentRecordByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return load(ConversionUtils.toString(userId));
    }

    public List<AppParentSignRecord> loadAppParentSignRecordByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }

        Set<String> ids = userIds.stream().map(ConversionUtils::toString).collect(Collectors.toSet());
        return loads(ids).values().stream().collect(Collectors.toList());
    }

    public MapMessage updateOrInsertAppParentSignRecord(Long userId) {
        String currentDay = DayRange.current().toString();
        String id = ConversionUtils.toString(userId);
        long millis = System.currentTimeMillis();
        Update update = updateBuilder.build()
                .setOnInsert("createTime", millis)
                .set("appParentSignRecordMap." + currentDay, millis)
                .set("updateTime", millis);

        AppParentSignRecord appParentSignRecord = upsert(id, update);
        if (appParentSignRecord != null) {
            return MapMessage.successMessage("签到成功");
        }
        return MapMessage.errorMessage("签到失败");
    }
}
