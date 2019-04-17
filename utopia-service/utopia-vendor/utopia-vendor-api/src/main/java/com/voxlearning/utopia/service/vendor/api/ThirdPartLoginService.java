package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.concurrent.TimeUnit;

/**
 *
 * 该服务全部用于与daite的用户登录态的交互过程，将来有其他的用户登录态的交互全部写入该类
 *
 * Created by zhouwei on 2018/8/2
 **/
@ServiceVersion(version = "20180802")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ThirdPartLoginService {

    /**
     *
     * 校验用户是否在daite是登录状态
     *
     * @param token
     * @return
     * @author zhouwei on 2018/8/2
     */
    MapMessage checkLogin(String source, String token);

}
