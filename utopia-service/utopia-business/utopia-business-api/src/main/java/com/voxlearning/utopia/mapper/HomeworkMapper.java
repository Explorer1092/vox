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

package com.voxlearning.utopia.mapper;

import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkState;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Homework list mapper
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-8
 */
@Data
public class HomeworkMapper implements Serializable {
    private static final long serialVersionUID = 6906238897255907273L;

    private Long clazzId;
    private String clazzName;
    private String clazzLevel;
    private int studentCount;
    private boolean graduated;

    private String homeWorkId;
    private String homeWorkName;
    private HomeworkState state;
    private boolean oldHomework;
    private String startDate;
    private String endDate;
    private Date createDatetime;
    private long normalFinishTime;
    private int practiceCount;
    private int specialCount;
    private int examPaperQuestionCount;
    private int readingPracticeCount; //英语阅读练习个数
    private String fileType;   //主观作业文件类型

    private int finishCount;
    private int unfinishCount;
    private int finishPercent;

    private boolean canAssign;
    private String message;

    private String examPaperId;

    private boolean pastdue;
    private boolean emptyContentJson;

    private boolean secondTimeFlag;//首页提示1.5倍金币

    // 老师app，首页待检查列表使用
    private long startTime;
    private long endTime;
    private boolean terminated;

    // 分组信息 by changyuan.liu
    private Long groupId;
    private String groupName;

    public static HomeworkMapper bulidNeonatalMapper() {
        HomeworkMapper mapper = new HomeworkMapper();
        mapper.setState(HomeworkState.ASSIGN_HOMEWORK);
        mapper.setFinishCount(0);
        mapper.setUnfinishCount(0);
        mapper.setCanAssign(true);
        mapper.setGraduated(false);
        return mapper;
    }
}
