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

import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import lombok.Data;

import java.io.Serializable;

/**
 * Homework history list mapper.
 *
 * @author Jingwei Dong
 * @author Xiaohai Zhang
 * @since 2012-08-07 15:30
 */
@Data
public class HomeworkHistoryListMapper implements Serializable {
    private static final long serialVersionUID = -1901712997288935978L;

    private String checkTime;
    private String clazzName;
    private String homeworkContext;
    private String homeworkId;
    private Long clazzId;
    private String startDate;
    private String endDate;
    private String unitsContext;
    private HomeworkType homeworkType;
    private Integer totalQuestionCount;
    private String homeworkName;
    private Integer accomplishCount;
    private Integer studentCount;
}
