package com.voxlearning.utopia.enanalyze.persistence.support;

import com.alibaba.fastjson.JSON;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import com.voxlearning.utopia.enanalyze.persistence.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 缓存持久层redis实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Repository
public class UserCacheRedisImpl extends BaseRedisCache implements UserCache {

    @Override
    public void saveSession(String token, UserEntity user) {
        final String key = RedisConstant.Key.USER_SESSION.getKey() + ":" + token;
        redisCommands.sync().getRedisHashCommands().hset(key, token,
                JSON.parseObject(JSON.toJSONString(user), Map.class));
        redisCommands.sync().getRedisKeyCommands().expire(key, RedisConstant.Key.USER_SESSION.TIMEOUT);
    }

    @Override
    public boolean isSessionValid(String token) {
        final String key = RedisConstant.Key.USER_SESSION.getKey() + ":" + token;
        return Long.valueOf(1).equals(redisCommands.sync().getRedisKeyCommands().exists(new String[]{key}));
    }
}
