package com.voxlearning.utopia.enanalyze.persistence.support;

import com.alibaba.fastjson.JSON;
import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import com.voxlearning.utopia.enanalyze.persistence.SentenceCache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好句子缓存redis实现
 *
 * @author xiaolei.li
 * @version 2018/7/22
 */
@Service
public class SentenceCacheRedisImpl extends BaseRedisCache implements SentenceCache {

    @Override
    public void update(Sentence sentence) {
        // 句子结构
        // key : key
        // field : openId
        // value : sentence's json string
        RedisHashCommands<String, Object> commands = redisCommands.sync().getRedisHashCommands();
        commands.hset(RedisConstant.Key.SENTENCE.getKey(), sentence.getOpenId(), JSON.toJSONString(sentence));
    }

    @Override
    public Sentence get(String openId) {
        RedisHashCommands<String, Object> commands = redisCommands.sync().getRedisHashCommands();
        Object value = commands.hget(RedisConstant.Key.SENTENCE.getKey(), openId);
        return null == value ? null : JSON.parseObject(value.toString(), Sentence.class);
    }

    @Override
    public List<Sentence> list(List<String> openIds) {
        RedisHashCommands<String, Object> commands = redisCommands.sync().getRedisHashCommands();
        List<Object> result = commands.hmget(RedisConstant.Key.SENTENCE.getKey(), openIds.toArray(new String[]{}));
        return result.stream()
                .filter(i -> StringUtils.isNotBlank(i.toString()))
                .map(i -> JSON.parseObject(i.toString(), Sentence.class)).collect(Collectors.toList());
    }

    @Override
    public Map<String, Sentence> queryMap(List<String> openIds) {
        RedisHashCommands<String, Object> commands = redisCommands.sync().getRedisHashCommands();
        List<Object> rs = commands.hmget(RedisConstant.Key.SENTENCE.getKey(), openIds.toArray(new String[]{}));
        return rs.stream()
                .filter(i -> null != i && StringUtils.isNotBlank(i.toString()))
                .map(i -> JSON.parseObject(i.toString(), Sentence.class))
                .collect(Collectors.toMap(Sentence::getOpenId, i -> i));

    }
}
