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

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 老师回流数据
 * Created by Shuai Huan on 2015/6/30.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_TEACHER_CALLBACK")
@UtopiaCacheExpiration
public class AgentTeacherCallback extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 7842392020786712411L;

    @UtopiaSqlColumn private Integer provinceCode;                          // 省编码
    @UtopiaSqlColumn private String provinceName;                           // 省名称
    @UtopiaSqlColumn private Integer cityCode;                              // 市编码
    @UtopiaSqlColumn private String cityName;                               // 市名称
    @UtopiaSqlColumn private Integer countyCode;                            // 区编码
    @UtopiaSqlColumn private String countyName;                             // 区名称
    @UtopiaSqlColumn private Long schoolId;                                 // 学校ID
    @UtopiaSqlColumn private String schoolName;                             // 学校名称
    @UtopiaSqlColumn private Long teacherId;                                // 老师ID
    @UtopiaSqlColumn private String teacherName;                            // 老师姓名
    @UtopiaSqlColumn private String callbackDesc;                           // 回流原因说明
    @UtopiaSqlColumn private Integer status;                                // 回流状态, 0:初始,1:已回流
    @UtopiaSqlColumn private Date callbackDatetime;                         // 回流时间
}
