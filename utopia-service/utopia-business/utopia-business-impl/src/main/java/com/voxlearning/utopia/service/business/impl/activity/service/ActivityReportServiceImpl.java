package com.voxlearning.utopia.service.business.impl.activity.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.impl.activity.dao.ActivityReportStudentDataPersistence;
import com.voxlearning.utopia.service.business.impl.activity.service.api.ActivityReportService;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class ActivityReportServiceImpl implements ActivityReportService {

    @Inject
    private ActivityReportStudentDataPersistence studentDataPersistence;

    @Override
    public MapMessage deleteActivityReportStudentDataById(String id) {
        studentDataPersistence.deleteActivityReportStudentDataById(id);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveActivityReportStudentData(ActivityReportStudentData activityReportStudentData) {
        studentDataPersistence.insert(activityReportStudentData);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveActivityReportStudentData(List<ActivityReportStudentData> list) {
        studentDataPersistence.inserts(list);
        return MapMessage.successMessage();
    }
}
