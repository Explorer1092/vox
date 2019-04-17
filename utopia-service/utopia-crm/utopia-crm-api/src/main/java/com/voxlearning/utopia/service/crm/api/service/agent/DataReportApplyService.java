package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;

import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/7
 */
@ServiceVersion(version = "20170607")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface DataReportApplyService extends IPingable {
    Long persist(DataReportApply apply);

    DataReportApply update(DataReportApply apply);

    Boolean updateWorkflowId(Long id, Long workflowId);
}
