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

package com.voxlearning.utopia.business.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 智慧课堂--学生选择结果
 *
 * @author Maofeng Lu
 * @since 14-10-24 下午3:28
 */
@Getter
@Setter
public class SmartClazzStudentResult implements Serializable {
    private static final long serialVersionUID = -3650262783884686632L;

    private Long studentId;
    private String studentName;
    private Integer studentCode;
    private String studentAnswer;
}
