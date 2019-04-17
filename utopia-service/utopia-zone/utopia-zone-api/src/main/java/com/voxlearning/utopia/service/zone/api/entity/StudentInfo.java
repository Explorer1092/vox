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

package com.voxlearning.utopia.service.zone.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.NONE;

/**
 * Student information data structure.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @serial
 * @since 2014-04-17
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
@DocumentTable(table = "VOX_STUDENT_INFO")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151222")
public class StudentInfo implements Serializable, TimestampTouchable, TimestampAccessor, PrimaryKeyAccessor<Long> {
    private static final long serialVersionUID = -1129601284654824783L;

    @UtopiaSqlColumn(name = "STUDENT_ID", primaryKey = true, primaryKeyGeneratorType = NONE) @NonNull private Long studentId;
    @UtopiaSqlColumn(name = "CREATE_DATETIME") private Date createDatetime;
    @UtopiaSqlColumn(name = "UPDATE_DATETIME") private Date updateDatetime;
    // 这两个字段已转存到 StudentExtAttribute
    @Deprecated
    @UtopiaSqlColumn(name = "BUBBLE_ID") private Long bubbleId;              // 使用的汽包
    @Deprecated
    @UtopiaSqlColumn(name = "HEAD_WEAR_ID") private String headWearId;  //头饰ID
    @UtopiaSqlColumn(name = "STUDY_MASTER_COUNT") private Integer studyMasterCount;   // 获得学霸称号次数
    @UtopiaSqlColumn(name = "LIKE_COUNT") private Integer likeCount;          // 获得赞的次数
    @UtopiaSqlColumn(name = "SIGN_IN_COUNT") private Integer signInCount;        // 总签到次数

    public static String ck_id(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(StudentInfo.class, studentId);
    }

    @Override
    @JsonIgnore
    public Long getId() {
        return studentId;
    }

    @Override
    @JsonIgnore
    public void setId(Long id) {
        this.studentId = id;
    }

    @Override
    @JsonIgnore
    public void touchCreateTime(long timestamp) {
        if (createDatetime == null) {
            createDatetime = new Date(timestamp);
        }
    }

    @Override
    @JsonIgnore
    public void touchUpdateTime(long timestamp) {
        updateDatetime = new Date(timestamp);
    }

    @Override
    public long fetchCreateTimestamp() {
        return createDatetime == null ? 0 : createDatetime.getTime();
    }

    @Override
    public long fetchUpdateTimestamp() {
        return updateDatetime == null ? 0 : updateDatetime.getTime();
    }

    @JsonIgnore
    public int getStudyMasterCountValue() {
        return studyMasterCount == null ? 0 : studyMasterCount;
    }

    @JsonIgnore
    public int getLikeCountValue() {
        return likeCount == null ? 0 : likeCount;
    }

    @JsonIgnore
    public int getSignInCountValue() {
        return signInCount == null ? 0 : signInCount;
    }

}
