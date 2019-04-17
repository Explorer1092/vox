/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.feedback.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroup;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroupDetail;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.06.06")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DislocationGroupService {

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<DislocationGroupDetail> loadDislocationGroupDetailByGroupId(Long groupId);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<DislocationGroupDetail>> loadDislocationGroupDetailsByRealSchoolId(Long realSchoolId);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<DislocationGroupDetail>> loadDislocationGroupDetailsByTime(Date beginTime, Date endTime);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<MapMessage> createDislocationGroup(DislocationGroup dislocationGroup);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<MapMessage> disableDislocationGroupByGroupId(Long groupId, String operationNotes, String operator);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<MapMessage> updateDislocationGroup(DislocationGroup dislocationGroup);
}
