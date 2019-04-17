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

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.*;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorRewardHistory;

import javax.inject.Named;
import java.util.List;

@Named
public class AmbassadorRewardHistoryDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorRewardHistory, Long> {

    public Page<AmbassadorRewardHistory> find(Pageable pageable, String month, Integer status) {
        pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.ASC, "ID");
        Criteria criteria = Criteria.where("MONTH").is(month).and("STATUS").is(status);
        Query query = Query.query(criteria).with(pageable);
        List<AmbassadorRewardHistory> content = query(query);
        long total = count(Query.query(criteria));
        return new PageImpl<>(content, pageable, total);
    }
}
