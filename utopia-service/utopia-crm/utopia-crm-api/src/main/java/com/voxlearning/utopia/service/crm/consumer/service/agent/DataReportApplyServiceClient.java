package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.DataReportApply;
import com.voxlearning.utopia.service.crm.api.service.agent.DataReportApplyService;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/7
 */
public class DataReportApplyServiceClient implements DataReportApplyService {
    @ImportService(interfaceClass = DataReportApplyService.class)
    private DataReportApplyService remoteReference;

    @Override
    public Long persist(DataReportApply apply) {
        return remoteReference.persist(apply);
    }

    @Override
    public DataReportApply update(DataReportApply apply) {
        return remoteReference.update(apply);
    }

    @Override
    public Boolean updateWorkflowId(Long id, Long workflowId) {
        return remoteReference.updateWorkflowId(id, workflowId);
    }
}
