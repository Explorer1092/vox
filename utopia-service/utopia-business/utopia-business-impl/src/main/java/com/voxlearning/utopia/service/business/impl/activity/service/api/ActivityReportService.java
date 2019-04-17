package com.voxlearning.utopia.service.business.impl.activity.service.api;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;

import java.util.List;

public interface ActivityReportService {

    MapMessage deleteActivityReportStudentDataById(String id);

    MapMessage saveActivityReportStudentData(ActivityReportStudentData activityReportStudentData);

    MapMessage saveActivityReportStudentData(List<ActivityReportStudentData> list);

}