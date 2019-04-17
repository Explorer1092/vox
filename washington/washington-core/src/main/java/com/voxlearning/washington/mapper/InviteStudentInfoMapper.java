/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * @File InviteStudentInfoMapper.java
 * @Date 2012-4-5
 * @Author RuiBao
 * @Description
 */
@Data
public class InviteStudentInfoMapper implements Serializable {
    private static final long serialVersionUID = 2163524982516540862L;

    private String studentId;    // 学生id
    private String etStudentId;    // 学生id
    private String parentPhone;    // 学生家长电话
    private String studentName;    // 学生姓名
    private String etStudentName;    // 学生姓名
    private String photo;        // 学生头像
    private int finishHomeworkCountNum;    //完成作业次数
    private String studentPassword;     //学生密码
    private String etStudentPassword;     //加载信息
    private Long clazzId;                   //班级id
    private int gold;            //金币数量
    private int homeworkCount;    //作业数量
    private int exerciseCount;    //练习数量
    private String clazzName;    //班级名称
    private boolean choose;     //是否目前是被选中状态

}
