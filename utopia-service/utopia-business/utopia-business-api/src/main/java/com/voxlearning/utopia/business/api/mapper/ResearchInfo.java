/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.utopia.service.business.api.entity.BizMarketingSchoolData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResearchInfo implements Serializable {

    private static final long serialVersionUID = 4037152833406942296L;
    private int teacherCount;                   // 认证老师总数或者增量
    private int studentCount;                   // 学生总数或者增量
    private int authStudentCount;              //认证学生数量
    private int rstaffAuthCount;                //教研员累计认证
    private List<BizMarketingSchoolData> details;   // 老师使用情况明细
}
