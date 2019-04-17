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

package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.NoCacheAsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.DayIncreaseDataStatus;

import javax.inject.Named;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2016/3/8
 */
@Named("com.voxlearning.utopia.service.business.impl.persistence.DayIncreaseDataStatusPersistence")
public class DayIncreaseDataStatusPersistence extends NoCacheAsyncStaticMongoPersistence<DayIncreaseDataStatus, String> {

    public List<DayIncreaseDataStatus> findLastSuccess() {
        Criteria criteria = Criteria.where("executeStatus").is(1);
        Sort sort = new Sort(Sort.Direction.DESC, "dataDay");
        Query query = Query.query(criteria).with(sort).limit(1);
        return query(query);
    }
}
