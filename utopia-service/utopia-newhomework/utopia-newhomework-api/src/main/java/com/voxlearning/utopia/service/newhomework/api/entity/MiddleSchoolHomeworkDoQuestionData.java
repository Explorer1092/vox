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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Deprecated
public class MiddleSchoolHomeworkDoQuestionData implements Serializable {

    private static final long serialVersionUID = 3237561875412885945L;

    private String questionId;
    private String wordId;
    private Integer contentTypeId;
    private Integer tagId;
    private Long duration;
    private Boolean grasp;
    private List<List<Boolean>> subGrasp;
    private List<List<String>> userAnswers;
    private Integer oralScore;
    private List<Integer> subOralScores;
    private List<Long> isRightList;

}