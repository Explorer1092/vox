package com.voxlearning.washington.controller.mobile.student.helper;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.action.api.document.UserGrowth;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.zone.client.ZoneLoaderClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/17
 * Time: 10:33
 * 排行榜輔助类.
 * FIXME 这个功能据说在很早就下线了
 */
@Named
public class MobileStudentRankHelper {

    @Inject private WashingtonCacheSystem washingtonCacheSystem;
    @Inject private ZoneLoaderClient zoneLoaderClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject private ActionLoaderClient actionLoaderClient;

    /**
     * 查询用户在土豪榜中的排名
     */
    public Integer getSilverRanking(Long studentId, Clazz clazz) {
        List<Map<String, Object>> silverRank = zoneLoaderClient.getZoneLoader().silverRankSnapshot(clazz, studentId);

        if (CollectionUtils.isEmpty(silverRank)) {
            return null;
        }

        for (int i = 0; i < silverRank.size(); i++) {
            if (Objects.equals(studentId, silverRank.get(i).get("studentId"))) {
                return i + 1;
            }
        }

        return null;
    }

    /**
     * 查询学生在学霸榜中的排名
     */
    public Integer getSmRanking(Long studentId, Clazz clazz) {
        List<Map<String, Object>> smRank = zoneLoaderClient.getZoneLoader().studyMasterCountRankSnapshot(clazz, studentId);

        if (CollectionUtils.isEmpty(smRank)) {
            return null;
        }

        for (int i = 0; i < smRank.size(); i++) {
            if (Objects.equals(studentId, smRank.get(i).get("studentId"))) {
                return i + 1;
            }
        }

        return null;
    }

    /**
     * 查询学生在成长榜中的排名
     */
    public Integer getGrowthRanking(Long userId, Clazz clazz) {
        Integer ranking = 0;
        String growthRankCacheKey = "USER_GROWTH_RANKING_" + userId;
        CacheObject<Integer> rankCacheObject = washingtonCacheSystem.CBS.flushable.get(growthRankCacheKey);
        if (null == rankCacheObject || null == rankCacheObject.getValue()) {
            List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazz.getId(), userId);
            Set<Long> mateIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Map<Long, UserGrowth> userGrowthMap = actionLoaderClient.getRemoteReference().loadUserGrowthSnapshot(clazz.getId(), mateIds);
            if (MapUtils.isNotEmpty(userGrowthMap)) {
                List<UserGrowth> sortedList = userGrowthMap.values().stream()
                        .sorted((u1, u2) -> u2.getGrowthValue().compareTo(u1.getGrowthValue()))
                        .collect(Collectors.toList());
                for (int i = 0; i < sortedList.size(); i++) {
                    if (Objects.equals(sortedList.get(i).getId(), userId)) {
                        ranking = i + 1;

                        break;
                    }
                }
            }

            //有可能没有名次，这里存个0，免得每次都查库
            washingtonCacheSystem.CBS.flushable.set(growthRankCacheKey, 30 * 60, ranking);
        } else {
            ranking = rankCacheObject.getValue();
        }
        return ranking;
    }

}
