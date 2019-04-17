/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.persist.entity.statistics.internal;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Alex on 14-10-30.
 */
@Getter
@Setter
public class Homework implements Serializable {
    private static final long serialVersionUID = -4301273674646261059L;

    private String homeworkId;            // 作业ID
    private String homeworkName;          // 作业名
    private String homeworkCreateTime;    // 布置时间
    private String homeworkStartTime;     // 开始时间
    private String homeworkEndTime;       // 结束时间
    private String finishStudentCount;    // 完成人数
}
