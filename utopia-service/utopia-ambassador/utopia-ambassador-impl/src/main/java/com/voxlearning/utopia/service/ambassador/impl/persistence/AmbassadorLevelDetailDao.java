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
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 15-2-12.
 */
@Named
@CacheBean(type = AmbassadorLevelDetail.class)
public class AmbassadorLevelDetailDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorLevelDetail, Long> {

    @CacheMethod
    public AmbassadorLevelDetail findByAmbassadorId(@CacheParameter("ambassadorId") Long ambassadorId) {
        Criteria criteria = Criteria.where("AMBASSADOR_ID").is(ambassadorId).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    public int disabled(Long detailId) {
        AmbassadorLevelDetail original = $load(detailId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(detailId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows;
    }
}
