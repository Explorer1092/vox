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

package com.voxlearning.utopia.agent.persist.spring2016;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author yuechen.wang
 * FIXME 先把缓存去了
 * @since 2015-03-15
 */
@Named
public class AgentUserKpiResultSpring2016Persistence extends StaticPersistence<Long, AgentUserKpiResultSpring2016> {
    @Override
    protected void calculateCacheDimensions(AgentUserKpiResultSpring2016 source, Collection<String> dimensions) {

    }

    public List<AgentUserKpiResultSpring2016> find(Integer salaryMonth) {
        return withSelectFromTable("WHERE SALARY_MONTH = ? AND SCHOOL_ID IS NULL AND DISABLED=FALSE ORDER BY REGION_ID ASC, PROVINCE_ID ASC").useParamsArgs(salaryMonth).queryAll();
    }

    public List<AgentUserKpiResultSpring2016> findUserIncome(Long userId) {
        return withSelectFromTable("WHERE USER_ID = ? AND SCHOOL_ID IS NULL AND FINANCE_CHECK=TRUE AND MARKET_CHECK=TRUE AND DISABLED=FALSE ORDER BY SALARY_MONTH DESC, REGION_ID ASC, PROVINCE_ID ASC").useParamsArgs(userId).queryAll();
    }

    public List<AgentUserKpiResultSpring2016> findUserIncome(Integer salaryMonth, Long userId) {
        return withSelectFromTable("WHERE SALARY_MONTH = ? AND USER_ID = ? AND SCHOOL_ID IS NULL AND DISABLED=FALSE ORDER BY SALARY_MONTH DESC, REGION_ID ASC, PROVINCE_ID ASC").useParamsArgs(salaryMonth, userId).queryAll();
    }

    public List<AgentUserKpiResultSpring2016> findUserKpiResultsByMonth(Collection<Long> userIds, final int month) {
        Set<Long> userIdSet = CollectionUtils.toLinkedHashSet(userIds);
        if (CollectionUtils.isEmpty(userIdSet)) {
            return Collections.emptyList();
        }
        return withSelectFromTable("WHERE DISABLED=FALSE AND USER_ID IN (:userIds) AND SALARY_MONTH=:month AND SCHOOL_ID IS NULL")
                .useParams(MiscUtils.m("userIds", userIdSet, "month", month))
                .queryAll();
    }

    public int deleteByUserId(Long userId, Integer month) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT_S2016 WHERE USER_ID=? AND SALARY_MONTH=? AND SCHOOL_ID IS NULL";
        return super.getUtopiaSql().withSql(sql).useParamsArgs(userId, month).executeUpdate();
    }

    public int deleteDictSchoolData(Integer month) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT_S2016 WHERE SALARY_MONTH=? AND SCHOOL_ID IS NOT NULL";
        return super.getUtopiaSql().withSql(sql).useParamsArgs(month).executeUpdate();
    }

    public int adjustUserSalary(Long userId, Integer month, String type, Long salary, String note) {
        String update = "SET CPA_SALARY=?, CPA_NOTE=?, UPDATE_DATETIME= NOW() " +
                "WHERE USER_ID=? AND SALARY_MONTH=? AND CPA_TYPE=? ";
        return withUpdateTable(update).useParamsArgs(salary, note, userId, month, type).executeUpdate();
    }

    public int deleteByUserId(Long userId, Integer month, String cpaType) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT_S2016 WHERE USER_ID=? AND SALARY_MONTH=? AND CPA_TYPE=?";
        return getUtopiaSql().withSql(sql).useParamsArgs(userId, month, cpaType).executeUpdate();
    }

    public int deleteByMonth(Integer month) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT_S2016 WHERE SALARY_MONTH=?";
        return super.getUtopiaSql().withSql(sql).useParamsArgs(month).executeUpdate();
    }
}
