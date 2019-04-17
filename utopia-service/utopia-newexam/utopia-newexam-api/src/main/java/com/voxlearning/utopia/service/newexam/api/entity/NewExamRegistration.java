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

package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "newexam_registration_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'newExamId':1, 'clazzId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171102")
public class NewExamRegistration implements Serializable {

    private static final long serialVersionUID = 4014177084325486100L;

    @DocumentId
    private String id;                       // 考试ID=年月-科目-考试ID-用户ID(201603-MATH-56d3587d97d60bb09cd9e927-346902335)
    private String newExamId;                // 考试ID
    private Subject subject;                 // 学科
    private Long userId;                     // 学生ID
    private String userName;                 // 学生姓名
    private NewExamType examType;            // 考试类型
    private Boolean beenCanceled;            // 是否取消报名
    private Date registerAt;                 // 报名时间
    private Integer provinceId;              // 省
    private Integer cityId;                  // 市
    private Integer regionId;                // 区
    private Long schoolId;                   // 学校ID
    private Integer clazzLevel;              // 年级
    private SchoolLevel schoolLevel;         // 学段
    private Long clazzId;                    // 班级ID
    private Long clazzGroupId;               // 班组级ID
    private Date startAt;                    // 开始时间
    private Date finishAt;                     // 完成时间
    private Date submitAt;                   // 交卷时间
    @DocumentUpdateTimestamp
    private Date updateAt;                   // 修改时间
    private Double score;                    // 实际分数
    private Double correctScore;             // 批改分数
    private Date correctAt;                 // 批改时间
    private Long durationMilliseconds;       // 完成时长(单位:毫秒)
    private String clientType;               // 客户端类型:pc,mobile
    private String clientName;               // 客户端名称:***app
    private String paperId;                  // 试卷ID

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"month", "subject", "eid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private String month;
        private Subject subject;
        private String eid;
        private String userId;

        @Override
        public String toString() {
            return month + "-" + subject + "-" + eid + "-" + userId;
        }
    }

    public ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = id.split("-");
        if (segments.length != 4) return null;
        String month = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String eid = segments[2];
        String uid = segments[3];
        return new ID(month, subject, eid, uid);
    }

    public double processScore(int totalScore) {
        double s;
        if (correctScore != null) {
            s = correctScore;
        } else {
            s = SafeConverter.toDouble(score);
        }
        if (s > totalScore) {
            s = totalScore;
        }
        return s;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(NewExamRegistration.class, id);
    }

    public static String ck_newExam(String newExamId) {
        return CacheKeyGenerator.generateCacheKey(NewExamRegistration.class,
                new String[]{"EID"},
                new Object[]{newExamId});
    }

    public static String ck_eIdAndClazzId(String newExamId, Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(NewExamRegistration.class,
                new String[]{"EID", "CLAZZID"},
                new Object[]{newExamId, clazzId});
    }
}
