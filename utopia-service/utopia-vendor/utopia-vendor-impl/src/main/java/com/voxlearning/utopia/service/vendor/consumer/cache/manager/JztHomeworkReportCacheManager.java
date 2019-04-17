package com.voxlearning.utopia.service.vendor.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.vendor.api.constant.HomeWorkReportMissionType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2017/2/23
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 60)
public class JztHomeworkReportCacheManager extends PojoCacheObject<String, Long> {
    private static String keyPrefix = "homework_report_";

    public JztHomeworkReportCacheManager(UtopiaCache cache) {
        super(cache);
    }

    /**
     * 对于家长通作业报告中完成任务获取学豆的奖励，存储某次作业某个学生每个任务对应获取的学豆数,因为作业报告是60天过期，所以这个数据也存60天
     * @param homeworkId 作业id
     * @param studentId 学生id
     * @param missionType   签字任务类型
     * @param integralCount 此任务应该活得的学豆数
     */
    public void recordReportMissionIntegral(String homeworkId, Long studentId, HomeWorkReportMissionType missionType, Integer integralCount) {
        if (StringUtils.isEmpty(homeworkId) || studentId == null) {
            return;
        }
        String cacheKey = keyPrefix + homeworkId + "_" + studentId;
        CacheObject<Object> cacheObject = getCache().get(cacheKey);
        Map<String, Object> map;
        if (cacheObject == null || cacheObject.getValue() == null) {
            map = new HashMap<>();
            map.put(missionType.name(), integralCount);
            getCache().add(cacheKey, 86400 * 60, map);
        } else {
            getCache().createCacheValueModifier()
                    .key(cacheKey)
                    .expiration(86400 * 60)
                    .modifier(currentValue -> {
                        if (currentValue instanceof HashMap) {
                            throw new UnsupportedOperationException();
                        }
                        Map<String, Object> dataMap = (HashMap<String, Object>)currentValue;
                        dataMap.putIfAbsent(missionType.name(), integralCount);
                        return dataMap;
                    })
                    .execute();
        }

    }

    /**
     * 获取学生在某次作业报告中完成任务可以获取的学豆数量总数
     */
    public Integer loadIntegralCount(String homeworkId, Long studentId) {
        if (StringUtils.isEmpty(homeworkId) || studentId == null) {
            return 0;
        }
        String cacheKey = keyPrefix + homeworkId + "_" + studentId;
        Map<String, Object> countMap = getCache().load(cacheKey);
        Integer totalCount = 0;
        if (MapUtils.isNotEmpty(countMap)) {
           for (Object count : countMap.values()) {
               totalCount += SafeConverter.toInt(count);
           }
        }
        return totalCount;
    }
}
