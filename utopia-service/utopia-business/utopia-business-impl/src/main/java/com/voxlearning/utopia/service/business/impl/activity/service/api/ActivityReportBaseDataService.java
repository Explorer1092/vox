package com.voxlearning.utopia.service.business.impl.activity.service.api;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;

import java.util.List;

public interface ActivityReportBaseDataService {

    MapMessage saveActivityReportBaseDatas(List<ActivityReportBaseData> inserts);

    MapMessage deleteAll();

    List<ActivityReportBaseData> loadAllActivityReportBaseDatas();

    List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityType(String activityType);

    List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityId(String activityId);

}
