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

package com.voxlearning.utopia.agent.persist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author shiwei.liao
 * @since 2015/7/20.
 */
@Setter
@Getter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_USER_CLUE")
public class AgentUserClue extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -2445371768065681821L;

    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private String userName;
    @UtopiaSqlColumn private Integer regionCode;
    @UtopiaSqlColumn private String regionName;
    @UtopiaSqlColumn private Long schoolId;
    @UtopiaSqlColumn private String schoolName;
    @UtopiaSqlColumn private String schoolAddress;
    @UtopiaSqlColumn private String keyContactName;   //关键联系人姓名
    @UtopiaSqlColumn private String keyContactPhone; //关键联系人电话
    @UtopiaSqlColumn private Integer totalStudentCount;
    @UtopiaSqlColumn private Integer highLevelCount;
    @UtopiaSqlColumn private Integer lowLevelCount;
    @UtopiaSqlColumn private Integer englishTeacherCount;
    @UtopiaSqlColumn private Integer mathTeacherCount;
    @UtopiaSqlColumn private Integer chineseTeacherCount;
    @UtopiaSqlColumn private Integer provideType;
    @UtopiaSqlColumn private String providePhone;
    @UtopiaSqlColumn private Boolean disabled;

    @JsonIgnore
    public boolean isDisabledTrue() {
        return disabled != null && disabled;
    }

//    @UtopiaSqlColumn private Integer regionCode;              // 所在区CODE
//    @UtopiaSqlColumn private String schoolName;               // 学校名
//    @UtopiaSqlColumn private String shortName;                // 学校简称
//    @UtopiaSqlColumn private Integer schoolPhase;             // 学校阶段（1小学，2中学）
//    @UtopiaSqlColumn private Integer schoolLevel;             // 学校等级，1表示重点学校，2表示非重点学校
//    @UtopiaSqlColumn private Integer schoolType;              // 学校类型（1国家规定学校，2自定义学校, 3私立学校)
//    @UtopiaSqlColumn private String address;                  // 学校地址
//    @UtopiaSqlColumn private String photoUrl;                 // 学校照片GFS地址
//    @UtopiaSqlColumn private Integer englishTeacherCount;     // 英语老师数量
//    @UtopiaSqlColumn private Integer mathTeacherCount;        // 数学老师数量
//    @UtopiaSqlColumn private Integer chineseTeacherCount;     // 语文老师数量
//    @UtopiaSqlColumn private Integer firstGradeClassCount;        // 一年级班级数量
//    @UtopiaSqlColumn private Integer firstGradeAvgStudentCount;   // 一年级班均人数
//    @UtopiaSqlColumn private Integer secondGradeClassCount;       // 二年级班级数量
//    @UtopiaSqlColumn private Integer secondGradeAvgStudentCount;  // 二年级班均人数
//    @UtopiaSqlColumn private Integer thirdGradeClassCount;        // 三年级班级数量
//    @UtopiaSqlColumn private Integer thirdGradeAvgStudentCount;   // 三年级班均人数
//    @UtopiaSqlColumn private Integer fourthGradeClassCount;       // 四年级班级数量
//    @UtopiaSqlColumn private Integer fourthGradeAvgStudentCount;  // 四年级班均人数
//    @UtopiaSqlColumn private Integer firthGradeClassCount;        // 五年级班级数量
//    @UtopiaSqlColumn private Integer firthGradeAvgStudentCount;   // 五年级班均人数
//    @UtopiaSqlColumn private Integer sixthGradeClassCount;        // 六年级班级数量
//    @UtopiaSqlColumn private Integer sixthGradeAvgStudentCount;   // 六年级班均人数
//    @UtopiaSqlColumn private String keyContactName;               // 关键联系人姓名
//    @UtopiaSqlColumn private String keyContactPhone;              // 关键联系人电话
//    @UtopiaSqlColumn private Integer status;                      // 状态， 0：暂存， 1：提交， 2：审核完毕，生成学校，9：审核不通过
//    @UtopiaSqlColumn private Long schoolId;                       // 审核完毕，生成学校后的SCHOOL ID
//    @UtopiaSqlColumn private Long recorderId;                     // 采集者ID
//    @UtopiaSqlColumn private String recorderName;                 // 采集者姓名

}
