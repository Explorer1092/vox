package com.voxlearning.utopia.service.newhomework.consumer;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.service.WeekReportService;

import java.util.List;

public class WeekReportServiceClient implements WeekReportService {
    @ImportService(interfaceClass = WeekReportService.class)
    private WeekReportService reference;

    @Override
    public MapMessage shareWholeReport(Long tid, String startTime, List<String> groupIdToReportIds,Long realId) {
        return reference.shareWholeReport(tid, startTime, groupIdToReportIds,realId);
    }

    @Override
    public MapMessage teacherPushMessage(Long tid,  List<String> groupIdToReportIds,String endTime) {
        return reference.teacherPushMessage(tid, groupIdToReportIds,endTime);
    }

    @Override
    public void pushMessageToTeacher(List<Long> teacherIds) {
        reference.pushMessageToTeacher(teacherIds);
    }
}
