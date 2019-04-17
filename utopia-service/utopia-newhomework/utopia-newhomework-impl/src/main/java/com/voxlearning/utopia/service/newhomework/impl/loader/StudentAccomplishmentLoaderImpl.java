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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.newhomework.api.StudentAccomplishmentLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkLocation;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.dao.StudentHomeworkAccomplishmentPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
@Service(interfaceClass = StudentAccomplishmentLoader.class)
@ExposeService(interfaceClass = StudentAccomplishmentLoader.class)
public class StudentAccomplishmentLoaderImpl implements StudentAccomplishmentLoader {

    @Inject private StudentHomeworkAccomplishmentPersistence studentHomeworkAccomplishmentPersistence;

    @Override
    public List<StudentHomeworkAccomplishment> findByLocation(HomeworkLocation location) {
        return studentHomeworkAccomplishmentPersistence.findByLocation(location);
    }

    @Override
    public List<String> findByAccomplishTime(Date startDate, Date endDate, Subject subject) {
        return studentHomeworkAccomplishmentPersistence.findByAccomplishTime(startDate, endDate, subject);
    }

    @Override
    public List<StudentHomeworkAccomplishment> findByStudentIdAndAccomplishTime(Long studentId, Date dayStart) {
        return studentHomeworkAccomplishmentPersistence.findByStudentIdAndAccomplishTime(studentId, dayStart);
    }

    @Override
    public Integer countByStudentIdAndAccomplishTime(Long studentId, Date start, Date end) {
        return studentHomeworkAccomplishmentPersistence.countByStudentIdAndAccomplishTime(studentId, start, end);
    }

    @Override
    public List<Long> findUserIdByAccomplishTime(Date startDate, Date endDate) {
        return studentHomeworkAccomplishmentPersistence.findUserIdByAccomplishTime(startDate, endDate);
    }

    @Override
    public void deletes(List<Long> ids) {
        studentHomeworkAccomplishmentPersistence.deletes(ids);
    }
}
