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

package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/6/24
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolTeacherBean implements Serializable, Comparable {
    private static final long serialVersionUID = -506992706697332834L;

    private Long schoolId;
    private String schoolName;
    private SchoolType schoolType;
    private int unusualTeacherCount;

    public SchoolTeacherBean(Long schoolId, String schoolName, SchoolType schoolType) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.schoolType = schoolType;
    }

    public void increaseUnusual() {
        this.unusualTeacherCount++;
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof SchoolTeacherBean)) {
            return -1;
        }
        SchoolTeacherBean bean = (SchoolTeacherBean) other;
        return bean.unusualTeacherCount - this.unusualTeacherCount;
    }
}
