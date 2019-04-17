/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * @author peng.zhang
 */
@Named
@CacheBean(type = StudentAdvertisementInfo.class)
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class StudentAdvertisementInfoDao extends AlpsStaticMongoDao<StudentAdvertisementInfo, String> {
    @Override
    protected void calculateCacheDimensions(StudentAdvertisementInfo source, Collection<String> dimensions) {
        dimensions.add(source.cacheKeyByUserId(source.getUserId()));
    }

    @CacheMethod
    public List<StudentAdvertisementInfo> findByUserId(@CacheParameter("UID") Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria);
        return query(query);
    }

}
