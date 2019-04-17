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

package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 阿分题学习计划推题记录
 * 使用userId取1000模来切表，开发测试环境模2
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-09-09 12:54PM
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_LEARNING_PLAN_PUSH_EXAMINATION_HISTORY_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170725")
public class AfentiLearningPlanPushExamHistory implements CacheDimensionDocument {
    private static final long serialVersionUID = 5322565913038095391L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    private Date createtime;
    @DocumentUpdateTimestamp
    private Date updatetime;
    private Long userId;                // 用户ID
    private Integer rank;               // 关卡
    private String knowledgePoint;      // 知识点
    private String examId;              // 题目ID
    private Integer rightNum;           // 正确数
    private Integer errorNum;           // 错误数
    private String pattern;             // 子题型（单个知识点题型统计）
    private String scoreCoefficient;    // 分数系数
    private String newBookId;           // 课本ID
    private String newUnitId;           // 单元ID
    private String subject;             // 学科

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"UID", "S"}, new Object[]{userId, subject}, new Object[]{null, ""}),
                newCacheKey(new String[]{"UID", "NBID"}, new Object[]{userId, newBookId}, new Object[]{null, ""})
        };
    }

    public int increaseErrorNum() {
        errorNum = SafeConverter.toInt(errorNum) + 1;
        return errorNum;
    }
}
