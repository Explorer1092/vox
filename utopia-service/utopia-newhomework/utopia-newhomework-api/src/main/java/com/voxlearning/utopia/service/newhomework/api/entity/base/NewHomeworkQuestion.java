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

package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class NewHomeworkQuestion implements Serializable {

    static private final List<Integer> SUBJECTIVE_TYPE_LIST = Arrays.asList(1, 2);

    private static final long serialVersionUID = -1579519984975285343L;

    private String questionId;              // 试题id
    private String similarQuestionId;       // 类题id（数学特有，用于作业错题后的错题重做）
    private Long questionVersion;           // 试题版本号（xx_online_question中的ol_updated_at属性的getTime()时间戳）
    private Double score;                   // 题目分值
    private Integer seconds;                // 建议作答时间，单位：秒

    // 下面两个属性用于特殊题型
    private List<List<Integer>> submitWay;  // 属性说明来源于内容库，提交方式，0：直接作答；1：拍照；2：录音；
    private Integer answerType;             // 用于预留主观题标识"

    // 以下属性为各个作业形式特有属性
    private String knowledgePointId;        // 知识点id（数学口算）
    private String questionBoxId;           // 精选作业包id（同步习题）；数学重点讲练测题包id即为sectionId
    private String questionBoxName;         // 精选作业包名称
    private String courseId;                // 课程id
    private String paperId;                 // 试卷id（测验）
    private List<List<Integer>> answerWay;  // 语文朗读背诵,1000朗读，1001背诵

    // 下面这个属性为期末复习特有属性
    private String sourceType;              // 题目来源（期末复习作业有三个模块数据都写到EXAM这个作业形式下，用来区分来源）

    // 下面的属性用于自学作业
    // private Double selfStudyScore;          // 用于记录自学中的错题订正题目的原始作业中分值

    @JsonIgnore
    public boolean isSubjectiveQuestion() {
        if (submitWay == null || submitWay.isEmpty()) {
            return false;
        }

        for (List<Integer> si : submitWay) {
            if (si == null || si.isEmpty()) {
                return false;
            }
            for (Integer i : si) {
                if (SUBJECTIVE_TYPE_LIST.contains(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    @JsonIgnore
    public String processAnswerWay() {
        if (answerWay == null || answerWay.isEmpty()) {
            return "";
        }
        for (List<Integer> si : answerWay) {
            if (si.contains(1001)) {
                return "背诵";
            }
        }
        return "朗读";
    }
}
