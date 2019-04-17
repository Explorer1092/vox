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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.user.api.entities.UserExtensionAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;

import static com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType.TEACHER_CERTIFICATED;

/**
 * 教师首页弹窗，一次性的和非一次性的都在这里
 *
 * @author RuiBao
 * @version 0.1
 * @since 14-3-27
 */
@Named
public class LoadPopup extends AbstractTeacherIndexDataLoader {

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        FlightRecorder.dot("LP_START");
        if (!context.isSkipNextAll()) {
            // 有顺序的弹窗
            context.getParam().put("popup", sequentialPopup(context));
        }
        FlightRecorder.dot("LP_END");
        return context;
    }

    private String sequentialPopup(TeacherIndexDataContext context) {
        Teacher teacher = context.getTeacher();
        // 教师认证(只有认证教师才能弹出这个弹窗，一次)
        if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            UserExtensionAttribute attribute = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                    .type(TEACHER_CERTIFICATED)
                    .findFirst();
            if (attribute != null) {
                MapMessage message;
                try {
                    message = userAttributeServiceClient.deleteExtensionAttribute(attribute);
                } catch (Exception ex) {
                    logger.error("Failed to delete user extension attribute [id={}]", attribute.getId(), ex);
                    message = MapMessage.errorMessage();
                }
                if (message.isSuccess()) {
                    return "certificated";
                }
            }

        }
        // opo弹窗
        return "";
    }
}
