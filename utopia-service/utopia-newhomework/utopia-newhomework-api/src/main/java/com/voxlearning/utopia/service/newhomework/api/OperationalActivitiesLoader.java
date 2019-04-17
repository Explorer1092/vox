package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180529")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface OperationalActivitiesLoader extends IPingable {
    //母亲节报告
    @Idempotent
    MapMessage fetchMotherDayJztReport(String hid, Long sid, User user);

    MapMessage rewardStudent(String hid, Long sid, User user);
}
