package com.voxlearning.utopia.service.rstaff.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;
import com.voxlearning.utopia.service.rstaff.api.service.ActivityReportBaseDataService;
import com.voxlearning.utopia.service.rstaff.impl.dao.ActivityReportBaseDataPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Service(interfaceClass = ActivityReportBaseDataService.class)
@ExposeService(interfaceClass = ActivityReportBaseDataService.class)
public class ActivityReportDataServiceImpl extends SpringContainerSupport implements ActivityReportBaseDataService {

    @Inject
    private ActivityReportBaseDataPersistence activityReportBaseDataPersistence;

    @Override
    public MapMessage saveActivityReportBaseDatas(List<ActivityReportBaseData> inserts) {
        activityReportBaseDataPersistence.inserts(inserts);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAll() {
        activityReportBaseDataPersistence.deleteAll();
        return MapMessage.successMessage();
    }

    @Override
    public List<ActivityReportBaseData> loadAllActivityReportBaseDatas() {
        return activityReportBaseDataPersistence.loadAllActivityReportBaseDatas();
    }

    @Override
    public List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityType(String activityCode) {
        return activityReportBaseDataPersistence.loadActivityReportBaseDatasByActivityType(activityCode);
    }

    @Override
    public List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityId(String activityId) {
        return activityReportBaseDataPersistence.loadActivityReportBaseDatasByActivityId(activityId);
    }


}
