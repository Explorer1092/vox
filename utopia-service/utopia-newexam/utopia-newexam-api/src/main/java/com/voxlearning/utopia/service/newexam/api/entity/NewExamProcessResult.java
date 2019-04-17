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
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "newexam_process_result_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171227")
public class NewExamProcessResult implements Serializable {
    private static final long serialVersionUID = 5112127035332147913L;

    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;                          // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                          // 修改时间
    private Long clazzId;                           // 班级ID
    private Long clazzGroupId;                      // 班组级ID
    private String newExamId;                       // 考试ID
    private String paperDocId;                      // 试卷ID
    private String partId;                          // 模块ID
    private Long userId;                            // 学生ID
    private String questionId;                      // 题ID
    private String questionDocId;                   // 题ID
    private Double standardScore;                   // 标准分保留四位小数
    private Double score;                           // 用户实际得分,学生口语分数 =　calculateStudentOralScore()
    private Boolean grasp;                          // 是否掌握(全对/部分对)
    private List<List<Boolean>> subGrasp;           // 作答区域的掌握情况
    private List<List<String>> userAnswers;         // 用户答案
    private List<Double> subScore;                  // 用于复合题
    private List<Double> correctSubScore;           // 子题批改分数
    private Long durationMilliseconds;              // 完成时长（单位：毫秒）
    private Subject subject;                        // 学科
    private String clientType;                      // 客户端类型:pc,mobile
    private String clientName;                      // 客户端名称:***app
    private Map<String, String> additions;          // 扩展属性
    private List<List<NewExamQuestionFile>> files;  // 主观作答类型试题的存储
    private Double correctScore;                    // 批改分数
    private Date correctAt;                         // 批改时间
    //口语部分
    private List<List<OralDetail>> oralDetails;     // 口语题详情


    /**
     * 口语题详情数据结构
     */
    @Getter
    @Setter
    @EqualsAndHashCode(of = {"audio", "macScore", "fluency", "integrity", "pronunciation"})
    public static class OralDetail implements Serializable {
        private static final long serialVersionUID = -4695541592547724198L;

        @DocumentField("audio") private String audio;  // 音频地址
        @DocumentField("macScore") private Integer macScore;  // 引擎分
        @DocumentField("fluency") private Integer fluency;    // 流利程度
        @DocumentField("integrity") private Integer integrity;// 完整度
        @DocumentField("pronunciation") private Integer pronunciation;// 发音准确度
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private String randomId = RandomUtils.nextObjectId();
        private String time;

        public ID(Date createTime) {
            this.time = Long.toString(createTime.getTime());
        }

        @Override
        public String toString() {
            return randomId + "-" + time;
        }
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(NewExamProcessResult.class, id);
    }

    public List<NewExamQuestionFile> findAllFiles() {
        if (files != null && !files.isEmpty()) {
            return files.stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<OralDetail> findOralFiles() {
        if (oralDetails != null && !oralDetails.isEmpty()) {
            return oralDetails.stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 如果有批改分数则返回老师的批改分数否则返回原始分数
     * @return
     */
    public Double processScore(){
        return correctScore != null ? correctScore : score;
    }

    /**
     * 如果有批改分数则返回老师的批改分数否则返回原始分数
     * @return
     */
    public List<Double> processSubScore(){
        return correctSubScore != null ? correctSubScore : subScore;
    }


}
