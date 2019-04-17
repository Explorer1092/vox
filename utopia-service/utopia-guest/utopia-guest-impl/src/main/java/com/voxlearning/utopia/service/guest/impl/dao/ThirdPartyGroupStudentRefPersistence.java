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

package com.voxlearning.utopia.service.guest.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupStudentRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ThirdPartyGroupStudentRef} persistence implementation.
 *
 * @author xuesong.zhang
 * @since 2016/9/20
 */
@Named("com.voxlearning.utopia.service.guest.impl.dao.ThirdPartyGroupStudentRefPersistence")
@CacheBean(type = ThirdPartyGroupStudentRef.class)
public class ThirdPartyGroupStudentRefPersistence extends StaticMySQLPersistence<ThirdPartyGroupStudentRef, Long> {

    @Override
    protected void calculateCacheDimensions(ThirdPartyGroupStudentRef source, Collection<String> dimensions) {
        dimensions.add(ThirdPartyGroupStudentRef.ck_id(source.getId()));
        dimensions.add(ThirdPartyGroupStudentRef.ck_groupId(source.getGroupId()));
        dimensions.add(ThirdPartyGroupStudentRef.ck_studentId(source.getStudentId()));
    }

    @CacheMethod
    public List<ThirdPartyGroupStudentRef> findByGroupId(@CacheParameter("G") final Long groupId) {
        Criteria criteria = Criteria.where("THIRD_PARTY_GROUP_ID").is(groupId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<ThirdPartyGroupStudentRef>> findByGroupIds(@CacheParameter(value = "G", multiple = true) Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("THIRD_PARTY_GROUP_ID").in(groupIds).and("DISABLED").is(false);
        Map<Long, List<ThirdPartyGroupStudentRef>> map = query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(ThirdPartyGroupStudentRef::getGroupId));
        return MapUtils.resort(map, groupIds, Collections.emptyList());
    }

    @CacheMethod
    public List<ThirdPartyGroupStudentRef> findByUserId(@CacheParameter("S") final Long userId) {
        Criteria criteria = Criteria.where("STUDENT_ID").is(userId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<ThirdPartyGroupStudentRef>> findByUserIds(@CacheParameter(value = "S", multiple = true) Collection<Long> userIds) {
        Criteria criteria = Criteria.where("STUDENT_ID").in(userIds).and("DISABLED").is(false);
        Map<Long, List<ThirdPartyGroupStudentRef>> map = query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(ThirdPartyGroupStudentRef::getStudentId));
        return MapUtils.resort(map, userIds, Collections.emptyList());
    }

    public boolean disable(Long id) {
        Criteria criteria = Criteria.where("ID").is(id).and("DISABLED").is(false);
        ThirdPartyGroupStudentRef original = query(Query.query(criteria)).stream().findFirst().orElse(null);
        if (original == null) {
            return false;
        }
        Update update = Update.update("DISABLED", true);
        long rows = $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows > 0;
    }
}
