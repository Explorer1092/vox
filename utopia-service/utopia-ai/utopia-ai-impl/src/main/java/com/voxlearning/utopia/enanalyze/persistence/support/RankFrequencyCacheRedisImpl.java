package com.voxlearning.utopia.enanalyze.persistence.support;

import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.utopia.enanalyze.persistence.RankFrequencyCache;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 排行服务 - 批改频率
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
@Slf4j
@Service
public class RankFrequencyCacheRedisImpl extends BaseRedisCache implements RankFrequencyCache, InitializingBean {
    @Override
    public void update(String openId, double frequency) {
        RedisSortedSetAsyncCommands<String, Object> commands = redisCommands.async().getRedisSortedSetAsyncCommands();
        commands.zadd(RedisConstant.Key.RANK_FREQUENCY.getKey(), frequency, openId);
    }

    @Override
    public Long get(String openId) {
        RedisSortedSetCommands<String, Object> commands = redisCommands.sync().getRedisSortedSetCommands();
        return commands.zrevrank(RedisConstant.Key.RANK_FREQUENCY.getKey(), openId);
    }

    @Override
    public Long count() {
        RedisSortedSetCommands<String, Object> commands = redisCommands.sync().getRedisSortedSetCommands();
        return commands.zcard(RedisConstant.Key.RANK_FREQUENCY.getKey());
    }
}
