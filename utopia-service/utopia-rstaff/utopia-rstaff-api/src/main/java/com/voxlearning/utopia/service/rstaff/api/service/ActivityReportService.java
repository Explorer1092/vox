package com.voxlearning.utopia.service.rstaff.api.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181029")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface ActivityReportService {

    List<ActivityReportClassSnapshotData> loadClassSnapshot(String activityId, Long clazzId);

    List<ActivityReportStudentData> loadStudentData(String activityId, Long clazzId);

    MapMessage deleteClassSnapshotByActivityIdCurDate(String activityId, String curDate);
}
