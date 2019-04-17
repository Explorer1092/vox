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

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.content.ReadingQuestions;

import javax.inject.Named;
import java.util.List;

/**
 * Persistence implementation of entity {@link ReadingQuestions}.
 *
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @since Jun 20, 2014
 */
@Named
@UtopiaCacheSupport(ReadingQuestions.class)
public class ReadingQuestionsPersistence extends StaticCacheDimensionDocumentJdbcDao<ReadingQuestions, Long> {

    @UtopiaCacheable
    public List<ReadingQuestions> findByReadingId(@UtopiaCacheKey(name = "readingId") Long readingId) {
        Criteria criteria = Criteria.where("READING_ID").is(readingId);
        Sort sort = new Sort(Sort.Direction.ASC, "RANK");
        return query(Query.query(criteria).with(sort));
    }
}
