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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import javax.inject.Named;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Maofeng Lu
 * @since 14-10-24 下午3:43
 */
@Named
@UtopiaCacheSupport(SmartClazzQuestionRef.class)
public class SmartClazzQuestionRefPersistence extends StaticPersistence<Long, SmartClazzQuestionRef> {
    @Override
    protected void calculateCacheDimensions(SmartClazzQuestionRef source, Collection<String> dimensions) {
        dimensions.add(CacheKeyGenerator.generateCacheKey(SmartClazzQuestionRef.class, "questionId", source.getQuestionId()));
    }

    @Deprecated
    public Page<SmartClazzQuestionRef> pagingFindRefByClazzIdAndSubject(Long clazzId, Subject subject, Pageable pageable) {
        return withPageFromTable(pageable)
                .where("CLAZZ_ID=:clazzId AND SUBJECT=:subject AND DISABLED=FALSE")
                .useParams(MiscUtils.map().add("clazzId", clazzId).add("subject", subject.name()))
                .orderBy("CREATE_DATETIME DESC")
                .queryPage();
    }

    public Page<SmartClazzQuestionRef> pagingFindRefByGroupId(Long groupId, Pageable pageable) {
        return withPageFromTable(pageable)
                .where("CLAZZ_GROUP_ID=:groupId AND DISABLED=FALSE")
                .useParams(MiscUtils.map().add("groupId", groupId))
                .orderBy("CREATE_DATETIME DESC")
                .queryPage();
    }

    @UtopiaCacheable
    public List<SmartClazzQuestionRef> findSmartClazzQuestionRefByQid(@UtopiaCacheKey(name = "questionId") String questionId)
            throws DataAccessException {
        if (StringUtils.isBlank(questionId)) return Collections.emptyList();
        return withSelectFromTable("WHERE QUESTION_ID=:questionId")
                .useParams(MiscUtils.map().add("questionId", questionId)).queryAll();
    }

    public SmartClazzQuestionRef findQuestionByClazzIdAndQuestionId(Long clazzId, String questionId) {
        if (clazzId == null || StringUtils.isBlank(questionId)) return null;
        return withSelectFromTable("WHERE CLAZZ_ID=:clazzId AND QUESTION_ID=:questionId")
                .useParams(MiscUtils.map("clazzId", clazzId, "questionId", questionId)).queryObject();
    }


    public void batchUpdateAndEvictCache(String questionId, final List<SmartClazzQuestionRef> refList) {
        if (CollectionUtils.isEmpty(refList)) {
            return;
        }
        String sql = "UPDATE VOX_SMARTCLAZZ_QUESTION_REF SET UPDATE_DATETIME=?, DISABLED=? WHERE ID=?";
        utopiaSql.getJdbcOperations().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setBoolean(2, refList.get(i).getDisabled());
                ps.setLong(3, refList.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return refList.size();
            }
        });
    }

    public void batchInsert(final List<SmartClazzQuestionRef> refList) {
        if (CollectionUtils.isEmpty(refList)) {
            return;
        }
        String sql = " INSERT INTO VOX_SMARTCLAZZ_QUESTION_REF(CLAZZ_ID,SUBJECT,QUESTION_ID,CLAZZ_GROUP_ID,DISABLED,CREATE_DATETIME,UPDATE_DATETIME) VALUES(?,?,?,?,?,?,?)";
        utopiaSql.getJdbcOperations().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, refList.get(i).getClazzId());
                ps.setString(2, refList.get(i).getSubject().name());
                ps.setString(3, refList.get(i).getQuestionId());
                ps.setLong(4, refList.get(i).getGroupId());
                ps.setBoolean(5, refList.get(i).getDisabled());
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            }

            @Override
            public int getBatchSize() {
                return refList.size();
            }
        });
    }
}
