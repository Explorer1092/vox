package com.voxlearning.utopia.service.rstaff.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;
import com.voxlearning.utopia.service.rstaff.api.service.ActivityReportService;
import com.voxlearning.utopia.service.rstaff.impl.dao.ActivityReportClassSnapshotDataPersistence;
import com.voxlearning.utopia.service.rstaff.impl.dao.ActivityReportStudentDataPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.List;

@Named
@ExposeService(interfaceClass = ActivityReportService.class)
public class ActivityReportServiceImpl extends SpringContainerSupport implements ActivityReportService {

    @Inject
    private ActivityReportStudentDataPersistence studentDataPersistence;
    @Inject
    private ActivityReportClassSnapshotDataPersistence classSnapshotDataPersistence;

    @Override
    public List<ActivityReportClassSnapshotData> loadClassSnapshot(String activityId, Long clazzId) {
        List<ActivityReportClassSnapshotData> snapshotData = classSnapshotDataPersistence.getByActivityIdClazzId(activityId, clazzId);
        snapshotData.sort(Comparator.comparing(ObjectIdEntity::getCreateDatetime));
        return snapshotData;
    }

    @Override
    public List<ActivityReportStudentData> loadStudentData(String activityId, Long clazzId) {
        return studentDataPersistence.getByActivityIdClazzId(activityId, clazzId);
    }

    @Override
    public MapMessage deleteClassSnapshotByActivityIdCurDate(String activityId, String curDate) {
        classSnapshotDataPersistence.deleteByActivityIdCurDate(activityId, curDate);
        return MapMessage.successMessage();
    }
}
