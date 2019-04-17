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

package com.voxlearning.utopia.service.business.api.entity.embedded;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/13.
 * 教研员技能数据
 */
@Data
public class RSSkillData implements Serializable {
    private static final long serialVersionUID = 5236723023661763353L;

    private Double rate;
    private Integer num;
    private String type;
    private Integer pavg;
}
