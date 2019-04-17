package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.api.AsyncVendorCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.HomeWorkReportMissionType;
import com.voxlearning.utopia.service.vendor.consumer.cache.manager.*;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Named("com.voxlearning.utopia.service.vendor.impl.service.AsyncVendorCacheServiceImpl")
@ExposeServices({
        @ExposeService(interfaceClass = AsyncVendorCacheService.class, version = @ServiceVersion(version = "2017.05.16")),
        @ExposeService(interfaceClass = AsyncVendorCacheService.class, version = @ServiceVersion(version = "2017.12.04"))
})
@Getter
public class AsyncVendorCacheServiceImpl extends SpringContainerSupport implements AsyncVendorCacheService {

    private JztHomeworkReportCacheManager jztHomeworkReportCacheManager;

    private StudentAppDoHomeworkRecordCacheManager studentAppDoHomeworkRecordCacheManager;
    private StudentCheckJztCacheManager studentCheckJztCacheManager;

    private HomeworkReportPicListenTaskCacheManager homeworkReportPicListenTaskCacheManager;
    private ParentFairylandClassmatesUsageCacheManager parentFairylandClassmatesUsageCacheManager;
    @Inject private YiQiXuePushTagCacheSystem yiQiXuePushTagCacheSystem;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        UtopiaCache unflushable = CacheSystem.CBS.getCache("unflushable");
        studentAppDoHomeworkRecordCacheManager = new StudentAppDoHomeworkRecordCacheManager(unflushable);
        studentCheckJztCacheManager = new StudentCheckJztCacheManager(unflushable);
        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        homeworkReportPicListenTaskCacheManager = new HomeworkReportPicListenTaskCacheManager(persistence);
        parentFairylandClassmatesUsageCacheManager = new ParentFairylandClassmatesUsageCacheManager(persistence);
        jztHomeworkReportCacheManager = new JztHomeworkReportCacheManager(persistence);


    }


    @Override
    public AlpsFuture<Boolean> HomeworkReportPicListenTaskCacheManager_finishTask(String homeworkId, Long studentId) {
        homeworkReportPicListenTaskCacheManager.finishTask(homeworkId, studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Integer> HomeworkReportPicListenTaskCacheManager_getTaskStatus(String homeworkId, Long studentId) {
        Integer i = homeworkReportPicListenTaskCacheManager.getTaskStatus(homeworkId, studentId);
        return new ValueWrapperFuture<>(i);
    }



    @Override
    public AlpsFuture<Map<Long, String>> ParentFairylandClassmatesUsageCacheManager_fetch(Long clazzId, OrderProductServiceType appKey) {
        Map<Long, String> m = parentFairylandClassmatesUsageCacheManager.fetch(clazzId, appKey);
        return new ValueWrapperFuture<>(m);
    }

    @Override
    public AlpsFuture<Boolean> ParentFairylandClassmatesUsageCacheManager_record(Long clazzId, OrderProductServiceType appKey, Long studentId, String content) {
        parentFairylandClassmatesUsageCacheManager.record(clazzId, appKey, studentId, content);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentAppDoHomeworkRecordCacheManager_doHomework(Long studentId) {
        studentAppDoHomeworkRecordCacheManager.doHomework(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentAppDoHomeworkRecordCacheManager_hasDoneHomework(Long studentId) {
        boolean b = studentAppDoHomeworkRecordCacheManager.hasDoneHomework(studentId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> StudentCheckJztCacheManager_record(Long studentId) {
        studentCheckJztCacheManager.record(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentCheckJztCacheManager_hasRecord(Long studentId) {
        boolean b = studentCheckJztCacheManager.hasRecord(studentId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> JztHomeworkReportCacheManager_recordReportMissionIntegral(String homeworkId, Long studentId, HomeWorkReportMissionType missionType, Integer integralCount) {
        jztHomeworkReportCacheManager.recordReportMissionIntegral(homeworkId, studentId, missionType, integralCount);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Integer> JztHomeworkReportCacheManager_loadIntegralCount(String homeworkId, Long studentId) {
        Integer i = jztHomeworkReportCacheManager.loadIntegralCount(homeworkId, studentId);
        return new ValueWrapperFuture<>(i);
    }

    @Override
    public AlpsFuture<Set<String>> loadYiQiXuePushTag(Long userId) {
        if (userId == null) {
            return new ValueWrapperFuture<>(Collections.emptySet());
        }
        return new ValueWrapperFuture<>(yiQiXuePushTagCacheSystem.getYiQiXuePushTagCache().loadUserYiQiXuePushTag(userId));
    }

}
