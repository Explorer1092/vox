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

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20161221")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface PersonalZoneLoader extends IPingable {

    /*@Idempotent
    default StudentInfo loadStudentInfo(Long studentId) {
        if (studentId == null) {
            return null;
        }
        return loadStudentInfos(Collections.singleton(studentId)).get(studentId);
    }*/

    StudentInfo loadStudentInfo(Long studentId);

    Map<Long, StudentInfo> loadStudentInfos(Collection<Long> studentIds);

    /**
     * 检查学生是否已经拥有了指定的有效气泡
     *
     * @param studentId 学生ID
     * @param bubbleId  气泡ID
     * @return true or false
     */
    boolean hasBubble(Long studentId, Long bubbleId);

    MapMessage showBubbles(Long studentId);

    void __deleteBubbles(Collection<Long> bagIds, Long userId);

}
