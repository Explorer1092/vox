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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.entity.activity.tuckerhomework.TuckerActivityRecord;

import javax.inject.Named;

/**
 * 2018春季开学老师端活动
 *
 * @author yuechen.wang
 * @since 2018-02-02
 */
@Named
@CacheBean(type = TuckerActivityRecord.class)
public class TuckerActivityRecordDao extends StaticCacheDimensionDocumentMongoDao<TuckerActivityRecord, String> {

    @CacheMethod
    public TuckerActivityRecord loadByTeacherId(@CacheParameter("teacherId") Long teacherId){
        Criteria criteria = Criteria.where("teacherId").is(teacherId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

}
