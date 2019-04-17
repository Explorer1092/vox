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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.OneOffPopup;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Rui.Bao
 * @since 2014-08-21 8:22 PM
 */
@Named
public class LoadStudentPopups extends AbstractStudentIndexDataLoader {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();
        context.getParam().put("popup", studentIndexPopup(student));
        return context;
    }

    private String studentIndexPopup(StudentDetail student) {
        // 学霸
        if (showOnceOnlyPopupWindow(student.getId(), UserExtensionAttributeKeyType.STUDENT_STUDY_MASTER)) {
            return "studyMaster";
        }
        // 最赞
        if (showOnceOnlyPopupWindow(student.getId(), UserExtensionAttributeKeyType.STUDENT_MOST_FAVORITE)) {
            return "mostFavorite";
        }
        return "";
    }

    private boolean showOnceOnlyPopupWindow(Long userId, UserExtensionAttributeKeyType type) {
        OneOffPopup oop = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .StudentOneOffPopupCacheManager_showOneOffPopupWindow(userId, type)
                .getUninterruptibly();
        FlightRecorder.dot("after showOnceOnlyPopupWindow " + type);
        return oop != null && oop.isShowIt();
    }
}
