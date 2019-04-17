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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportInfo;

import javax.inject.Named;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 14-10-29.
 */
@Named
@CacheBean(type = AmbassadorReportInfo.class)
public class AmbassadorReportInfoDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorReportInfo, Long> {

    public List<AmbassadorReportInfo> loadByType(Integer type) {
        Criteria criteria = Criteria.where("TYPE").is(type).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public int deleteById(long id) {
        AmbassadorReportInfo original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows;
    }

    @CacheMethod
    public List<AmbassadorReportInfo> loadByTeacherId(@CacheParameter("teacherId") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}
