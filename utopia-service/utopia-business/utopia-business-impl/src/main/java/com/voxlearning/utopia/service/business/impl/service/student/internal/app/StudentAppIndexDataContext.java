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

package com.voxlearning.utopia.service.business.impl.service.student.internal.app;

import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataContext;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

/**
 * 学生端App data context
 * Created by Shuai Huan on 2016/1/14.
 */
public class StudentAppIndexDataContext extends StudentIndexDataContext {
    private static final long serialVersionUID = -4693841960242484109L;

    public final String ver;
    public final String sys;

    public StudentAppIndexDataContext(StudentDetail student, String ver, String sys) {
        super(student);
        this.ver = ver;
        this.sys = sys;
    }
}
