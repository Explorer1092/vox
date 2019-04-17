package com.voxlearning.utopia.enanalyze.persistence;

import com.voxlearning.utopia.enanalyze.entity.UserEntity;

/**
 * 缓存持久层
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface UserCache {

    /**
     * 存储会话
     *
     * @param token 令牌
     * @param user  用户信息
     */
    void saveSession(String token, UserEntity user);

    /**
     * 会话是否有效
     *
     * @param token
     * @return
     */
    boolean isSessionValid(String token);

}
