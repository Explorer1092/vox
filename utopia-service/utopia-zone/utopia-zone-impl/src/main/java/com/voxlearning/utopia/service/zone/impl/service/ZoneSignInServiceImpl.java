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

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.zone.api.ZoneSignInService;
import com.voxlearning.utopia.service.zone.data.SignInContext;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import com.voxlearning.utopia.service.zone.impl.support.ZoneSignInCacheManager;

import javax.inject.Inject;
import javax.inject.Named;

@Spring
@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneSignInServiceImpl")
@ExposeService(interfaceClass = ZoneSignInService.class)
public class ZoneSignInServiceImpl extends SpringContainerSupport
        implements ZoneSignInService {

    @Inject private StudentInfoPersistence studentInfoPersistence;
    @Inject private ZoneSignInCacheManager zoneSignInCacheManager;

    @Override
    public AlpsFuture<Boolean> checkSignIn(SignInContext context) {
        Long studentId = context.getStudentId();
        Long clazzId = context.getClazzId();
        boolean ret = zoneSignInCacheManager.alreadySignedIn(studentId, clazzId);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> finishSignIn(SignInContext context) {
        Long studentId = context.getStudentId();
        Long clazzId = context.getClazzId();
        studentInfoPersistence.createOrIncreaseSignInCountByOne(studentId);
        zoneSignInCacheManager.setSignedIn(studentId, clazzId);
        return new ValueWrapperFuture<>(true);
    }
}
