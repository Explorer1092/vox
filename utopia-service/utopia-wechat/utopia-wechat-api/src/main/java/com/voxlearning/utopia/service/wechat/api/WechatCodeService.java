package com.voxlearning.utopia.service.wechat.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.02.09")
@ServiceTimeout(timeout = 1, unit = TimeUnit.MINUTES)
@ServiceRetries
public interface WechatCodeService {

    @Async
    AlpsFuture<String> generateAccessToken(WechatType wechatType);

    @Async
    AlpsFuture<String> generateQRCode(String eventKey, WechatType wechatType);

    @Async
    AlpsFuture<String> generateF2FQrcode(String scene, WechatType wechatType);

    @Async
    AlpsFuture<String> generateJsApiTicket(WechatType wechatType);
}
