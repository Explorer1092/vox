package com.voxlearning.utopia.service.reward.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.entity.*;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180612")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface PublicGoodService {

    MapMessage upsertCollect(PublicGoodCollect collect);

    MapMessage addCollectLike(PublicGoodCollect collect, PublicGoodFeed feed);

    MapMessage addUserActivityMoney(PublicGoodCollect collect, Long money);

    void persistenceClazzCollectToRedis(PublicGoodCollect collect, Boolean done);

    MapMessage collectThirdPartyLike(Long activityId, Long userId);

    MapMessage upsertFeed(PublicGoodFeed feed);

    MapMessage donate(String eleTypeCode,RewardActivityRecord record);

    void addFeedRedis(Long activityId, Long userId);

    MapMessage useKey(Long userId,Long activityId);

    MapMessage upsertUserActivity(PublicGoodUserActivity userActivity);

    MapMessage updateReward(PublicGoodReward reward);

    /**
     * 把全国榜的数据同步到数据库里面
     * @return
     */
    MapMessage persistRank();

    /**
     * 把数据库里面的数据恢复到redis
     * @param activityId
     * @return
     */
    MapMessage restoreRank(Long activityId);

    MapMessage moveLikeToMongo(Integer start, Integer end);

}
