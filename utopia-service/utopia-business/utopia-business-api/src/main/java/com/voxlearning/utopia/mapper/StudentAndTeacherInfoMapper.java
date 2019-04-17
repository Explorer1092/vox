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

package com.voxlearning.utopia.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Data;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: dongjw
 * Date: 12-7-11
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
@Data
public class StudentAndTeacherInfoMapper implements Serializable {
    private static final long serialVersionUID = 3760104335921468899L;

    private Long studentId;
    private String studentName;
    private String studentImg;
    private Long teacherId;
    private String teacherName;
    private String teacherImg;
    private Long clazzId;
    private String clazzName;
    private Integer clazzLevel;
    private Integer clazzType;
    private Subject subject;
}
