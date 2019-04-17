package com.voxlearning.utopia.enanalyze.service.support;

import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.utopia.enanalyze.model.ArticleCompositeAbility;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import com.voxlearning.utopia.enanalyze.service.RankAbilityService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 排名服务实现 - 作文综合能力
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
@Service
public class RankAbilityServiceImpl implements RankAbilityService, InitializingBean {

    IRedisCommands redisCommands;

    @Override
    public void update(String userId, ArticleCompositeAbility ability) {
        RedisSortedSetAsyncCommands<String, Object> commands = redisCommands.async().getRedisSortedSetAsyncCommands();
        commands.zadd(RedisConstant.Key.RANK_ABILITY.getKey(), ability.getScore(), userId);
    }

    @Override
    public long get(String userId) {
        RedisSortedSetCommands<String, Object> commands = redisCommands.sync().getRedisSortedSetCommands();
        return commands.zrevrank(RedisConstant.Key.RANK_ABILITY.getKey(), userId);
    }

    @Override
    public void afterPropertiesSet() {
        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        redisCommands = instance.getRedisCommands(RedisConstant.CONFIG);
    }
}
