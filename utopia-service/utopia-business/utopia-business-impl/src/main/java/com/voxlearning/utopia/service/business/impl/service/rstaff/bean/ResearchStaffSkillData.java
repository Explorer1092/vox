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

package com.voxlearning.utopia.service.business.impl.service.rstaff.bean;

import com.voxlearning.utopia.service.business.api.entity.embedded.RSSkillData;
import lombok.Data;

import java.util.List;

/**
 * Created by Changyuan on 2015/1/29.
 */
@Data
public class ResearchStaffSkillData {
    String name;

    Long stuNum;

    private List<RSSkillData> skills;
}
