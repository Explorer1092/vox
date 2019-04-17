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

package com.voxlearning.utopia.service.ambassador.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.service.ambassador.api.SchoolAmbassadorService;
import com.voxlearning.utopia.service.ambassador.impl.persistence.SchoolAmbassadorPersistence;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.ambassador.impl.service.SchoolAmbassadorServiceImpl")
@ExposeService(interfaceClass = SchoolAmbassadorService.class)
public class SchoolAmbassadorServiceImpl extends SpringContainerSupport implements SchoolAmbassadorService {

    @Inject private SchoolAmbassadorPersistence schoolAmbassadorPersistence;

    @Override
    public AlpsFuture<SchoolAmbassador> loadSchoolAmbassadorByUserId(Long userId) {
        if (userId == null) {
            return new ValueWrapperFuture<>(null);
        }
        return new ValueWrapperFuture<>(schoolAmbassadorPersistence.findByUserId(userId));
    }

    @Override
    public AlpsFuture<SchoolAmbassador> insertSchoolAmbassador(SchoolAmbassador schoolAmbassador) {
        if (schoolAmbassador == null) {
            return new ValueWrapperFuture<>(null);
        }
        schoolAmbassadorPersistence.insert(schoolAmbassador);
        return new ValueWrapperFuture<>(schoolAmbassador);
    }
}
