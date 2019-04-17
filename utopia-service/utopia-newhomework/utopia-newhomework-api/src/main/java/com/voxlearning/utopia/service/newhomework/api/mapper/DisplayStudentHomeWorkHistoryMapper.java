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

package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DisplayStudentHomeWorkHistoryMapper.java
 *
 * @author Jingwei Dong
 * @author Xiaohai Zhang
 * @serial
 * @since 2011-8-23
 */
@Data
public class DisplayStudentHomeWorkHistoryMapper implements Serializable {
    private static final long serialVersionUID = -2015887354073901989L;

    private Long userId;
    private String userName;
    private Long clazzId;
    private String clazzName;
    private String homeworkId;
    private String startDate;
    private String endDate;
    private Boolean checked;
    private Boolean homeworkTerminated;
    private String bookName;
    private Integer homeworkScore;
    private String note = "";
    private String commentTeacherName = "";
    private String commentTeacherImg = "";
    private String state; // UNFINISHED表示未完成需要补做，FINISHED表示已完成并显示分数，UNCHECKED表示已完成但老师未检查，不显示分数
    private Long createTime;
    private String unitNames;
    private Boolean finished;
    private String submitTime;
    private String homeworkType; // 作业类型
//    private String isTermEnd;
    private Subject subject;
    private String correctedType; //作业批改状态
    private List<String> types; //作业包含的作业形式
    // 是否是限时口算训练
    private Boolean timeLimit = false;
    private HomeworkCorrectStatus hCorrectStatus;//作业订正状态
}
