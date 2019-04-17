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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;

import javax.inject.Named;
import java.util.*;

/**
 * {@link StudentInfo} persistence implementation.
 * Use cache to improve performance. Only primary key will be treated as cache key.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 2014-04-17
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence")
@UtopiaCacheSupport(StudentInfo.class)
public class StudentInfoPersistence extends StaticPersistence<Long, StudentInfo> {

    @Override
    protected void calculateCacheDimensions(StudentInfo source, Collection<String> dimensions) {
        dimensions.add(StudentInfo.ck_id(source.getStudentId()));
    }

    public List<StudentInfo> findByUserIdsOrderByStudyMasterCountDesc(Set<Long> userIds) {
        List<StudentInfo> studentInfos = new LinkedList<>(loads(userIds).values());
        if (CollectionUtils.isEmpty(studentInfos)) {
            return Collections.emptyList();
        }
        Collections.sort(studentInfos, new Comparator<StudentInfo>() {
            @Override
            public int compare(StudentInfo o1, StudentInfo o2) {
                Integer c1 = o1.getStudyMasterCountValue();
                Integer c2 = o2.getStudyMasterCountValue();
                return c2.compareTo(c1);
            }
        });
        return studentInfos;
    }

    public List<StudentInfo> findByUserIdsOrderByLikeCountDesc(Set<Long> userIds) {
        List<StudentInfo> studentInfos = new LinkedList<>(loads(userIds).values());
        if (CollectionUtils.isEmpty(studentInfos)) {
            return Collections.emptyList();
        }
        Collections.sort(studentInfos, new Comparator<StudentInfo>() {
            @Override
            public int compare(StudentInfo o1, StudentInfo o2) {
                Integer l1 = o1.getLikeCountValue();
                Integer l2 = o2.getLikeCount();
                return l2.compareTo(l1);
            }
        });
        return studentInfos;
    }

    // insert on duplicate key update' can only be used under mysql ???

    public void createOrIncreaseStudyMasterCountByOne(final Long studentId) {
        String sql = "INSERT INTO VOX_STUDENT_INFO (STUDENT_ID, CREATE_DATETIME, UPDATE_DATETIME, STUDY_MASTER_COUNT, LIKE_COUNT, BUBBLE_ID, SIGN_IN_COUNT) " +
                "VALUES (?,NOW(),NOW(),1,0,0,0) ON DUPLICATE KEY UPDATE STUDY_MASTER_COUNT=STUDY_MASTER_COUNT+1, UPDATE_DATETIME=NOW() ";
        int rows = utopiaSql.withSql(sql).useParamsArgs(studentId).executeUpdate();
        if (rows > 0) {
            getCache().delete(StudentInfo.ck_id(studentId));
        }
    }

    public void createOrIncreaseLikeCountByOne(final Long studentId) {
        String sql = "INSERT INTO VOX_STUDENT_INFO (STUDENT_ID, CREATE_DATETIME, UPDATE_DATETIME, STUDY_MASTER_COUNT, LIKE_COUNT, BUBBLE_ID, SIGN_IN_COUNT) " +
                "VALUES (?,NOW(),NOW(),0,1,0,0) ON DUPLICATE KEY UPDATE LIKE_COUNT=LIKE_COUNT+1, UPDATE_DATETIME=NOW() ";
        int rows = utopiaSql.withSql(sql).useParamsArgs(studentId).executeUpdate();
        if (rows > 0) {
            getCache().delete(StudentInfo.ck_id(studentId));
        }
    }

    public void createOrUpdateBubble(final Long studentId, final Long bubbleId) {
        String sql = "INSERT INTO VOX_STUDENT_INFO (STUDENT_ID, CREATE_DATETIME, UPDATE_DATETIME, STUDY_MASTER_COUNT, LIKE_COUNT, BUBBLE_ID, SIGN_IN_COUNT) " +
                "VALUES (?,NOW(),NOW(),0,0,?,0) ON DUPLICATE KEY UPDATE BUBBLE_ID=?, UPDATE_DATETIME=NOW() ";
        int rows = utopiaSql.withSql(sql).useParamsArgs(studentId, bubbleId, bubbleId).executeUpdate();
        if (rows > 0) {
            getCache().delete(StudentInfo.ck_id(studentId));
        }
    }

    public void createOrIncreaseSignInCountByOne(final Long studentId) {
        String sql = "INSERT INTO VOX_STUDENT_INFO (STUDENT_ID, CREATE_DATETIME, UPDATE_DATETIME, STUDY_MASTER_COUNT, LIKE_COUNT, BUBBLE_ID, SIGN_IN_COUNT) " +
                "VALUES (?,NOW(),NOW(),0,0,0,1) ON DUPLICATE KEY UPDATE SIGN_IN_COUNT=SIGN_IN_COUNT+1, UPDATE_DATETIME=NOW() ";
        int rows = utopiaSql.withSql(sql).useParamsArgs(studentId).executeUpdate();
        if (rows > 0) {
            getCache().delete(StudentInfo.ck_id(studentId));
        }
    }

    public void createOrUpdateHeadWear(Long studentId, String headWearId) {
        String sql = "INSERT INTO VOX_STUDENT_INFO (STUDENT_ID, CREATE_DATETIME, UPDATE_DATETIME, STUDY_MASTER_COUNT, LIKE_COUNT, BUBBLE_ID, SIGN_IN_COUNT,HEAD_WEAR_ID) " +
                "VALUES (?,NOW(),NOW(),0,0,0,0,?) ON DUPLICATE KEY UPDATE HEAD_WEAR_ID=?, UPDATE_DATETIME=NOW() ";
        int rows = utopiaSql.withSql(sql).useParamsArgs(studentId, headWearId, headWearId).executeUpdate();
        if (rows > 0) {
            getCache().delete(StudentInfo.ck_id(studentId));
        }
    }
}
