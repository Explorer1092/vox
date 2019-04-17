/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.ambassador.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;

import javax.inject.Named;
import java.util.List;

/**
 * DAO implementation of {@link AmbassadorSchoolRef}.
 *
 * @author Summer Yang
 * @author Xiaohai Zhang
 * @since Jun 15, 2015
 */
@Named
@CacheBean(type = AmbassadorSchoolRef.class)
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class AmbassadorSchoolRefDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorSchoolRef, Long> {

    @CacheMethod
    public List<AmbassadorSchoolRef> loadByAmbassadorId(@CacheParameter("ambassadorId") Long ambassadorId) {
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(ambassadorId).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "ID");
        return query(Query.query(criteria).with(sort));
    }

    @CacheMethod
    public List<AmbassadorSchoolRef> findBySchoolId(@CacheParameter("schoolId") final Long schoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "ID");
        return query(Query.query(criteria).with(sort));
    }

    public int disabledByAmbassadorId(Long ambassadorId) {
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(ambassadorId);
        List<AmbassadorSchoolRef> originals = query(Query.query(criteria));
        if (originals.isEmpty()) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(originals);
        }
        return rows;
    }
}
