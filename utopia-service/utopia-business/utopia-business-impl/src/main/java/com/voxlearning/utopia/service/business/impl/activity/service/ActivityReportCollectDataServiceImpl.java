package com.voxlearning.utopia.service.business.impl.activity.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.impl.activity.dao.ActivityReportCollectDataPersistence;
import com.voxlearning.utopia.service.business.impl.activity.service.api.ActivityReportCollectDataService;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.business.impl.activity.service.ActivityReportCollectDataServiceImpl")
public class ActivityReportCollectDataServiceImpl extends SpringContainerSupport implements ActivityReportCollectDataService {

    @Inject
    private ActivityReportCollectDataPersistence activityReportCollectDataPersistence;

    @Override
    public MapMessage saveActivityReportCollectDatas(List<ActivityReportCollectData> inserts) {
        activityReportCollectDataPersistence.inserts(inserts);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAll() {
        activityReportCollectDataPersistence.deleteAll();
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteByActivityId(String activityId) {
        activityReportCollectDataPersistence.deleteActivityId(activityId);
        return MapMessage.successMessage();
    }
}
