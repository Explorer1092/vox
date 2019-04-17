package com.voxlearning.utopia.service.reward.impl.internal;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;

@Named
public class InternalAvatarService extends SpringContainerSupport {

    private IRedisCommands redisCommands;
    private static final String KEY_REMARD_TIP = "REMARD_CENTRE_AVATAR_TYPE";
    public static final int TOBY_AVATAR_TYPE = 1;

    @Override
    public void afterPropertiesSet() {
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    private String genKey(long userId){
        return KEY_REMARD_TIP + ":" + userId;
    }

    /**
     * 获取头像类型
     * @param userId
     * @return
     */
    public boolean isTobyAvatarType(long userId) {
        int value = 0;
        try {
            RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
            String key = genKey(userId);

            Object obj = stringCommands.get(key);
            if (obj != null) {
                value = (int) obj;
            }

        } catch (Exception e) {
            logger.warn(String.format("isTobyAvatarType warn userId:%s", userId), e);
        }

        return value == TOBY_AVATAR_TYPE;
    }

    /**
     * 设置头像类型
     * @param userId
     * @param type
     * @return
     */
    public boolean setAvatarType(long userId, int type) {
        String value = null;
        try {
            RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
            String key = genKey(userId);

            value = stringCommands.set(key, type);

        } catch (Exception e) {
            logger.warn(String.format("setAvatarType warn userId:%s", userId), e);
        }

        return value != null;
    }
}
