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
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Jia HuanYin
 * @since 2015/6/24
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolDetailBean implements Serializable, Comparable {
    private static final long serialVersionUID = -8970058977153298903L;

    private School school;
    private SchoolType type;
    private int teacherCount;
    private Collection<Teacher> ambassadors;

    public SchoolDetailBean(School school, SchoolType type, int teacherCount, Collection<Teacher> ambassadors) {
        this.school = school;
        this.type = type;
        this.teacherCount = teacherCount;
        this.ambassadors = ambassadors;
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof SchoolDetailBean)) {
            return -1;
        }
        SchoolDetailBean bean = (SchoolDetailBean) other;
        return bean.teacherCount - this.teacherCount;
    }
}
