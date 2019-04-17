package com.voxlearning.utopia.service.rstaff.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.monitor.ServiceMetric;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@ServiceMetric
public interface ActivityReportBaseDataService {


    MapMessage saveActivityReportBaseDatas(List<ActivityReportBaseData> inserts);

    MapMessage deleteAll();

    List<ActivityReportBaseData> loadAllActivityReportBaseDatas();

    List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityType(String activityType);

    List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityId(String activityId);


}
