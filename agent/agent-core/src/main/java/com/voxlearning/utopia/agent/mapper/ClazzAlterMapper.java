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

package com.voxlearning.utopia.agent.mapper;

import com.voxlearning.utopia.service.user.api.constants.AlterationCcProcessState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Yuechen.Wang on 2016/9/9.
 */
@Getter
@Setter
public class ClazzAlterMapper implements Serializable {

    private static final long serialVersionUID = 3953835003355190274L;
    private Long recordId;
    private Long schoolId;
    private Long clazzId;
    private String clazzName;
    private String schoolName;
    private Long applicantId;
    private String applicantName; //申请人
    private String applicantSubject;
    private Integer applicantAuthState;
    private Long respondentId;
    private String respondentName;
    private String respondentSubject;
    private Integer respondentAuthState;
    private ClazzTeacherAlterationType type;
    private ClazzTeacherAlterationState state;
    private AlterationCcProcessState ccProcessState;
    private Date updateTime;
    private Long updateTimeLong;
    private Date createTime;
    private Long createTimeLong;
    private Integer orderIndex; // 排序标签
    private Boolean available; // 是否可以操作
}
