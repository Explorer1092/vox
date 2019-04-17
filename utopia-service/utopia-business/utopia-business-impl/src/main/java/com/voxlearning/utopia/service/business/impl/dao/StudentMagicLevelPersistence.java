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

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/12/1.
 */
@Named
@UtopiaCacheSupport(StudentMagicLevel.class)
public class StudentMagicLevelPersistence extends AlpsStaticJdbcDao<StudentMagicLevel, Long> {

    @Override
    protected void calculateCacheDimensions(StudentMagicLevel document, Collection<String> dimensions) {
        dimensions.add(StudentMagicLevel.ck_magicianId(document.getMagicianId()));
    }

    @UtopiaCacheable
    public StudentMagicLevel findByMagicianId(@UtopiaCacheKey(name = "magicianId") Long magicianId) {
        Criteria criteria = Criteria.where("MAGICIAN_ID").is(magicianId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1))
                .stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, StudentMagicLevel> loadByMagicianIds(@CacheParameter(value = "magicianId", multiple = true)
                                                                  Collection<Long> magicianIds) {
        Criteria criteria = Criteria.where("MAGICIAN_ID").in(magicianIds)
                .and("DISABLED").is(false);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.toMap(StudentMagicLevel::getMagicianId, Function.identity()));
    }

    public void updateLevel(Long magicianId, int level) {
        Update update = Update.update("LEVEL", level);
        Criteria criteria = Criteria.where("MAGICIAN_ID").is(magicianId);
        if ($update(update, criteria) > 0) {
            getCache().delete(StudentMagicLevel.ck_magicianId(magicianId));
        }
    }

    public void updateLevelValue(Long magicianId, int exp) {
        Update update = Update.update("LEVEL_VALUE", exp);
        Criteria criteria = Criteria.where("MAGICIAN_ID").is(magicianId);
        if ($update(update, criteria) > 0) {
            getCache().delete(StudentMagicLevel.ck_magicianId(magicianId));
        }
    }
}
