package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180420")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface WeekReportService extends IPingable {

    //分享整份作业报告
    MapMessage shareWholeReport(Long tid, String startTime, List<String> groupIdToReportIds,Long realId);


    //预留接口
    MapMessage teacherPushMessage(Long tid, List<String> groupIdToReportIds, String endTime);


    //预留接口
    void pushMessageToTeacher(List<Long> teacherIds);

}
