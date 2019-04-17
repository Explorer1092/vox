package com.voxlearning.enanalyze.aggregate;

import com.voxlearning.enanalyze.view.UserLoginRequest;
import com.voxlearning.enanalyze.view.UserLoginView;
import com.voxlearning.enanalyze.view.UserRequest;

/**
 * 用户聚合服务
 *
 * @author xiaolei.li
 * @version 2018/7/19
 */
public interface UserAggregator {

    /**
     * 更新用户信息
     *
     * @param request 用户信息
     */
    void update(UserRequest request);

    /**
     * 登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    UserLoginView login(UserLoginRequest request);

    /**
     * 鉴权
     *
     * @param token 令牌
     * @return
     */
    boolean isValid(String token);
}
