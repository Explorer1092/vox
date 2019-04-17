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

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.agent.persist.entity.AgentUserKpiResult;

import javax.inject.Named;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex on 2014/7/10
 */
@Named
public class AgentUserKpiResultPersistence extends AbstractEntityPersistence<Long, AgentUserKpiResult> {

    public List<AgentUserKpiResult> findUserRecentData(Long userId) {
        return withSelectFromTable("WHERE USER_ID = ? AND KPI_EVAL_DATE = (SELECT DISTINCT(KPI_EVAL_DATE) FROM AGENT_USER_KPI_RESULT ORDER BY KPI_EVAL_DATE DESC LIMIT 1)").useParamsArgs(userId).queryAll();
    }

    public List<AgentUserKpiResult> findUserData(Long userId, Date runDate) {
        return withSelectFromTable("WHERE USER_ID = ? AND DATEDIFF(KPI_EVAL_DATE,?) = 0").useParamsArgs(userId, runDate).queryAll();
    }

    public List<AgentUserKpiResult> findByUser(Long userId) {
        return withSelectFromTable("WHERE USER_ID=? AND FINANCE_CHECK = TRUE AND MANAGER_CHECK = TRUE ORDER BY KPI_EVAL_DATE DESC, REGION_NAME").useParamsArgs(userId).queryAll();
    }

    public int delete(final Long id) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT WHERE ID=?";
        return getUtopiaSql().withSql(sql).useParamsArgs(id).executeUpdate();
    }

    public List<AgentUserKpiResult> findByEvalDate(Date runDate, Long userId) {
        return withSelectFromTable("WHERE DATEDIFF(KPI_EVAL_DATE,?) = 0 AND USER_ID = ? AND FINANCE_CHECK = TRUE AND MANAGER_CHECK = TRUE").useParamsArgs(runDate, userId).queryAll();
    }

    public List<AgentUserKpiResult> findByUserIdAndKpiEvalId(Long userId, Long kpiEvalId) {
        return withSelectFromTable("WHERE USER_ID=? AND KPI_EVAL_ID=? AND FINANCE_CHECK = TRUE AND MANAGER_CHECK = TRUE").useParamsArgs(userId, kpiEvalId).queryAll();
    }

    public int deleteByEvalDate(final Date runDate, Long userId) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT WHERE DATEDIFF(KPI_EVAL_DATE,?) = 0 AND USER_ID=?";
        return getUtopiaSql().withSql(sql).useParamsArgs(runDate, userId).executeUpdate();
    }

    public int deleteByEvalDate(final Date runDate) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT WHERE DATEDIFF(KPI_EVAL_DATE,?) = 0";
        return getUtopiaSql().withSql(sql).useParamsArgs(runDate).executeUpdate();
    }

    public int deleteByUserIdAndKpiEvalId(Long userId, Long kpiEvalId) {
        final String sql = "DELETE FROM AGENT_USER_KPI_RESULT WHERE USER_ID=? AND KPI_EVAL_ID=?";
        return getUtopiaSql().withSql(sql).useParamsArgs(userId, kpiEvalId).executeUpdate();
    }

    public AgentUserKpiResult findByUserIdAndEvalIdAndEvalDate(Long userId, Long evalId, Integer regionCode, Date evalDate) {
        return withSelectFromTable("WHERE USER_ID=? AND KPI_EVAL_ID=? AND REGION_CODE=? AND DATEDIFF(KPI_EVAL_DATE,?) = 0").useParamsArgs(userId, evalId, regionCode, evalDate).queryObject();
    }

    public List<AgentUserKpiResult> findKpiResultByEvalDate(Date evalDate) {
        return withSelectFromTable("WHERE DATEDIFF(KPI_EVAL_DATE,?) = 0").useParamsArgs(evalDate).queryAll();
    }

    public List<AgentUserKpiResult> findByIds(List<Long> ids) {
        return withSelectFromTable("WHERE ID IN(:ids)").useParams(MiscUtils.map("ids", ids)).queryAll();
    }

    public List<String> findAllKpiEvalDates() {
        String sql = "SELECT DISTINCT DATE_FORMAT(KPI_EVAL_DATE, '%Y-%m-%d') FROM AGENT_USER_KPI_RESULT";
        return new LinkedList<>(getUtopiaSql().withSql(sql).queryColumnValues(String.class));
    }

    public List<AgentUserKpiResult> findUserKpiResult(Long userId) {
        return withSelectFromTable("WHERE USER_ID=? ORDER BY KPI_EVAL_DATE, KPI_EVAL_ID").useParamsArgs(userId).queryAll();
    }
}
