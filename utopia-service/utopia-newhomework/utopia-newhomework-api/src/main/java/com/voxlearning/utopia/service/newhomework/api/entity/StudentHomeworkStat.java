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

package com.voxlearning.utopia.service.newhomework.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.*;

import java.io.Serializable;

/**
 * 学生作业次数统计表，teacherId-studentId-clazzId为联合unique索引
 * WARNING：认证专用！！！
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @version 0.1
 * @serial
 * @since 14-1-13
 */
@Getter
@Setter
@DocumentTable(table = "VOX_STUDENT_HOMEWORK_STAT")
@DocumentConnection(configName = "homework")
@UtopiaCacheExpiration(3600)
public class StudentHomeworkStat extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 3225186633014819484L;

    @UtopiaSqlColumn(name = "TEACHER_ID") @NonNull private Long teacherId;
    @UtopiaSqlColumn(name = "STUDENT_ID") @NonNull private Long studentId;
    @UtopiaSqlColumn(name = "CLAZZ_ID") @NonNull private Long clazzId;
    @UtopiaSqlColumn(name = "FINISH_HOMEWORK_COUNT") private Long finishHomeworkCount;
    @UtopiaSqlColumn(name = "FINISH_QUIZ_COUNT") private Long finishQuizCount;
    @UtopiaSqlColumn(name = "FINISH_SUBJECTIVE_COUNT") private Long finishSubjectiveCount;
    @UtopiaSqlColumn(name = "FINISH_ORAL_COUNT") private Long finishOralCount;
    @UtopiaSqlColumn(name = "FINISH_O2O_OFFLINE_COUNT") private Long finishO2OOfflineCount;//线下O2O扫码听力
    @UtopiaSqlColumn(name = "FINISH_O2O_ONLINE_COUNT") private Long finishO2OOnlineCount;//线上O2O练习册听力
    @UtopiaSqlColumn(name = "FINISH_VH_COUNT") private Long finishVhCount;

    public static String ck_clazzId(Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(StudentHomeworkStat.class, "C", clazzId);
    }

    public static String ck_teacherId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(StudentHomeworkStat.class, "T", teacherId);
    }

    public static String ck_teacherId_clazzId(Long teacherId, Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(StudentHomeworkStat.class,
                new String[]{"T", "C"},
                new Object[]{teacherId, clazzId});
    }

    public static String ck_teacherId_clazzId_studentId(Long teacherId, Long clazzId, Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                StudentHomeworkStat.class,
                new String[]{"T", "C", "S"},
                new Object[]{teacherId, clazzId, studentId});
    }

    // #15398 老师认证标准中作业定义修正 8名学生完成3次作业的“作业”定义与学生认证的“作业”定义同步，只计算普通作业和测验次数。
    @JsonIgnore
    public long getNormalHomeworkCount() {
        return (this.finishHomeworkCount == null ? 0 : this.finishHomeworkCount) + (this.finishQuizCount == null ? 0 : this.finishQuizCount);
    }

    /**
     * 不要改变这里的行为
     */
    public String toUnique() {
        return (teacherId == null ? 0 : teacherId) + "-" + (clazzId == null ? 0 : clazzId) + "-" + (studentId == null ? 0 : studentId);
    }

    public static StudentHomeworkStat mockInstance() {
        StudentHomeworkStat inst = new StudentHomeworkStat();
        inst.teacherId = 0L;
        inst.clazzId = 0L;
        inst.studentId = 0L;
        return inst;
    }

    @Data
    @NoArgsConstructor
    public static class DataMapper implements Serializable {
        private static final long serialVersionUID = 1863521673748732503L;
        Long studentId;
        Long normalHomeworkCount;
    }
}
