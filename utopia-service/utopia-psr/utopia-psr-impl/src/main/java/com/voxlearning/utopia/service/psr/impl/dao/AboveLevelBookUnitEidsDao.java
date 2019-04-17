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

package com.voxlearning.utopia.service.psr.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.NoCacheAsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.psr.entity.AboveLevelBookUnitEids;

import javax.inject.Named;

/**
 * Created by ChaoLi Lee on 14-7-7.
 * 查找 book 对应的Eid-list
 */
@Named
public class AboveLevelBookUnitEidsDao extends NoCacheAsyncStaticMongoPersistence<AboveLevelBookUnitEids, String> {

    public AboveLevelBookUnitEids findByBookId(Long bookId) {
        Criteria criteria = Criteria.where("bookid").is(bookId);
        Query query = Query.query(criteria).limit(1);
        return query(query).stream().findFirst().orElse(null);
    }
}

