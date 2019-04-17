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

package com.voxlearning.utopia.service.vendor.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.FairylandService;
import com.voxlearning.utopia.service.vendor.api.constant.PopupTitle;
import com.voxlearning.utopia.service.vendor.api.constant.StudentFairylandMessageType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.FairylandProductUrl;

import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-8-26
 */
@Deprecated
public class FairylandServiceClient implements FairylandService {

    @ImportService(interfaceClass = FairylandService.class)
    private FairylandService fairylandService;

    @Override
    @Deprecated
    public MapMessage updateFairylandProduct(Long id, FairylandProduct fairylandProduct) {
        return fairylandService.updateFairylandProduct(id, fairylandProduct);
    }

    @Override
    @Deprecated
    public MapMessage insertFairylandProduct(FairylandProduct fairylandProduct) {
        return fairylandService.insertFairylandProduct(fairylandProduct);
    }

    @Override
    @Deprecated
    public MapMessage deleteFairylandProduct(Long id) {
        return fairylandService.deleteFairylandProduct(id);
    }

    @Override
    @Deprecated
    public MapMessage isExistNewMessageAndPopupTitle(StudentDetail studentDetail) {
        return fairylandService.isExistNewMessageAndPopupTitle(studentDetail);
    }

    @Override
    @Deprecated
    public MapMessage saveOpenAppMessage(List<Long> userIds, String title, Long expiredTime,
                                         PopupTitle popupTitle, String appKey, String content, Map<String, Object> extInfo) {
        return fairylandService.saveOpenAppMessage(userIds, title, expiredTime, popupTitle, appKey, content, extInfo);
    }

    @Override
    @Deprecated
    public MapMessage saveAppMessage(List<Long> userIds, String title, String linkUrl, Integer linkType, StudentFairylandMessageType type,
                                     Long expiredTime, PopupTitle popupTitle, String appKey, String content, Map<String, Object> extInfo) {
        return fairylandService.saveAppMessage(userIds, title, linkUrl, linkType, type, expiredTime, popupTitle, appKey, content, extInfo);
    }

    @Override
    @Deprecated
    public AlpsFuture<List<FairylandProductUrl>> fetchStudentAppFairylandProductUrl() {
        return fairylandService.fetchStudentAppFairylandProductUrl();
    }

    @Override
    @Deprecated
    public boolean isExistRedDot(StudentDetail studentDetail) {
        return fairylandService.isExistRedDot(studentDetail);
    }
}
