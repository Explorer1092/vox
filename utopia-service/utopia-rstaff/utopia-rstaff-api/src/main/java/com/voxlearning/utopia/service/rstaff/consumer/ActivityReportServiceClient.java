package com.voxlearning.utopia.service.rstaff.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;
import com.voxlearning.utopia.service.rstaff.api.service.ActivityReportBaseDataService;
import com.voxlearning.utopia.service.rstaff.api.service.ActivityReportCollectDataService;
import com.voxlearning.utopia.service.rstaff.api.service.ActivityReportService;
import com.voxlearning.utopia.service.rstaff.api.service.YearEndDataService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-10-12 15:07
 */

public class ActivityReportServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ActivityReportServiceClient.class);

    @Getter
    @ImportService(interfaceClass = ActivityReportBaseDataService.class)
    private ActivityReportBaseDataService activityReportBaseDataService;

    @Getter
    @ImportService(interfaceClass = ActivityReportCollectDataService.class)
    private ActivityReportCollectDataService activityReportCollectDataService;

    @Getter
    @ImportService(interfaceClass = ActivityReportService.class)
    private ActivityReportService activityReportService;

    @Getter
    @ImportService(interfaceClass = YearEndDataService.class)
    private YearEndDataService yearEndDataService;

    public Map<Long, Long> loadParticipateCountMapByClazzIds(String activityId) {
        return activityReportCollectDataService.loadParticipateCountMapByClazzIds(activityId);
    }

    public Long loadParticipateCountByClazzIds(String activityId, Collection<Long> clazzId) {
        return activityReportCollectDataService.loadParticipateCountByClazzIds(activityId, clazzId);
    }

    public Long loadParticipateCountByClazzId(String activityId, Long clazzId) {
        return activityReportCollectDataService.loadParticipateCountByClazzId(activityId, clazzId);
    }

    public MapMessage saveActivityReportBaseDatas(List<ActivityReportBaseData> inserts) {
        try {
            return activityReportBaseDataService.saveActivityReportBaseDatas(inserts);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return MapMessage.successMessage();
    }

    public MapMessage deleteAllBaseData() {
        return activityReportBaseDataService.deleteAll();
    }

    public MapMessage saveActivityReportCollectDatas(List<ActivityReportCollectData> inserts) {
        try {
            return activityReportCollectDataService.saveActivityReportCollectDatas(inserts);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return MapMessage.successMessage();
    }

    public MapMessage deleteAllCollectData() {
        return activityReportCollectDataService.deleteAll();
    }


    public void batchUpdateActivityReportCollectDatas(List<ActivityReportCollectData> collectDatas) {
        activityReportCollectDataService.batchUpdateActivityReportCollectDatas(collectDatas);
    }

    public Map<String,Object> loadActivityReportSurvey(String regionLevel, String regionCode, String id) {
        return activityReportCollectDataService.loadActivityReportSurvey(regionLevel,regionCode,id);
    }

    public Map<String,Object> loadActivityScoreState(String regionLevel, String regionCode, String id) {
        return activityReportCollectDataService.loadActivityScoreState(regionLevel,regionCode,id);
    }

    public Map<String,Object> loadActivityScoreLevel(String regionLevel, String regionCode, String id) {
        return activityReportCollectDataService.loadActivityScoreLevel(regionLevel,regionCode,id);
    }

    public Map<String,Object> loadActivityAnswerSpeed(String regionLevel, String regionCode, String id) {
        return activityReportCollectDataService.loadActivityAnswerSpeed(regionLevel,regionCode,id);
    }

    public List<ActivityReportClassSnapshotData> loadClassSnapshot(String activityId, Long clazzId) {
        return activityReportService.loadClassSnapshot(activityId, clazzId);
    }

    public List<ActivityReportStudentData> loadStudentData(String activityId, Long clazzId) {
        return activityReportService.loadStudentData(activityId, clazzId);
    }

    public Map<String,Object> loadTeacherYearData(Long teacherId) {
        return yearEndDataService.loadTeacherYearData(teacherId);
    }

    public Map<String,Object> loadStudentYearData(Long studentId) {
        return yearEndDataService.loadStudentYearData(studentId);
    }
}
