package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.user.api.mappers.AbtestMapper;
import com.voxlearning.utopia.service.user.consumer.UserAbtestLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class UserAbtestLoaderClientHelper {

    @Inject private UserAbtestLoaderClient userAbtestLoaderClient;

    private AbtestMapper loadAbtestForUserFromCache(String experimentId, long userId) {
        String cacheKey = AbtestMapper.generateCacheKey(userId, experimentId);
        CacheObject<AbtestMapper> cacheObject = CacheSystem.CBS.getCache("persistence").get(cacheKey);
        if (cacheObject == null) {
            return null;
        }
        return cacheObject.getValue();
    }

    public AbtestMapper generateUserAbtestInfo(long userId, String experimentId) {
        AbtestMapper abtestMapper = loadAbtestForUserFromCache(experimentId, userId);
        if (abtestMapper == null || RuntimeMode.lt(Mode.STAGING)) {
            abtestMapper = userAbtestLoaderClient.getRemoteReference().generateUserAbtestInfo(userId, experimentId);
        }
        // 打一个日志到苏浩，然后数据专门出一张表供策略查询用户的abtest分组，没有分组的数据就不打了
        if (StringUtils.isNotEmpty(abtestMapper.getGroupId())) {
            LogCollector.info("user_abtest_info", MiscUtils.map(
                    "experimentId", experimentId,
                    "experimentName", abtestMapper.getExperimentName(),
                    "groupId", abtestMapper.getGroupId(),
                    "groupName", abtestMapper.getGroupName(),
                    "planId", abtestMapper.getPlanId(),
                    "planName", abtestMapper.getPlanName(),
                    "hit", abtestMapper.getHit(),
                    "error", abtestMapper.getError(),
                    "uid", userId,
                    "op", "abtest_generate_info"
            ));
        }
        return abtestMapper;
    }
}
