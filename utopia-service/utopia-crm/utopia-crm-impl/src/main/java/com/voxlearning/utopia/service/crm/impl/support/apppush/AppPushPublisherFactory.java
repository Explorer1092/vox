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

package com.voxlearning.utopia.service.crm.impl.support.apppush;



import com.voxlearning.utopia.service.crm.impl.support.apppush.publisher.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

@Named
public class AppPushPublisherFactory {

    @Inject private ParentAppPushPublisher parentAppPushPublisher;
    @Inject private StudentAppPushPublisher studentAppPushPublisher;
    @Inject private TeacherAppPushPublisher teacherAppPushPublisher;

    public AppPushPublisher getPublisher(String sendApp) {
        Objects.requireNonNull(sendApp);
        switch (sendApp) {
            case "parent":
                return parentAppPushPublisher;
            case "student":
                return studentAppPushPublisher;
            case "teacher":
                return teacherAppPushPublisher;
            default:
                return null;
        }
    }
}
