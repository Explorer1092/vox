package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190304")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsWechatUserService extends IPingable {

    /**
     *
     * @param openId
     * @param type com.voxlearning.utopia.service.ai.constant.WechatUserType
     * @return
     */
    MapMessage register(String openId, String type);

    /**
     *
     * @param openId
     * @param type com.voxlearning.utopia.service.ai.constant.WechatUserType
     * @return
     */
    MapMessage register(String openId, String type, String nickName, String avatar, Long userId);

    MapMessage updateUserInfo(Long wechatUserId, String nickName, String avatar);
}
