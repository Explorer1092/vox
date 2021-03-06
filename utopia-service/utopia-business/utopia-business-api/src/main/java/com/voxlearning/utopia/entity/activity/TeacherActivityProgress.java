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

package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 2017秋季开学老师端活动
 * Feature #60599 现在春季开学数学老师继续用 UpdateAt: 2018-01-26
 *
 * @author yuechen.wang
 * @since 2017-08-14
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_ACTIVITY_TERM_2017")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TeacherActivityProgress implements CacheDimensionDocument {

    private static final long serialVersionUID = -2237659722551908121L;

    @UtopiaSqlColumn(name = "ID", primaryKey = true) private Long id;       // 参与老师ID, 这里只记录主账号老师ID
    @UtopiaSqlColumn(name = "SCHOOL_ID") private Long schoolId;             // 参与活动老师学校ID
    @UtopiaSqlColumn(name = "AUTH_STU_CNT") private Integer authStuCnt;     // 认证学生数
    @UtopiaSqlColumn(name = "DAILY_RANK") private Integer rank;             // 今日老师排名
    @UtopiaSqlColumn(name = "LEVEL_1") private Boolean level1;              // 是否发放过第一档奖励
    @UtopiaSqlColumn(name = "REWARD_TIME_1") private Date rewardDate1;      // 发放第一档奖励的时间
    @UtopiaSqlColumn(name = "LEVEL_2") private Boolean level2;              // 是否发放过第二档奖励
    @UtopiaSqlColumn(name = "REWARD_TIME_2") private Date rewardDate2;      // 发放第二档奖励的时间
    @UtopiaSqlColumn(name = "LEVEL_3") private Boolean level3;              // 是否发放过第三档奖励
    @UtopiaSqlColumn(name = "REWARD_TIME_3") private Date rewardDate3;      // 发放第三档奖励的时间

    @DocumentCreateTimestamp @UtopiaSqlColumn(name = "CREATE_TIME") private Date createTime;
    @DocumentUpdateTimestamp @UtopiaSqlColumn(name = "UPDATE_TIME") private Date updateTime;

    public TeacherActivityProgress() {
        // Dao requires Public no-args constructor
    }

    public TeacherActivityProgress(Long teacherId) {
        this.id = teacherId;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public boolean achieveLevel1() {
        return Boolean.TRUE.equals(level1);
    }

    public boolean achieveLevel2() {
        return Boolean.TRUE.equals(level2);
    }

    public boolean achieveLevel3() {
        return Boolean.TRUE.equals(level3);
    }

}
