package com.voxlearning.utopia.service.wechat.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.wechat.api.WechatCodeService;
import lombok.Getter;

public class WechatCodeServiceClient {

    @Getter
    @ImportService(interfaceClass = WechatCodeService.class)
    private WechatCodeService wechatCodeService;
}
