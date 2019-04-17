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

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2015/7/30.
 */
@DocumentTable(table = "VOX_TERM_BEGIN_STUDENT_APP_RECORD")
@NoArgsConstructor
@UtopiaCacheExpiration
public class TermBeginStudentAppRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -2338850581291764508L;
    @UtopiaSqlColumn @Getter @Setter private Long teacherId;
    @UtopiaSqlColumn @Getter @Setter private Long studentId;
    @UtopiaSqlColumn @Getter @Setter private Long clazzId;
    @UtopiaSqlColumn @Getter @Setter private String homeworkId;
    @UtopiaSqlColumn @Getter @Setter private String homeworkType;
    @UtopiaSqlColumn @Getter @Setter private String studentName;
    @UtopiaSqlColumn @Getter @Setter private String clazzName;

    public static String ck_teacherId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TermBeginStudentAppRecord.class,
                new String[]{"teacherId"},
                new Object[]{teacherId},
                new Object[]{0L});
    }
}