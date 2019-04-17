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

package com.voxlearning.utopia.service.mentor.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.api.constant.MentorLevel;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;

import javax.inject.Named;
import java.util.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/7/2015
 */
@Named("com.voxlearning.utopia.service.mentor.impl.persistence.MentorHistoryPersistence")
@CacheBean(type = MentorHistory.class)
public class MentorHistoryPersistence extends StaticMySQLPersistence<MentorHistory, Long> {

    @Override
    protected void calculateCacheDimensions(MentorHistory document, Collection<String> dimensions) {
        dimensions.add(MentorHistory.ck_id(document.getId()));
        dimensions.add(MentorHistory.ck_mentorId(document.getMentorId()));
        dimensions.add(MentorHistory.ck_menteeId(document.getMenteeId()));
    }

    @CacheMethod
    public List<MentorHistory> findByMentorId(@CacheParameter("mentorId") Long mentorId) {
        Criteria criteria = Criteria.where("MENTOR_ID").is(mentorId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<MentorHistory> findByMenteeId(@CacheParameter("menteeId") Long menteeId) {
        Criteria criteria = Criteria.where("MENTEE_ID").is(menteeId);
        return query(Query.query(criteria));
    }

    public int updateSuccess(final Long id) {
        MentorHistory original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("SUCCESS", true);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    public void updateLevel(Long id, MentorLevel mentorLevel) {
        MentorHistory original = $load(id);
        if (original == null) {
            return;
        }
        Update update = Update.update("MENTOR_LEVEL", mentorLevel);
        Criteria criteria = Criteria.where("ID").is(id);
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
    }

    public List<MentorHistory> loadAutoMentorRewardJobData() {
        //查10天内 所有非认证的帮助类型
        Date beginDate = DateUtils.calculateDateDay(new Date(), -10);
        Criteria criteria = Criteria.where("SUCCESS").is(false)
                .and("CREATE_DATETIME").gte(beginDate)
                .and("MENTOR_CATEGORY").ne(MentorCategory.MENTOR_AUTHENTICATION);
        return query(Query.query(criteria));
    }
}
