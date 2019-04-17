package com.voxlearning.utopia.service.reward.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.reward.api.PublicGoodService;
import com.voxlearning.utopia.service.reward.entity.*;
import org.slf4j.Logger;

public class PublicGoodServiceClient {

    /** 日志 **/
    private static final Logger logger = LoggerFactory.getLogger(PublicGoodServiceClient.class);

    @ImportService(interfaceClass = PublicGoodService.class)
    private PublicGoodService remoteReference;

    public MapMessage upsertCollect(PublicGoodCollect col) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("PublicGood:upsertCollect")
                    .keys(col.getUserId(), col.getActivityId())
                    .callback(() -> remoteReference.upsertCollect(col))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to upsert collect (user={},activity={}): DUPLICATED OPERATION", col.getUserId(), col.getActivityId());
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to upsert collect (user={},activity={})", col.getUserId(), col.getActivityId(), ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage addCollectLike(PublicGoodCollect collect, PublicGoodFeed feed) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("PublicGood:addCollectLike")
                    .keys(collect.getUserId(), collect.getActivityId())
                    .callback(() -> remoteReference.addCollectLike(collect, feed))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to addCollectLike (user={},activity={}): DUPLICATED OPERATION", collect.getUserId(), collect.getActivityId());
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to addCollectLike (user={},activity={})", collect.getUserId(), collect.getActivityId(), ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage collectThirdPartyLike(Long activityId, Long userId) {
        return remoteReference.collectThirdPartyLike(activityId, userId);
    }

    public MapMessage upsertFeed(PublicGoodFeed feed) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("PublicGood:upsertFeed")
                    .keys(feed.getUserId(), feed.getActivityId())
                    .callback(() -> remoteReference.upsertFeed(feed))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to upsertFeed (user={},activity={}): DUPLICATED OPERATION", feed.getUserId(), feed.getActivityId());
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to upsertFeed (user={},activity={})", feed.getUserId(), feed.getActivityId(), ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage donate(String typeCode, RewardActivityRecord record) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("PublicGood:donate")
                    .keys(record.getUserId(), record.getActivityId())
                    .callback(() -> remoteReference.donate(typeCode, record))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to donate (user={},activity={}): DUPLICATED OPERATION", record.getUserId(), record.getActivityId());
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to donate (user={},activity={})", record.getUserId(), record.getActivityId(), ex);
            return MapMessage.errorMessage();
        }
    }

    public void addFeedRedis(Long activityId, Long userId) {
        remoteReference.addFeedRedis(activityId, userId);
    }

    public MapMessage useKey(Long userId, Long activityId) {
        return remoteReference.useKey(userId, activityId);
    }

    public MapMessage upsertUserActivity(PublicGoodUserActivity userActivity) {
        return remoteReference.upsertUserActivity(userActivity);
    }

    public MapMessage updateReward(PublicGoodReward reward) {
        return remoteReference.updateReward(reward);
    }

    public MapMessage persistRank(){
        return remoteReference.persistRank();
    }

    public MapMessage moveLikeToMongo(Integer start, Integer end) {
        return remoteReference.moveLikeToMongo(start, end);
    }
}
