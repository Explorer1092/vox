/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2018 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class CrmJxtNewsDao extends AsyncStaticMongoPersistence<JxtNews, String> {

    @Override
    protected void calculateCacheDimensions(JxtNews jxtNews, Collection<String> collection) {
    }

    public List<JxtNews> loadAllFromSecondary() {
        Query query = Query.query(new Criteria());
        return $executeQuery(createMongoConnection(), query, ReadPreference.secondaryPreferred(), null)
                .getUninterruptibly();
    }
}
