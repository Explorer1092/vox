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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

@Getter
@Setter
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "newexam_result_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'newExamId':1, 'clazzId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171102")
public class NewExamResult implements Serializable {

    private static final long serialVersionUID = 2427710397214679850L;

    @DocumentId
    private String id;                      //ID=年月-科目-考试ID-用户ID(201603-MATH-56d3587d97d60bb09cd9e927-346902335)
    private String newExamId;           // 考试ID
    private Subject subject;                // 学科
    private Long clazzId;              // 班ID
    private Long clazzGroupId;               // 班组级ID
    private Long userId;                    // 用户ID
    private LinkedHashMap<String, String> answers; // <试题DocID, 考试过程数据id(newexam_process_result的ID)>
    @DocumentCreateTimestamp
    private Date createAt;                  // 生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                  // 更新时间
    private Date finishAt;                    // 完成时间
    private Date submitAt;                    // 交卷时间
    private Double score;                   // 实际分数
    private Double correctScore;            // 批改分数
    private Date correctAt;                 // 批改时间
    private Long durationMilliseconds;      // 完成时长(单位:毫秒)
    private String clientType;              // 客户端类型:pc,mobile
    private String clientName;              // 客户端名称:***app
    private Date flightRecorderTime;        // 打点时间(每次进入考试和每次提交都会修改成当前时间，用来计算完成时长)
    private String paperId;                 // 试卷ID

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
        String hid = segments[2];
        String uid = segments[3];
        return new ID(month, subject, hid, uid);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = 2552375471484983061L;

        private String id;
        private String newExamId;
        private Subject subject;                                                        // 学科
        private Long clazzId;                                                       // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
        private Long clazzGroupId;                                                      // 班组id，有问题问长远
        private Long userId;                                                            // 用户id，根据大作业的趋势，以后做题的会变成各种角色
        private Date createAt;                                                          // 作业生成时间
        private Date updateAt;
        private Date finishAt;
        private Date submitAt;
        private Double score;
        private Double correctScore;
        private Long durationMilliseconds;
    }

    public Location toLocation() {
        Location location = new Location();
        location.id = getId();
        location.newExamId = newExamId;
        location.subject = subject;
        location.clazzId = clazzId;
        location.clazzGroupId = clazzGroupId;
        location.userId = userId;
        location.createAt = createAt;
        location.updateAt = updateAt;
        location.finishAt = finishAt;
        location.submitAt = submitAt;
        location.score = score;
        location.correctScore = correctScore;
        location.durationMilliseconds = durationMilliseconds;
        return location;
    }

    @JsonIgnore
    public String obtainPaperId(String paperId) {
        if (this.paperId != null) {
            return this.paperId;
        }
        return paperId;
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
        return CacheKeyGenerator.generateCacheKey(NewExamResult.class, id);
    }

    public static String ck_newExam(String newExamId) {
        return CacheKeyGenerator.generateCacheKey(NewExamResult.class,
                new String[]{"EID"},
                new Object[]{newExamId});
    }

    public static String ck_eIdAndClazzId(String newExamId, Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(NewExamResult.class,
                new String[]{"EID", "CLAZZID"},
                new Object[]{newExamId, clazzId});
    }


}
