package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190313")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsWechatUserLoader extends IPingable {
    /**
     *
     * @param openId
     * @param type com.voxlearning.utopia.service.ai.constant.WechatUserType
     * @return
     */
    ChipsWechatUser loadByOpenId(String openId, String type);
}
