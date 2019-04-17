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

package com.voxlearning.utopia.service.business.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.api.AmbassadorManagement;
import com.voxlearning.utopia.service.business.impl.service.teacher.DeprecatedAmbassadorService;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;

@Spring
@Named
@Service(interfaceClass = AmbassadorManagement.class)
@ExposeService(interfaceClass = AmbassadorManagement.class)
public class AmbassadorManagementImpl extends SpringContainerSupport implements AmbassadorManagement {

    @Inject
    private DeprecatedAmbassadorService ambassadorService;

    @Override
    public MapMessage resignationAmbassador(TeacherDetail detail) {
        return ambassadorService.resignationAmbassador(detail);
    }

}
