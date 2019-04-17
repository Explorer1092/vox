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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Named;
import java.util.stream.Collectors;

/**
 * 加载学生的分组信息。
 * 加载一次，多次使用。
 *
 * @author Xiaohai Zhang
 * @since Oct 9, 2015
 */
@Named
public class LoadStudentGroup extends AbstractStudentIndexDataLoader {

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();

        context.__studentGroups = groupLoaderClient.loadStudentGroups(student.getId(), false)
                .stream()
                .filter(t -> t != null)
                .filter(t -> t.getId() != null)
                .collect(Collectors.toList());

        context.__groupIds = context.__studentGroups.stream()
                .map(GroupMapper::getId)
                .filter(t -> t != null)
                .collect(Collectors.toSet());

        return context;
    }
}
