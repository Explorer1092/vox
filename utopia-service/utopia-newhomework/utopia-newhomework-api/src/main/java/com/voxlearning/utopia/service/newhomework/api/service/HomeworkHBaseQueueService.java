package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用于双写作业数据到HBase
 *
 * @author xuesong.zhang
 * @since 2017/8/17
 */
@ServiceVersion(version = "20170817")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface HomeworkHBaseQueueService extends IPingable {

    @NoResponseWait
    void sendSubHomeworkResult(List<SubHomeworkResult> results);

    @NoResponseWait
    void sendSubHomeworkResultAnswer(List<SubHomeworkResultAnswer> results);

    @NoResponseWait
    void sendSubHomeworkProcessResult(List<SubHomeworkProcessResult> results);
}
