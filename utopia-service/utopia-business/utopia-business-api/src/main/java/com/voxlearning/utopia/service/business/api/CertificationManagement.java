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

package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20160308")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
@CyclopsMonitor("utopia")
public interface CertificationManagement extends IPingable {

    byte[] getAmbassadorCertificationRawData();

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    boolean hasEnoughStudentsFinishedHomework(Long teacherId);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    boolean hasEnoughStudentsBindParentMobileOrBindSelfMobile(Long teacherId);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    void changeUserAuthenticationState(Long userId, AuthenticationState authenticationState,
                                       Long operatorId, String operatorName);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    Map<Long, Boolean> hasEnoughStudentsFinishHomeworkByTeacherIds(Collection<Long> teacherIds);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    Map<Long, Boolean> hasEnoughStudentsBindParentMobileOrStudentsBindSelfMobile(Collection<Long> teacherIds);
}
