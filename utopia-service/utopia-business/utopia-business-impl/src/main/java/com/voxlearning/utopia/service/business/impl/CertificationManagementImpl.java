/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.api.CertificationManagement;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherCertificationServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.Certification;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@Spring
@Named
@Service(interfaceClass = CertificationManagement.class)
@ExposeService(interfaceClass = CertificationManagement.class)
public class CertificationManagementImpl extends SpringContainerSupport implements CertificationManagement {

    @Inject private TeacherCertificationServiceImpl teacherCertificationService;

    @Override
    public byte[] getAmbassadorCertificationRawData() {
        return Certification.getAmbassadorCertificationContent();
    }

    @Override
    public boolean hasEnoughStudentsFinishedHomework(Long teacherId) {
        return teacherCertificationService.hasEnoughStudentsFinishedHomework(teacherId);
    }

    @Override
    public boolean hasEnoughStudentsBindParentMobileOrBindSelfMobile(Long teacherId) {
        return teacherCertificationService.hasEnoughStudentsBindParentMobileOrBindSelfMobile(teacherId);
    }

    @Override
    public void changeUserAuthenticationState(Long userId, AuthenticationState authenticationState,
                                              Long operatorId, String operatorName) {
        teacherCertificationService.changeUserAuthenticationState(userId, authenticationState,
                operatorId, operatorName);
    }

    @Override
    public Map<Long, Boolean> hasEnoughStudentsFinishHomeworkByTeacherIds(Collection<Long> teacherIds) {
        return teacherCertificationService.hasEnoughStudentsFinishHomeworkByTeacherIds(teacherIds);
    }

    @Override
    public Map<Long, Boolean> hasEnoughStudentsBindParentMobileOrStudentsBindSelfMobile(Collection<Long> teacherIds) {
        return teacherCertificationService.hasEnoughStudentsBindParentMobileOrStudentsBindSelfMobile(teacherIds);
    }
}
