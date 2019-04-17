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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yhh on 2016/4/19.
 * 学生表
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "rpt_mock_exam_student_day")
public class RptMockExamStudentDay implements Serializable {
    private static final long serialVersionUID = -5064982367259763794L;
    @DocumentId
    private String id;
    @DocumentField("doc_id")
    private String doc_id;
    @DocumentField("exam_id")
    private String exam_id;
    @DocumentField("student_id")
    private Integer student_id;
    @DocumentField("student_name")
    private String student_name;
    @DocumentField("teacher_id")
    private Integer teacher_id;
    @DocumentField("teacher_name")
    private String teacher_name;
    @DocumentField("group_id")
    private Integer group_id;
    @DocumentField("class_id")
    private Integer class_id;
    @DocumentField("class_name")
    private String class_name;
    @DocumentField("school_id")
    private Integer school_id;
    @DocumentField("school_name")
    private String school_name;
    @DocumentField("county_id")
    private Integer county_id;
    @DocumentField("city_id")
    private Integer city_id;
    @DocumentField("city_name")
    private String city_name;
    @DocumentField("province_id")
    private Integer province_id;
    @DocumentField("province_name")
    private String province_name;
    @DocumentField("total_score")
    private Double total_score;
    @DocumentField("total_duration")
    private Double total_duration;
    @DocumentField("parts_score_duration")
    private String parts_score_duration;
    @DocumentField("dt")
    private String dt;
    @DocumentField("answer")
    private String answer;
    @DocumentField("submitAt")
    private Boolean submitAt;
    @DocumentField("correct_total_score")
    private Double correctTotalScore;
}
