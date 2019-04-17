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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.MentorRewardHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by xiaopeng.yang on 2015/5/28.
 */
@Named("com.voxlearning.utopia.service.mentor.impl.persistence.MentorRewardHistoryPersistence")
@CacheBean(type = MentorRewardHistory.class)
public class MentorRewardHistoryPersistence extends AsyncStaticMongoPersistence<MentorRewardHistory, String> {

    @Override
    protected void calculateCacheDimensions(MentorRewardHistory source, Collection<String> dimensions) {
        dimensions.add(MentorRewardHistory.ck_id(source.getId()));
        dimensions.add(MentorRewardHistory.ck_mentorId(source.getMentorId()));
    }

    @CacheMethod
    public List<MentorRewardHistory> findByMentorId(@CacheParameter("MRID") Long mentorId) {
        Criteria criteria = Criteria.where("mrid").is(mentorId);
        Query query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, "ct"));
        return query(query);
    }
}
