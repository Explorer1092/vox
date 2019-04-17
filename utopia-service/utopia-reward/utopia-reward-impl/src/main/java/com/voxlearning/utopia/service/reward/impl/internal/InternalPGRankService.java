package com.voxlearning.utopia.service.reward.impl.internal;

import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.entity.PublicGoodNationRank;
import com.voxlearning.utopia.service.reward.impl.dao.PublicGoodNationRankDao;
import com.voxlearning.utopia.service.reward.mapper.PGRankEntry;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Named
public class InternalPGRankService extends SpringContainerSupport{

    @Inject private PublicGoodNationRankDao nationRankDao;
    @Inject private SchoolLoaderClient schoolLoader;

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        redisCommands = builder.getRedisCommands("user-easemob");
    }

    private String genNationRankKey(Long activityId){
        return "PublicGoodRank:nation:activityId:" + activityId;
    }

    private String genSchoolRankKey(Long activityId, Long schoolId){
        return CacheKeyGenerator.generateCacheKey(
                "PublicGoodRank:school",
                new String[]{"activityId","schoolId"},
                new Object[]{activityId,schoolId});
    }

    private String genRankSchoolKey(Long activityId, Long schoolId) {
        return genNationRankKey(activityId) + ":school:" + schoolId;
    }

    public void fetchRankEntry(Long activityId,Long schoolId,FetchRankCallback callback){
        RedisSortedSetCommands<String,Object> ssCommands = redisCommands.sync().getRedisSortedSetCommands();
        RedisStringCommands<String,Object> stringCommands = redisCommands.sync().getRedisStringCommands();

        Long rank = ssCommands.zrevrank(genNationRankKey(activityId),schoolId);
        if(rank == null)
            rank = 10000L;

        // 捐的总钱数
        Long money = SafeConverter.toLong(stringCommands.get(genRankSchoolKey(activityId, schoolId)));

        // 从校榜的所有班级数据中，汇总出总的教室完成数
        Map<Long,PGRankEntry> schoolRankMap = RewardCache.getPersistent().load(genSchoolRankKey(activityId, schoolId));
        long finishNum = Optional.ofNullable(schoolRankMap)
                .map(Map::values)
                .orElse(Collections.emptySet())
                .stream()
                .mapToLong(p -> SafeConverter.toLong(p.getFinishNum()))
                .sum();

        callback.deal(rank,money,finishNum);
    }

    public interface FetchRankCallback{
        void deal(Long rank,Long money,Long finishNum);
    }

    public PublicGoodNationRank loadNationRankEntry(Long schoolId){
        return nationRankDao.load(schoolId);
    }

    public MapMessage saveNationRankEntry(PublicGoodNationRank rankEntry){
        try{
            nationRankDao.upsert(rankEntry);
            return MapMessage.successMessage();
        }catch (Exception e){
            logger.error("PG:upsert nation rank entry error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    public MapMessage persistRank(){
        try{
            RedisSortedSetCommands<String, Object> ssCmd = redisCommands.sync().getRedisSortedSetCommands();
            Long activityId;
            if(RuntimeMode.isUsingTestData())
                activityId = 66L;
            else if(RuntimeMode.isProduction())
                activityId = 24L;
            else
                return MapMessage.errorMessage("该环境不能执行!");

            ssCmd.zrangeWithScores(genNationRankKey(activityId), 0, -1).forEach(sv -> {
                Long money = new Double(sv.score).longValue();
                Long schoolId = SafeConverter.toLong(sv.value);

                PublicGoodNationRank rankEntry = new PublicGoodNationRank();
                rankEntry.setId(schoolId);
                rankEntry.setMoney(money);

                Optional.ofNullable(schoolLoader.getSchoolLoader()
                        .loadSchool(rankEntry.getId())
                        .getUninterruptibly())
                        .ifPresent(s -> rankEntry.setSchoolName(s.getShortName()));

                saveNationRankEntry(rankEntry);
            });

            return MapMessage.successMessage();
        }catch (Exception e){
            logger.error("PG:persist to rank failed!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    public MapMessage restoreRank(Long activityId){
        try{
            RedisSortedSetCommands<String, Object> ssCmd = redisCommands.sync().getRedisSortedSetCommands();
            String cacheKy = genNationRankKey(activityId);

            nationRankDao.query().forEach(entry -> {
                ssCmd.zadd(cacheKy, entry.getMoney(), entry.getId());
            });

            return MapMessage.successMessage();
        }catch (Exception e){
            logger.error("PG:restore to rank failed!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }
}
