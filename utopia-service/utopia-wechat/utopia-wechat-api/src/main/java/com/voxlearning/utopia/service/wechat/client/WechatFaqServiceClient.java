package com.voxlearning.utopia.service.wechat.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.wechat.api.WechatFaqService;
import lombok.Getter;

public class WechatFaqServiceClient {

    @Getter
    @ImportService(interfaceClass = WechatFaqService.class)
    private WechatFaqService wechatFaqService;
}
