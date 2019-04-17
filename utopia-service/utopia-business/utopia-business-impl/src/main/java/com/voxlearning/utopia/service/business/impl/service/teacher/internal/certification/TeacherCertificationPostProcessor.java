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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author RuiBao
 * @version 0.1
 * @since 4/11/2015
 */
@Named
@Slf4j
@NoArgsConstructor
public class TeacherCertificationPostProcessor extends SpringContainerSupport {

    @Inject private ProcessPopup processPopup;
    @Inject private ProcessCertificationReward processCertificationReward;
    @Inject private ProcessCertificationLatest processCertificationLatest;
    @Inject private ProcessMentor processMentor;
    @Inject private ProcessInvitationReward processInvitationReward;
    @Inject private ProcessInitTeacherPrivilegeCache processInitTeacherPrivilegeCache;

    private final List<AbstractTcpProcessor> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        chains.add(processCertificationReward);
        chains.add(processInvitationReward);
        chains.add(processMentor);
        chains.add(processInitTeacherPrivilegeCache);
        chains.add(processPopup);
        chains.add(processCertificationLatest);
    }

    public void process(final TeacherCertificationContext context) {
        TeacherCertificationContext contextForUse = context;
        for (AbstractTcpProcessor unit : chains) {
            contextForUse = unit.process(contextForUse);
        }
    }
}
