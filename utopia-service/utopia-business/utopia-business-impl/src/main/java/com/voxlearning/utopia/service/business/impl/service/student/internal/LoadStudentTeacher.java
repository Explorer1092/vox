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

import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 读取系统自建班级可关联老师
 * GroupId已经是现成的了，直接读取GroupTeacherRef获得。
 *
 * @author changyuan.liu
 * @author Xiaohai Zhang
 * @since 2015/6/23
 */
@Named
public class LoadStudentTeacher extends AbstractStudentIndexDataLoader {

    @Inject private RaikouSDK raikouSDK;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {

        Set<Long> teacherIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByGroupIds(context.__groupIds)
                .stream()
                .map(GroupTeacherTuple::getTeacherId)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        context.__teacherIds = teacherIds;

        // 读取学生关联老师
        // refer to: WEB-INF/ftl/default/studentv3/index.ftl
        // 只调用了fetchImageUrl和fetchRealname方法，因此读取user对象就足够了

        List<Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds)
//        List<User> teachers = context.__userLoaderBuffer.loadUsers(teacherIds)
                .values()
                .stream()
                .collect(Collectors.toList());

        context.getParam().put("linkedTeachers", teachers);
        return context;
    }
}
