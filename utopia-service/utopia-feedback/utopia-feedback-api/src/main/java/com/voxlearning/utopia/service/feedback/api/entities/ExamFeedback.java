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

package com.voxlearning.utopia.service.feedback.api.entities;

import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Longlong Yu
 * @since 下午2:35,13-10-29.
 */
@DocumentTable(table = "VOX_EXAM_FEEDBACK")
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ExamFeedback implements Serializable, PrimaryKeyAccessor<Long>, TimestampTouchable {

    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateDatetime = new Date(timestamp);
    }

    public static String fetchExamFeedbackType(Integer typeValue) {
        return examFeedbackTypeMap.get(typeValue);
    }

    private static final long serialVersionUID = 3182136224546961599L;

    @UtopiaSqlColumn(primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;

    @DocumentCreateTimestamp
    @UtopiaSqlColumn private Date createDatetime;
    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private String questionId;
    @UtopiaSqlColumn private String content;
    @UtopiaSqlColumn private String type;           //反馈类型
    @UtopiaSqlColumn private Long operator;         //实际解决人
    @UtopiaSqlColumn private String state;          //反馈状态,未处理：UNTREATED ,已处理： RESOLVED
    @UtopiaSqlColumn private String itemAnalysis;   //题目分析
    @UtopiaSqlColumn private String reply;          //回复内容
    @UtopiaSqlColumn private String comment;        //备注
    @DocumentUpdateTimestamp
    @UtopiaSqlColumn private Date updateDatetime;   //更新时间

    public static final Map<Integer, String> examFeedbackTypeMap;

    static {
        examFeedbackTypeMap = new LinkedHashMap<>();
        examFeedbackTypeMap.put(1, "ExamEnglish");
        examFeedbackTypeMap.put(2, "ExamMath");
        examFeedbackTypeMap.put(3, "Afenti");
        examFeedbackTypeMap.put(4, "TEACHER_HOMEWORK_ASSIGN");     // 老师布置随堂练
        examFeedbackTypeMap.put(5, "TEACHER_EXAM_PREVIEW");        // 老师测验预览
        examFeedbackTypeMap.put(6, "STUDENT_AFENTI");              // 学生阿分题
        examFeedbackTypeMap.put(7, "STUDENT_ANSWER");              // 学生答题
        examFeedbackTypeMap.put(8, "STUDENT_REPORT");              // 学生结果页
        examFeedbackTypeMap.put(9, "STUDENT_APP_ANSWER");          // 小学学生答题_移动端
        examFeedbackTypeMap.put(10, "JUNIOR_STUDENT_APP_ANSWER");  // 中学学生答题_移动端
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ExamFeedback.class, id);
    }
}
