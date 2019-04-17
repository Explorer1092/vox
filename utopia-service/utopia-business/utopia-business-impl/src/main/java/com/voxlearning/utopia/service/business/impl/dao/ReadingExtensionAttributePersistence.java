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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.entity.content.ReadingExtensionAttribute;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 14-8-6.
 */
@Named
@UtopiaCacheSupport(ReadingExtensionAttribute.class)
public class ReadingExtensionAttributePersistence extends StaticCacheDimensionDocumentJdbcDao<ReadingExtensionAttribute, Long> {

    @UtopiaCacheable
    public ReadingExtensionAttribute findByBookIdAndUnitIdAndReadingId(@UtopiaCacheKey(name = "bookId") Long bookId,
                                                                       @UtopiaCacheKey(name = "unitId") Long unitId,
                                                                       @UtopiaCacheKey(name = "readingId") Long readingId) {
        Criteria criteria = new Criteria()
                .and("BOOK_ID").is(bookId)
                .and("UNIT_ID").is(unitId)
                .and("READING_ID").is(readingId);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, ReadingExtensionAttribute> findByBookIdAndUnitIdAndReadingIds(@CacheParameter("bookId") final Long bookId,
                                                                                   @CacheParameter("unitId") final Long unitId,
                                                                                   @CacheParameter(value = "readingId", multiple = true) final Collection<Long> readingIds) {
        Criteria criteria = new Criteria()
                .and("BOOK_ID").is(bookId)
                .and("UNIT_ID").is(unitId)
                .and("READING_ID").in(readingIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.toMap(ReadingExtensionAttribute::getReadingId, Function.identity()));
    }
}
