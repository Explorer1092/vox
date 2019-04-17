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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "1.2")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeacherClazzService extends IPingable {
//    /**
//     * 查询重名班级，用于判断创建还是加入班级
//     */
//    MapMessage findClazzWithSameName(Teacher teacher, String clazzLevel, String[] clazzNames);

    /**
     * 获取与即将创建的班级符合某种关系的班级列表 >*_*< 。。。
     * 1.有3个及以上重名学生(重名的只算一次)
     * 2.已录入的名单不重名总数≤（班级内无姓名学生人数）
     */
    List<Map<String, Object>> getStudentNameOverlapClazzs(ClassMapper command);

    boolean upgradeClazzBook(Long clazzId, Teacher teacher);

    MapMessage findClazzWithSameNameForWechat(Teacher teacher, String clazzLevel, String[] clazzNames);
}
