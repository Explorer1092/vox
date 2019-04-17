package com.voxlearning.utopia.service.business.impl.activity.service.api;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;

import java.util.List;


public interface ActivityReportCollectDataService {

    MapMessage saveActivityReportCollectDatas(List<ActivityReportCollectData> inserts);

    MapMessage deleteAll();

    MapMessage deleteByActivityId(String activityId);
}
