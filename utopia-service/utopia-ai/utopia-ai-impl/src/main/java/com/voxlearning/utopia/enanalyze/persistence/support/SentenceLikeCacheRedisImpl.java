package com.voxlearning.utopia.enanalyze.persistence.support;

import com.google.common.collect.Maps;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.lambdaworks.redis.api.sync.RedisSetCommands;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.enanalyze.exception.BusinessException;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import com.voxlearning.utopia.enanalyze.persistence.SentenceLikeCache;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.enanalyze.api.SentenceLikeService.Params;
import static com.voxlearning.utopia.enanalyze.api.SentenceLikeService.Result;

/**
 * 好句子点赞redis实现
 *
 * @author xiaolei.li
 * @version 2018/7/24
 */
@Repository
public class SentenceLikeCacheRedisImpl extends BaseRedisCache implements SentenceLikeCache {

    @Override
    public Result like(Params params) {
        return like(params.getOpenGroupId(), params.getFromOpenId(), params.getToOpenId());
    }

    @Override
    public Result like(String openGroupId, String fromOpenId, String toOpenId) {
        // step 1 : 通过set元素不可重复来模拟点赞，如果sadd成功，说明之前没有点过赞，如果sadd是把，说明已经点过赞了，此时，再srem掉，
        //          也就是说通过写操作的结果来判断是否已经点赞
        // key : 固定前缀:openGroupId:toOpenId
        // element ： fromOpenId
        final String key = RedisConstant.Key.LIKE_SENTENCE_SET.getKey() + ":" + openGroupId + ":" + toOpenId;
        RedisSetCommands<String, Object> setCommands = redisCommands.sync().getRedisSetCommands();
        Long addResult = setCommands.sadd(key, fromOpenId);
        boolean isLike = Long.valueOf(1L).equals(addResult);
        if (!isLike)
            // 重复点赞 => 本次操作为取消点赞 => 删除点赞记录
            setCommands.srem(key, fromOpenId);
        Result result = new Result();
        result.setOpenGroupId(openGroupId);
        result.setFromOpenId(fromOpenId);
        result.setToOpenId(toOpenId);
        result.setLikeStatus(isLike);
        Long likes = setCommands.scard(key);
        result.setLikes(null == likes ? 0 : likes.longValue());
        // step 2 : 冗余数据，点赞数量，hash格式
        RedisHashAsyncCommands<String, Object> hashCommonds = redisCommands.async().getRedisHashAsyncCommands();
        hashCommonds.hset(
                // key : 固定前缀:openGroupId
                RedisConstant.Key.LIKE_SENTENCE_COUNTER.getKey() + ":" + openGroupId,
                // field : openId
                toOpenId,
                // value : 点赞数
                Long.valueOf(result.getLikes()));
        // step 3 : 冗余数据，点赞状态，hash格式
        hashCommonds.hset(
                // key : 固定前缀:openGroupId:
                RedisConstant.Key.LIKE_SENTENCE_STATUS.getKey() + ":" + openGroupId,
                // field : fromOpenId_toOpenId
                fromOpenId + "@" + toOpenId,
                // value : 是否点赞
                isLike
        );
        return result;
    }

    @Override
    public void purge(List<String> openGroupIds, String openId) {
        openGroupIds.stream()
                .filter(openGroupId -> StringUtils.isNotBlank(openGroupId))
                .forEach(openGroupId -> {
                    // 删除互斥set
                    redisCommands.async().getRedisKeyAsyncCommands().del(
                            RedisConstant.Key.LIKE_SENTENCE_SET.getKey() + ":" + openGroupId + ":" + openId);
                    // 删除点赞数量
                    redisCommands.async().getRedisHashAsyncCommands().hdel(
                            RedisConstant.Key.LIKE_SENTENCE_COUNTER.getKey() + ":" + openGroupId,
                            openId
                    );
                    // 删除点赞状态
                    redisCommands.async().getRedisKeyAsyncCommands().del(
                            RedisConstant.Key.LIKE_SENTENCE_STATUS.getKey() + ":" + openGroupId);
                });
    }

    @Override
    public long getLikes(String openGroupId, String openId) {
        final String key = RedisConstant.Key.LIKE_SENTENCE_COUNTER.getKey() + ":" + openGroupId;
        RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        Object likes = hashCommands.hget(key, openId);
        return null == likes ? 0 : Long.valueOf(likes.toString());
    }

    @Override
    public Map<String, Long> queryLikes(String openGroupId, List<String> openIds) {
        RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        List<Object> list = hashCommands.hmget(RedisConstant.Key.LIKE_SENTENCE_COUNTER.getKey() + ":" + openGroupId, openIds.toArray(new String[]{}));
        if (list.size() != openIds.size())
            throw new BusinessException(String.format("查询某个群下多个人的点赞数时发生异常，人数和结果不一致,openGroupId = %s", openGroupId));
        HashMap<String, Long> map = Maps.newHashMap();
        for (int i = 0; i < openIds.size(); i++) {
            final String openId = openIds.get(i);
            Object o = list.get(i);
            final long likes = null == o ? 0 : Long.valueOf(o.toString());
            map.put(openId, likes);
        }
        return map;
    }

    @Override
    public Map<String, Long> queryLikes(List<String> openGroupIds, String openId) {
        RedisHashCommands<String, Object> hashCommonds = redisCommands.sync().getRedisHashCommands();
        Map<String, Long> map = Maps.newHashMap();
        for (String openGroupId : openGroupIds) {
            Object likes = hashCommonds.hget(RedisConstant.Key.LIKE_SENTENCE_COUNTER.getKey() + ":" + openGroupId, openId);
            if (null != likes)
                map.put(openGroupId, Long.valueOf(likes.toString()));
            else
                map.put(openGroupId, Long.valueOf(0L));
        }
        return map;
    }

    @Override
    public Map<String, Boolean> batchGetLikeStatus(String openGroupId, String fromOpenId, List<String> toOpenIds) {
        RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        List<String> fields = toOpenIds.stream().map(i -> fromOpenId + "@" + i).collect(Collectors.toList());
        List<Object> list = hashCommands.hmget(RedisConstant.Key.LIKE_SENTENCE_STATUS.getKey() + ":" + openGroupId, fields.toArray(new String[]{}));
        if (list.size() != toOpenIds.size())
            throw new BusinessException(String.format("查询某个群下多个人的点赞状态时发生异常，人数和结果不一致,openGroupId = %s", openGroupId));
        HashMap<String, Boolean> map = Maps.newHashMap();
        for (int i = 0; i < toOpenIds.size(); i++) {
            final String openId = toOpenIds.get(i);
            if (null != list.get(i))
                map.put(openId, Boolean.valueOf(list.get(i).toString()));
            else
                map.put(openId, Boolean.FALSE);

        }
        return map;
    }
}
