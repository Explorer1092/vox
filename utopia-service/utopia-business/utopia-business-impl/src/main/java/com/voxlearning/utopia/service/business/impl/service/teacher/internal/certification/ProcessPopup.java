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

import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;

import static com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType.TEACHER_CERTIFICATED;

/**
 * 认证后弹窗，无论是不是反复认证，都弹出
 *
 * @author RuiBao
 * @version 0.1
 * @since 4/11/2015
 */
@Named
public class ProcessPopup extends AbstractTcpProcessor {

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        User user = context.getUser();
        try {
            userAttributeServiceClient.setExtensionAttribute(user.getId(), TEACHER_CERTIFICATED);
        } catch (Exception ex) {
            logger.warn("Failed to persist UserExtensionAttribute", ex);
        }
        return context;
    }
}
