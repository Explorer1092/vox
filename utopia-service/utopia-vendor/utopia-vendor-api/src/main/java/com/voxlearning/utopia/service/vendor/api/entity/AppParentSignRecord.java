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

package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.spi.common.DateFormatParser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by malong on 2016/5/11.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "vox_app_parent_sign_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20160511")
public class AppParentSignRecord implements Serializable {
    private static final long serialVersionUID = -4130345766566930268L;

    @DocumentId
    private String id;                                                //userId
    private Map<String, Long> appParentSignRecordMap;
    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    public static String generateCacheKeyById(String id) {
        return CacheKeyGenerator.generateCacheKey(AppParentSignRecord.class, id);
    }

    public boolean hasSigned(MonthRange monthRange) {
        return getAppParentSignRecordMap() != null && getAppParentSignRecordMap().keySet().stream()
                .anyMatch(p -> monthRange.contains(DateFormatParser.getInstance().parse(p, "yyyyMMdd")));
    }
}
