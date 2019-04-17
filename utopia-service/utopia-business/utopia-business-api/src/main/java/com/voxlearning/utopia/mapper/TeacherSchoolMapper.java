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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * Teacher school mapper data structure.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-04-03 10:17
 */
@Data
public class TeacherSchoolMapper implements Serializable {
    private static final long serialVersionUID = -928747402885135881L;

    private String teacherName;
    private String schoolName;
    private String province;
    private String city;
    private String county;

    @JsonIgnore
    public String getLocation() {
        return StringUtils.defaultString(province) + StringUtils.defaultString(city) + StringUtils.defaultString(county);
    }
}
