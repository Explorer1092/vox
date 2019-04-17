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

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.middleschool.MiddleSchoolHomeworkCrmHistory;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20161207")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface MiddleSchoolHomeworkLoader extends IPingable {

    @Idempotent
    List<MiddleSchoolHomeworkCrmHistory> loadGroupRecentHomeworkList(Collection<GroupMapper> groups, Integer day);

}
