package com.voxlearning.utopia.service.ai.impl.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;

public class WechatConfig {

    public static String getBaseSiteUrl() {
        return RuntimeMode.lt(Mode.STAGING) ? "https://wechat.test.17zuoye.net" : "https://wechat.17zuoye.com";
    }
}
