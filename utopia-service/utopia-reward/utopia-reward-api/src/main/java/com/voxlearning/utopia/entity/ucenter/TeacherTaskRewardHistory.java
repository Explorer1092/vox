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

package com.voxlearning.utopia.entity.ucenter;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Summer Yang on 2015/7/20.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_TEACHER_TASK_REWARD_HISTORY")
@UtopiaCacheExpiration
public class TeacherTaskRewardHistory extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -9182607073334501144L;

    @UtopiaSqlColumn private Long teacherId;
    @UtopiaSqlColumn private String taskType;
    @UtopiaSqlColumn private String rewardName;

    public static String ck_teacherId_taskType(Long teacherId, String taskType) {
        return CacheKeyGenerator.generateCacheKey(TeacherTaskRewardHistory.class,
                new String[]{"teacherId", "taskType"},
                new Object[]{teacherId, taskType},
                new Object[]{0L, ""});
    }
}

