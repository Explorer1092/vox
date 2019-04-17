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

package com.voxlearning.utopia.service.wechat.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.wechat.api.WechatCodeService;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.consumer.helpers.WechatCodeManager;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.wechat.impl.service.WechatCodeServiceImpl")
@ExposeService(interfaceClass = WechatCodeService.class)
public class WechatCodeServiceImpl extends SpringContainerSupport implements WechatCodeService {

    @Inject private WechatCodeManager wechatCodeManager;

    @Override
    public AlpsFuture<String> generateAccessToken(WechatType wechatType) {
        String ret = wechatCodeManager.generateAccessToken(wechatType);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<String> generateQRCode(String eventKey, WechatType wechatType) {
        String ret = wechatCodeManager.generateQRCode(eventKey, wechatType);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<String> generateF2FQrcode(String scene, WechatType wechatType) {
        String ret = wechatCodeManager.generateF2FQrcode(scene, wechatType);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<String> generateJsApiTicket(WechatType wechatType) {
        String ret = wechatCodeManager.generateJsApiTicket(wechatType);
        return new ValueWrapperFuture<>(ret);
    }
}
