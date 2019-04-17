package com.voxlearning.utopia.service.business.impl.activity.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.impl.activity.dao.ActivityReportClassSnapshotDataPersistence;
import com.voxlearning.utopia.service.business.impl.activity.service.api.ActivityReportClassSnapshotDataService;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.business.impl.activity.service.ActivityReportClassSnapshotDataServiceImpl")
public class ActivityReportClassSnapshotDataServiceImpl extends SpringContainerSupport implements ActivityReportClassSnapshotDataService {

    @Inject
    private ActivityReportClassSnapshotDataPersistence activityReportClassSnapshotDataPersistence;

    @Override
    public MapMessage saveActivityReportClassSnapshotData(ActivityReportClassSnapshotData data) {
        activityReportClassSnapshotDataPersistence.insert(data);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveActivityReportClassSnapshotDatas(List<ActivityReportClassSnapshotData> inserts) {
        activityReportClassSnapshotDataPersistence.inserts(inserts);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteActivityReportClassSnapshotData(String activityId) {
        activityReportClassSnapshotDataPersistence.removeByActivityId(activityId);
        return MapMessage.successMessage();
    }
}
