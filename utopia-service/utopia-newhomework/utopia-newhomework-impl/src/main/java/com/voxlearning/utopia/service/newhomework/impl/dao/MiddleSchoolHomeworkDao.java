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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomework;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type = MiddleSchoolHomework.class)
public class MiddleSchoolHomeworkDao extends AlpsStaticMongoDao<MiddleSchoolHomework, String> {

    @Override
    protected void calculateCacheDimensions(MiddleSchoolHomework document, Collection<String> dimensions) {

    }

    public List<MiddleSchoolHomework> loadGroupHomeworkList(Collection<Long> groupIds, Date startDate, Date endDate) {
        Collection<Integer> statusList = new ArrayList<>();
        statusList.add(0);
        statusList.add(-1);

        Criteria criteria = Criteria.where("create_time").gte(startDate).lte(endDate);
        criteria.and("clazz_id").in(groupIds);
        criteria.and("status").in(statusList);
        criteria.and("type").is(3);
        Sort sort = new Sort(Sort.Direction.DESC, "create_time");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
}
