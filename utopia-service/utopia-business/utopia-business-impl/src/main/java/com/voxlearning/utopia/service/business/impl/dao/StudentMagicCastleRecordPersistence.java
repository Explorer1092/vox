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

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.activity.StudentMagicCastleRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Summer Yang on 2015/12/1.
 */
@Named
@UtopiaCacheSupport(StudentMagicCastleRecord.class)
public class StudentMagicCastleRecordPersistence extends AlpsStaticJdbcDao<StudentMagicCastleRecord, Long> {

    @Override
    protected void calculateCacheDimensions(StudentMagicCastleRecord document, Collection<String> dimensions) {
        dimensions.add(StudentMagicCastleRecord.ck_clazzId(document.getClazzId()));
        dimensions.add(StudentMagicCastleRecord.ck_magicianId(document.getMagicianId()));
        dimensions.add(StudentMagicCastleRecord.ck_activeId(document.getActiveId()));
    }

    @UtopiaCacheable
    public List<StudentMagicCastleRecord> findUnDisabledByClazzId(@UtopiaCacheKey(name = "clazzId") Long clazzId) {
        Criteria criteria = Criteria.where("CLAZZ_ID").is(clazzId)
                .and("DISABLED").is(false)
                .and("SUCCESS").is(false);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable
    public List<StudentMagicCastleRecord> loadActivingRecordByMagicianId(@UtopiaCacheKey(name = "magicianId") Long magicianId) {
        Criteria criteria = Criteria.where("MAGICIAN_ID").is(magicianId)
                .and("DISABLED").is(false)
                .and("SUCCESS").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    @UtopiaCacheable
    public List<StudentMagicCastleRecord> findByActiveIdIncludeDisabled(@UtopiaCacheKey(name = "activeId") Long activeId) {
        Criteria criteria = Criteria.where("ACTIVE_ID").is(activeId);
        return query(Query.query(criteria));
    }

    public void disabled(Long id) {
        StudentMagicCastleRecord original = $load(id);
        if (original == null) {
            return;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(id);
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
    }

    public void success(Long id) {
        StudentMagicCastleRecord original = $load(id);
        if (original == null) {
            return;
        }
        Update update = Update.update("DISABLED", true)
                .set("SUCCESS", true)
                .set("SUCCESS_LEVEL", 1);
        Criteria criteria = Criteria.where("ID").is(id);
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
    }

    public void updateSuccessLevel(Long id, int level) {
        StudentMagicCastleRecord original = $load(id);
        if (original == null) {
            return;
        }
        Update update = Update.update("SUCCESS_LEVEL", level);
        Criteria criteria = Criteria.where("ID").is(id);
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
    }
}
