package com.voxlearning.utopia.enanalyze.api;


import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.enanalyze.model.UserLoginParams;
import com.voxlearning.utopia.enanalyze.model.User;
import com.voxlearning.utopia.enanalyze.model.UserTotalCountParams;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface UserService {

    /**
     * 小程序应用标识
     */
    String APP_ID = "wx13e146a81ef5e9be";

    /**
     * 小程序应用秘钥
     */
    String APP_SECRET = "259a092d4a32ce27f1f1ddf3df5d2588";

    /**
     * 授权
     *
     * @param input
     * @return
     */
    MapMessage login(UserLoginParams input);

    /**
     * 鉴权
     *
     * @param token
     * @return
     */
    MapMessage isValid(String token);

    /**
     * 获取用户总数
     *
     * @param input 输入
     * @return 用户总数
     */
    MapMessage getTotalCount(UserTotalCountParams input);

    /**
     * 更新
     *
     * @param user 用户信息
     * @return 更新结果
     */
    MapMessage update(User user);
}

