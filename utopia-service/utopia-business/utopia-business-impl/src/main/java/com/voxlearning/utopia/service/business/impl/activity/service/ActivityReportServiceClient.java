package com.voxlearning.utopia.service.business.impl.activity.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.impl.activity.service.api.ActivityReportBaseDataService;
import com.voxlearning.utopia.service.business.impl.activity.service.api.ActivityReportClassSnapshotDataService;
import com.voxlearning.utopia.service.business.impl.activity.service.api.ActivityReportCollectDataService;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-10-12 15:07
 */
@Named("com.voxlearning.utopia.service.business.impl.activity.service.ActivityReportServiceClient")
public class ActivityReportServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ActivityReportServiceClient.class);

    @Inject
    private ActivityReportBaseDataService activityReportBaseDataService;
    @Inject
    private ActivityReportCollectDataService activityReportCollectDataService;
    @Inject
    private ActivityReportClassSnapshotDataService activityReportClassSnapshotDataService;

    public MapMessage saveActivityReportBaseDatas(List<ActivityReportBaseData> inserts) {
        try {
            return activityReportBaseDataService.saveActivityReportBaseDatas(inserts);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
        }
        return MapMessage.successMessage();
    }

    public MapMessage deleteAllCollectData() {
        return activityReportCollectDataService.deleteAll();
    }

    public MapMessage deleteCollectDataByActivityId(String activityId) {
        return activityReportCollectDataService.deleteByActivityId(activityId);
    }

    public MapMessage saveActivityReportClassSnapshotData(ActivityReportClassSnapshotData data) {
        return activityReportClassSnapshotDataService.saveActivityReportClassSnapshotData(data);
    }

    public MapMessage deleteActivityReportClassSnapshotData(String activityId) {
        return activityReportClassSnapshotDataService.deleteActivityReportClassSnapshotData(activityId);
    }
}
