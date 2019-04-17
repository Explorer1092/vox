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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.content.ReadingParts;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 14-6-20.
 */
@Named
@UtopiaCacheSupport(ReadingParts.class)
public class ReadingPartsPersistence extends StaticCacheDimensionDocumentJdbcDao<ReadingParts, Long> {

    @UtopiaCacheable
    public List<ReadingParts> findByReadingId(@UtopiaCacheKey(name = "readingId") Long readingId) {
        Criteria criteria = Criteria.where("READING_ID").is(readingId);
        Sort sort = new Sort(Sort.Direction.ASC, "PAGE_NUM", "SUB_PAGE_NUM", "PARAGRAPH", "RANK");
        return query(Query.query(criteria).with(sort));
    }

    @CacheMethod
    public Map<Long, List<ReadingParts>> findByReadingIds(@CacheParameter(value = "readingId", multiple = true)
                                                          final Collection<Long> readingIds) {
        Criteria criteria = Criteria.where("READING_ID").in(readingIds);
        Sort sort = new Sort(Sort.Direction.ASC, "PAGE_NUM", "SUB_PAGE_NUM", "PARAGRAPH", "RANK");
        return query(Query.query(criteria).with(sort)).stream()
                .collect(Collectors.groupingBy(ReadingParts::getReadingId));
    }
}
