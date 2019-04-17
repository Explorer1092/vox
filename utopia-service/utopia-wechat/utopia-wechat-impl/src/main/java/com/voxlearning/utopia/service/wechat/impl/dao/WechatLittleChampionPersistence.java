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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.wechat.api.entities.WechatLittleChampion;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2015/7/2.
 */
@Named
@CacheBean(type = WechatLittleChampion.class)
@Deprecated
public class WechatLittleChampionPersistence extends AlpsStaticJdbcDao<WechatLittleChampion, Long> {

    @Override
    protected void calculateCacheDimensions(WechatLittleChampion document, Collection<String> dimensions) {
        dimensions.add(WechatLittleChampion.ck_id(document.getId()));
        dimensions.add(WechatLittleChampion.ck_teacherId(document.getTeacherId()));
        dimensions.add(WechatLittleChampion.ck_studentId(document.getStudentId()));
    }

    @CacheMethod
    public List<WechatLittleChampion> findByTeacherId(@CacheParameter("teacherId") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        Sort sort = new Sort(Sort.Direction.DESC, "SCORE");
        return query(Query.query(criteria).with(sort));
    }

    @CacheMethod
    public WechatLittleChampion findByStudentId(@CacheParameter("studentId") Long studentId) {
        Criteria criteria = Criteria.where("STUDENT_ID").is(studentId);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

}
