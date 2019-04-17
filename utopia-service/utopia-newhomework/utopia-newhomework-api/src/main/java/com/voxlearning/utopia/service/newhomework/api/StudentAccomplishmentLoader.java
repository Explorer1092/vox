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

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkLocation;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkAccomplishment;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180903")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
@CyclopsMonitor("utopia")
public interface StudentAccomplishmentLoader extends IPingable {

    @Idempotent
    List<StudentHomeworkAccomplishment> findByLocation(HomeworkLocation location);

    @Idempotent
    List<String> findByAccomplishTime(Date startDate, Date endDate, Subject subject);

    @Idempotent
    List<StudentHomeworkAccomplishment> findByStudentIdAndAccomplishTime(Long studentId, Date dayStart);

    @Idempotent
    Integer countByStudentIdAndAccomplishTime(Long studentId, Date start, Date end);

    @Idempotent
    List<Long> findUserIdByAccomplishTime(Date startDate, Date endDate);

    @ServiceRetries(retries = 0)
    void deletes(List<Long> ids);
}
