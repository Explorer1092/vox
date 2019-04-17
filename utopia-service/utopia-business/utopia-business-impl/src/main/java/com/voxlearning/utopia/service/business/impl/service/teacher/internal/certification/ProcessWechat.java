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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;

import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证后微信，无论是不是反复认证，都弹出
 * @author RuiBao
 * @version 0.1
 * @since 5/14/2015
 */
@Named
public class ProcessWechat extends AbstractTcpProcessor {

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        User teacher = context.getUser();
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
        if (ua != null) {
            Map<String, Object> extensions = new HashMap<>();
            extensions.put("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(ua.getId()));
            wechatServiceClient.processWechatNotice(
                    WechatNoticeProcessorType.TeacherCertificateRemindNotice,
                    teacher.getId(),
                    extensions,
                    WechatType.TEACHER
            );
        }
        return context;
    }
}
