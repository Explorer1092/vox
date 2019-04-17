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

package com.voxlearning.utopia.service.feedback.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.api.constant.RegisterFeedbackCategory;
import com.voxlearning.utopia.api.constant.RegisterFeedbackState;
import com.voxlearning.utopia.service.feedback.api.entities.RegisterFeedback;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author xin.xin
 * @since 2014-03-03
 */
@Named("com.voxlearning.utopia.service.feedback.impl.dao.RegisterFeedbackPersistence")
@CacheBean(type = RegisterFeedback.class)
public class RegisterFeedbackPersistence extends AlpsStaticJdbcDao<RegisterFeedback, Long> {

    @Override
    protected void calculateCacheDimensions(RegisterFeedback document, Collection<String> dimensions) {
        dimensions.add(RegisterFeedback.ck_id(document.getId()));
    }

    public List<RegisterFeedback> find(int state, Date start, Date end) {
        List<Criteria> list = new ArrayList<>();
        if (state > 0) {
            list.add(Criteria.where("STATE").is(state));
        }
        if (start != null) {
            list.add(Criteria.where("CREATE_DATETIME").gte(start));
        }
        if (end != null) {
            list.add(Criteria.where("CREATE_DATETIME").lte(end));
        }
        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public List<RegisterFeedback> findByMobileAndCategoryAndStateWithinDayRange(String mobile,
                                                                                RegisterFeedbackCategory category,
                                                                                RegisterFeedbackState state) {
        DayRange range = DayRange.current();
        Criteria criteria = Criteria.where("MOBILE").is(mobile)
                .and("CATEGORY").is(category)
                .and("STATE").is(state.getType())
                .and("CREATE_DATETIME").gt(range.getStartDate()).lt(range.getEndDate());
        return query(Query.query(criteria));
    }
}
