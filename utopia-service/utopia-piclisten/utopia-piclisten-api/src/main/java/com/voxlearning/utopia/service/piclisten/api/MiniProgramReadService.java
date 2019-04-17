package com.voxlearning.utopia.service.piclisten.api;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramRead;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
@ServiceVersion(version = "20180521")
public interface MiniProgramReadService extends IPingable {


    MiniProgramRead save(MiniProgramRead po);

    void addNoticeFormId(Long pid, String formId);

    MapMessage setUserDayPlan(Long pid, Long uid,int planMinutes, int remind, String remindTime);
    MapMessage getUserDayPlan(Long pid,Long uid);

    void incrReadData(Long uid,Long readMillis, Integer readWords);

    MapMessage getTodayReadData(Long pid,Long uid);

    List<Long> getWeekReadTimes(Long uid);

    Long getTotalReadTimes(Long uid);

    MiniProgramRead loadByUid(Long uid);
}
