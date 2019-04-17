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

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by tanguohong on 2016/3/24.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "newexam_process_result_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
public class JournalNewExamProcessResult implements Serializable {
    private static final long serialVersionUID = -9032088789951680948L;

    @DocumentId
    private String id;
    private String oldId;                  //newexamprocessresult ID
    @DocumentCreateTimestamp
    private Date createAt;                    // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                  // 修改时间
    private Long clazzId;              // 班级ID
    private Long clazzGroupId;               // 班组级ID
    private String newExamId;           // 考试ID
    private String paperDocId;                 // 试卷ID
    private String partId;                  // 模块ID
    private Long userId;                    // 学生ID
    private String questionId;              // 题ID
    private String questionDocId;              // 题ID
    private Double standardScore;           // 标准分保留四位小数
    private Double score;                   // 用户实际得分,学生口语分数 =　calculateStudentOralScore()
    private Boolean grasp;                  // 是否掌握(全对/部分对)
    private List<List<Boolean>> subGrasp;   // 作答区域的掌握情况
    private List<List<String>> userAnswers; // 用户答案
    private List<Double> subScore;                  // 用于复合题
    private List<Double> correctSubScore;           // 子题批改分数
    private Long durationMilliseconds;           // 完成时长（单位：毫秒）
    private Subject subject;                // 学科
    private String clientType;              // 客户端类型:pc,mobile
    private String clientName;              // 客户端名称:***app
    private List<List<NewExamQuestionFile>> files;  // 主观作答类型试题的存储
    private Double correctScore;            // 批改分数
    private Date correctAt;                 // 批改时间
    //口语部分
    private List<List<NewExamProcessResult.OralDetail>> oralDetails;   // 口语题详情
    private String env;                         // 此字段在上报前赋的值

}
