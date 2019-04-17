package com.voxlearning.utopia.service.business.impl.activity.service.api;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;

import java.util.List;


public interface ActivityReportClassSnapshotDataService {

    MapMessage saveActivityReportClassSnapshotData(ActivityReportClassSnapshotData data);

    MapMessage saveActivityReportClassSnapshotDatas(List<ActivityReportClassSnapshotData> inserts);

    MapMessage deleteActivityReportClassSnapshotData(String activityId);
}