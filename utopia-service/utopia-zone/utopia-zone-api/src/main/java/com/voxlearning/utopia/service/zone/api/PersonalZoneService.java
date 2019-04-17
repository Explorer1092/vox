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
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20150820")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface PersonalZoneService extends IPingable {

    MapMessage changeBubble(StudentDetail student, Long bubbleId);

    MapMessage changeHeadWear(Long studentId, String headWearId);

    /**
     * 取消已选用的头饰,使用原始头像
     */
    MapMessage resetHeadWear(Long studentId);

    MapMessage __purchaseBubble(StudentDetail student, Long bubbleId);

    MapMessage __changeBubble(StudentDetail student, Long bubbleId);
}
