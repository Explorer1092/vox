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

package com.voxlearning.utopia.service.business.impl.support.mode1;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.RangeableMongoDao;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt64;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个按照时间范围切表
 */
abstract public class AbstractRSRegionStatDao1<E extends Serializable>
        extends RangeableMongoDao<E> {

    @Override
    protected void calculateCacheDimensions(E source, Collection<String> dimensions) {
    }

    public List<E> findByCityCode(Long cityCode) {
        if (cityCode == null) {
            return Collections.emptyList();
        }
        BsonDocument filter = new BsonDocument("ccode", new BsonInt64(cityCode));
        return __find_OTF(filter, ReadPreference.primary());
    }

    public List<E> findByCityCodes(Collection<Long> cityCodes) {
        cityCodes = CollectionUtils.toLinkedHashSet(cityCodes);
        if (cityCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<BsonInt64> ccodes = cityCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("ccode", new BsonDocument("$in", new BsonArray(ccodes)));
        return __find_OTF(filter, ReadPreference.primary());
    }
}
