package com.voxlearning.utopia.service.rstaff.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.monitor.ServiceMetric;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "1.0.1")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@ServiceMetric
public interface ActivityReportCollectDataService {

    Map<Long, Long> loadParticipateCountMapByClazzIds(String activityId);

    default Long loadParticipateCountByClazzIds(String activityId) {
        Collection<Long> values = loadParticipateCountMapByClazzIds(activityId).values();
        long sum = values.stream().mapToLong(i -> i).sum();
        return sum;
    }

    default Long loadParticipateCountByClazzIds(String activityId, Collection<Long> clazzId) {
        Long count = 0L;
        Map<Long, Long> longLongMap = loadParticipateCountMapByClazzIds(activityId);
        for (Map.Entry<Long, Long> entry : longLongMap.entrySet()) {
            if (clazzId.contains(entry.getKey())) {
                count += entry.getValue();
            }
        }
        return count;
    }

    default Long loadParticipateCountByClazzId(String activityId, Long clazzId) {
        Map<Long, Long> map = loadParticipateCountMapByClazzIds(activityId);
        return map.getOrDefault(clazzId, 0L);
    }

    MapMessage saveActivityReportCollectDatas(List<ActivityReportCollectData> inserts);

    MapMessage deleteAll();

    void batchUpdateActivityReportCollectDatas(List<ActivityReportCollectData> collectDatas);

    Map<String,Object> loadActivityReportSurvey(String regionLevel, String regionCode, String id);

    Map<String,Object> loadActivityScoreState(String regionLevel, String regionCode, String id);

    Map<String,Object> loadActivityScoreLevel(String regionLevel, String regionCode, String id);

    Map<String,Object> loadActivityAnswerSpeed(String regionLevel, String regionCode, String id);
}
