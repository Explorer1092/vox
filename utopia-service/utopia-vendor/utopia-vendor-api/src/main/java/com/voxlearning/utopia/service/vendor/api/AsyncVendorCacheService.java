package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.api.constant.HomeWorkReportMissionType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.12.04")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncVendorCacheService {


    @Async
    AlpsFuture<Boolean> HomeworkReportPicListenTaskCacheManager_finishTask(String homeworkId, Long studentId);

    @Async
    AlpsFuture<Integer> HomeworkReportPicListenTaskCacheManager_getTaskStatus(String homeworkId, Long studentId);



    // ========================================================================
    // ParentFairylandClassmatesUsageCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Map<Long, String>> ParentFairylandClassmatesUsageCacheManager_fetch(Long clazzId, OrderProductServiceType appKey);

    @Async
    AlpsFuture<Boolean> ParentFairylandClassmatesUsageCacheManager_record(Long clazzId, OrderProductServiceType appKey, Long studentId, String content);


    @Async
    AlpsFuture<Boolean> StudentAppDoHomeworkRecordCacheManager_doHomework(Long studentId);

    @Async
    AlpsFuture<Boolean> StudentAppDoHomeworkRecordCacheManager_hasDoneHomework(Long studentId);

    // ========================================================================
    // StudentCheckJztCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> StudentCheckJztCacheManager_record(Long studentId);

    @Async
    AlpsFuture<Boolean> StudentCheckJztCacheManager_hasRecord(Long studentId);

    // ========================================================================
    // StudentGrindEarDayRecordCacheManager
    // ========================================================================


    // ========================================================================
    // JztHomeworkReportCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> JztHomeworkReportCacheManager_recordReportMissionIntegral(String homeworkId, Long studentId, HomeWorkReportMissionType missionType, Integer integralCount);

    @Async
    AlpsFuture<Integer> JztHomeworkReportCacheManager_loadIntegralCount(String homeworkId, Long studentId);

    @Async
    AlpsFuture<Set<String>> loadYiQiXuePushTag(Long userId);
}
