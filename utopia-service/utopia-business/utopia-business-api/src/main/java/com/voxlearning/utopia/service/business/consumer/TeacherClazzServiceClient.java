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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.business.api.TeacherClazzService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;

import java.util.List;
import java.util.Map;

public class TeacherClazzServiceClient implements TeacherClazzService {

    @ImportService(interfaceClass = TeacherClazzService.class)
    private TeacherClazzService remoteReference;

//    @Override
//    public MapMessage findClazzWithSameName(Teacher teacher, String clazzLevel, String[] clazzNames) {
//        return remoteReference.findClazzWithSameName(teacher, clazzLevel, clazzNames);
//    }

    @Override
    public List<Map<String, Object>> getStudentNameOverlapClazzs(ClassMapper command) {
        return remoteReference.getStudentNameOverlapClazzs(command);
    }

    @Override
    public boolean upgradeClazzBook(Long clazzId, Teacher teacher) {
        return remoteReference.upgradeClazzBook(clazzId, teacher);
    }

    @Override
    public MapMessage findClazzWithSameNameForWechat(Teacher teacher, String clazzLevel, String[] clazzNames) {
        return remoteReference.findClazzWithSameNameForWechat(teacher, clazzLevel, clazzNames);
    }
}
