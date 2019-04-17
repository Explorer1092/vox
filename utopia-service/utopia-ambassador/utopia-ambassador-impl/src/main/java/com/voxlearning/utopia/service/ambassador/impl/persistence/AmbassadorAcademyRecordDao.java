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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorAcademyRecord;

import javax.inject.Named;

/**
 * DAO implementation of {@link AmbassadorAcademyRecord}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since May 6, 2015
 */
@Named
@CacheBean(type = AmbassadorAcademyRecord.class)
public class AmbassadorAcademyRecordDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorAcademyRecord, Long> {

    @CacheMethod
    public AmbassadorAcademyRecord findByAmbassadorId(@CacheParameter("ambassadorId") Long ambassadorId) {
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(ambassadorId)
                .and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "ID");
        return query(Query.query(criteria).with(sort).limit(1)).stream().findFirst().orElse(null);
    }
}
