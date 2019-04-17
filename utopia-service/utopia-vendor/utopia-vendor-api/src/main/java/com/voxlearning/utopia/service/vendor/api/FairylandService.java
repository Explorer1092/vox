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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.PopupTitle;
import com.voxlearning.utopia.service.vendor.api.constant.StudentFairylandMessageType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.FairylandProductUrl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.zhang.a
 * @since 16-8-26
 */

@ServiceVersion(version = "20181015")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@Deprecated
public interface FairylandService {

    MapMessage updateFairylandProduct(Long id, FairylandProduct fairylandProduct);

    MapMessage insertFairylandProduct(FairylandProduct fairylandProduct);

    MapMessage deleteFairylandProduct(Long id);

    MapMessage isExistNewMessageAndPopupTitle(StudentDetail studentDetail);

    MapMessage saveOpenAppMessage(List<Long> userIds, String title, Long expiredTime, PopupTitle popupTitle,
                                  String appKey,String content,Map<String,Object> extInfo);

    MapMessage saveAppMessage(List<Long> userIds, String title, String linkUrl, Integer linkType,
                              StudentFairylandMessageType type, Long expiredTime, PopupTitle popupTitle,
                              String appKey,String content,Map<String,Object> extInfo);

    // 在学生app的课外乐园webview中打开应用需要的参数
    @Async
    AlpsFuture<List<FairylandProductUrl>> fetchStudentAppFairylandProductUrl();
    boolean isExistRedDot(StudentDetail studentDetail);
}
