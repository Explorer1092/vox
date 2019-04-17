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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.agent.persist.entity.AgentOnlinePayShareDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Alex on 15-2-12.
 */
@Named
public class AgentOnlinePayShareDetailPersistence extends AlpsStaticJdbcDao<AgentOnlinePayShareDetail, Long> {

    @Override
    protected void calculateCacheDimensions(AgentOnlinePayShareDetail document, Collection<String> dimensions) {
    }

    public List<AgentOnlinePayShareDetail> findUserRecentData(Long userId) {
        String s = "SELECT DISTINCT(KPI_EVAL_DATE) FROM AGENT_ONLINE_PAY_SHARE_DETAIL ORDER BY KPI_EVAL_DATE DESC LIMIT 1";
        AtomicReference<String> sql = new AtomicReference<>(s);
        Date kpiEvalDate = getJdbcTemplate().queryForObject(sql.get(), Date.class);
        if (kpiEvalDate == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("KPI_EVAL_DATE").is(kpiEvalDate);
        return query(Query.query(criteria));
    }

    public List<AgentOnlinePayShareDetail> findUserData(Long userId, Date runDate) {
        DayRange range = DayRange.newInstance(runDate.getTime());
        Date start = range.getStartDate();
        Date end = range.getEndDate();
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("KPI_EVAL_DATE").gte(start).lte(end);
        return query(Query.query(criteria));
    }


    public int deleteByRunDate(final Date runDate) {
        DayRange range = DayRange.newInstance(runDate.getTime());
        Date start = range.getStartDate();
        Date end = range.getEndDate();
        Criteria criteria = Criteria.where("KPI_EVAL_DATE").gte(start).lte(end);
        return (int) $remove(criteria);
    }

    public int deleteByRunDate(final Date runDate, Long userId) {
        DayRange range = DayRange.newInstance(runDate.getTime());
        Date start = range.getStartDate();
        Date end = range.getEndDate();
        Criteria criteria = Criteria.where("KPI_EVAL_DATE").gte(start).lte(end)
                .and("USER_ID").is(userId);
        return (int) $remove(criteria);
    }
}