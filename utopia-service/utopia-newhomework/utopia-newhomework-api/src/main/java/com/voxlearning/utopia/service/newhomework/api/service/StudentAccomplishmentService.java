package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20170113")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface StudentAccomplishmentService {

    /**
     * 今天是否完成过作业，补做不算
     *
     * @param studentId student id
     * @return result future
     */
    @Async
    AlpsFuture<Boolean> finishedHomeworkWithinToday(Long studentId);

    /**
     * 今天是否完成了测验，或者完成过作业，补做不算
     *
     * @param studentId student id
     * @return result future
     */
    @Async
    AlpsFuture<Boolean> finishedQuizOrHomeworkWithinToday(Long studentId);
}
