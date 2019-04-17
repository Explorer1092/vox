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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.DynamicCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Named
@CacheBean(type = AfentiLearningPlanPushExamHistory.class)
public class AfentiLearningPlanPushExamHistoryDao extends DynamicCacheDimensionDocumentJdbcDao<AfentiLearningPlanPushExamHistory, Long> {

    @Override
    protected String calculateTableName(String template, AfentiLearningPlanPushExamHistory document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            // keep back compatibility
            long mod = document.getUserId() % 2;
            return StringUtils.formatMessage(template, mod);
        } else {
            long mod = document.getUserId() % 1000;
            return StringUtils.formatMessage(template, mod);
        }
    }

    public List<AfentiLearningPlanPushExamHistory> $queryByUserId(Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        String tableName = getTableName(userId);
        return executeQuery(Query.query(criteria), tableName);
    }

    @CacheMethod
    public Set<String> queryKnowledgePoint(@CacheParameter("UID") Long userId,
                                           @CacheParameter("S") Subject subject) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("SUBJECT").is(subject.name())
                .and("CREATETIME").gte(date());
        String tableName = getTableName(userId);
        Query query = Query.query(criteria);
        query.field().includes("RIGHT_NUM", "ERROR_NUM", "KNOWLEDGE_POINT");
        return executeQuery(query, tableName).stream()
                .filter(e -> e.getKnowledgePoint() != null)
                .filter(e -> {
                    int right = SafeConverter.toInt(e.getRightNum());
                    int error = SafeConverter.toInt(e.getErrorNum());
                    return right + error > 0;
                })
                .map(AfentiLearningPlanPushExamHistory::getKnowledgePoint)
                .collect(Collectors.toSet());
    }

    @CacheMethod
    public List<AfentiLearningPlanPushExamHistory> queryByUserIdAndNewBookId(@CacheParameter("UID") Long userId,
                                                                             @CacheParameter("NBID") String newBookId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("NEW_BOOK_ID").is(newBookId);
        String tableName = getTableName(userId);
        return executeQuery(Query.query(criteria), tableName);
    }

    public boolean updateRightAndErrorNums(AfentiLearningPlanPushExamHistory history) {
        String tableName = getTableName(history.getUserId());
        Criteria criteria = Criteria.where("ID").is(history.getId())
                .and("USER_ID").is(history.getUserId());
        Update update = Update.update("RIGHT_NUM", history.getRightNum())
                .set("ERROR_NUM", history.getErrorNum());
        long rows = executeUpdate(update, criteria, tableName);
        if (rows > 0) {
            String key = CacheKeyGenerator.generateCacheKey(AfentiLearningPlanPushExamHistory.class,
                    new String[]{"UID", "S"}, new Object[]{history.getUserId(), history.getSubject()}, new Object[]{null, ""});
            getCache().delete(key);
            key = CacheKeyGenerator.generateCacheKey(AfentiLearningPlanPushExamHistory.class,
                    new String[]{"UID", "NBID"}, new Object[]{history.getUserId(), history.getNewBookId()}, new Object[]{null, ""});
            ChangeCacheObject<List<AfentiLearningPlanPushExamHistory>> modifier = currentValue -> {
                AfentiLearningPlanPushExamHistory document = currentValue.stream()
                        .filter(e -> Objects.equals(history.getId(), e.getId()))
                        .findFirst()
                        .orElse(null);
                if (document == null) {
                    throw new UnsupportedOperationException();
                }
                document.setRightNum(history.getRightNum());
                document.setErrorNum(history.getErrorNum());
                document.setUpdatetime(new Date());
                return currentValue;
            };
            CacheValueModifierExecutor<List<AfentiLearningPlanPushExamHistory>> executor = getCache().createCacheValueModifier();
            executor.key(key)
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(modifier)
                    .execute();
        }
        return rows > 0;
    }

    public void delete(Long userId, String newBookId, String newUnitId, int rank) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("NEW_BOOK_ID").is(newBookId)
                .and("NEW_UNIT_ID").is(newUnitId)
                .and("RANK").is(rank);
        String tableName = getTableName(userId);
        List<AfentiLearningPlanPushExamHistory> documents = executeQuery(Query.query(criteria), tableName);
        if (!documents.isEmpty()) {
            Set<Long> ids = documents.stream().map(AfentiLearningPlanPushExamHistory::getId).collect(Collectors.toSet());
            AtomicReference<String> sql = new AtomicReference<>("DELETE FROM `" + tableName + "` WHERE `ID` IN (:ids)");
            MapSqlParameterSource source = new MapSqlParameterSource("ids", ids);
            int rows = getDataSourceConnection().getNamedParameterJdbcTemplate()
                    .update(sql.get(), source);
            if (rows > 0) {
                evictDocumentCache(documents);
            }
        }
    }

    // ========================================================================
    // internal methods
    // ========================================================================

    private String getTableName(Long userId) {
        AfentiLearningPlanPushExamHistory mock = new AfentiLearningPlanPushExamHistory();
        mock.setUserId(userId);
        return getDocumentTableName(mock);
    }

    private Date date() {
        Date date;
        if (RuntimeMode.current() == Mode.PRODUCTION) {
            date = DateUtils.stringToDate("2016-08-22 00:00:00");
        } else if (RuntimeMode.current() == Mode.STAGING) {
            date = DateUtils.stringToDate("2016-08-19 00:00:00");
        } else {
            date = DateUtils.stringToDate("2016-07-28 00:00:00");
        }
        return date;
    }
}
