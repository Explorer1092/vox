package com.voxlearning.utopia.enanalyze.persistence.support;

import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import com.voxlearning.utopia.enanalyze.persistence.SentenceRankCache;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排名服务实现 - 最好的句子
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
@Service
public class RankSentenceCacheRedisImpl extends BaseRedisCache implements SentenceRankCache {

    @Override
    public void update(Element element) {
        RedisSortedSetAsyncCommands<String, Object> zsortCommonds = redisCommands.async().getRedisSortedSetAsyncCommands();
        // 排行榜结构
        // key : prefix:openGroupId
        // member : openId
        // score : sentence's score
        Arrays.stream(element.getOpenGroupIds())
                .filter(openGroupId -> StringUtils.isNotBlank(openGroupId))
                .forEach(openGroupId -> zsortCommonds.zadd(
                        RedisConstant.Key.RANK_SENTENCE.getKey() + ":" + openGroupId,
                        element.getScore(),
                        element.getOpenId()));
    }

    @Override
    public Rank getRank(String openGroupId, String openId) {
        RedisSortedSetCommands<String, Object> commands = redisCommands.sync().getRedisSortedSetCommands();
        Long rank = commands.zrank(RedisConstant.Key.RANK_SENTENCE.getKey() + ":" + openGroupId, openId);
        return new Rank(openGroupId, openId, rank);
    }

    @Override
    public List<Rank> getRanks(String openGroupId) {
        RedisSortedSetCommands<String, Object> commands = redisCommands.sync().getRedisSortedSetCommands();
        List<Object> rs = commands.zrevrange(RedisConstant.Key.RANK_SENTENCE.getKey() + ":" + openGroupId, 0, -1);
        List<Rank> list = rs.stream()
                .map(i -> {
                    Rank rank = new Rank();
                    rank.setOpenGroupId(openGroupId);
                    rank.setOpenId(i.toString());
                    return rank;
                }).collect(Collectors.toList());
        // 添加序号，感觉很笨，但是stream好像没有可用的添加需要的方法
        for (int i = 0; i < list.size(); i++) {
            Rank r = list.get(i);
            r.setRank(Long.valueOf(i));
        }
        return list;
    }
}
