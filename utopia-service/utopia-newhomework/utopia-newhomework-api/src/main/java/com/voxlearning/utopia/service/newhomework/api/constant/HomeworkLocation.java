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

package com.voxlearning.utopia.service.newhomework.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@EqualsAndHashCode(of = {"subject", "homeworkId"})
public class HomeworkLocation implements Serializable {
    private static final long serialVersionUID = -8159674646201400115L;

    @Getter @Setter private Subject subject;
    @Getter @Setter private String homeworkId;

    public static HomeworkLocation newInstance(Subject subject, String homeworkId) {
        HomeworkLocation inst = new HomeworkLocation();
        inst.subject = subject;
        inst.homeworkId = homeworkId;
        return inst;
    }

    @Override
    public String toString() {
        return subject + "-" + homeworkId;
    }

    public static boolean isValid(HomeworkLocation loc) {
        return loc != null && loc.subject != null && loc.homeworkId != null;
    }
}
