package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentModifyDictSchoolApplyService;

import java.util.Collection;
import java.util.List;

/**
 * AgentModifyDictSchoolApplyServiceClient
 *
 * @author song.wang
 * @date 2016/12/28
 */
public class AgentModifyDictSchoolApplyServiceClient implements AgentModifyDictSchoolApplyService {

    @ImportService(interfaceClass = AgentModifyDictSchoolApplyService.class)
    private AgentModifyDictSchoolApplyService remoteReference;

    @Override
    public Long addApply(AgentModifyDictSchoolApply agentModifyDictSchoolApply) {
        return remoteReference.addApply(agentModifyDictSchoolApply);
    }

    @Override
    public Boolean updateStatus(Long id, ApplyStatus status) {
        return remoteReference.updateStatus(id, status);
    }

    @Override
    public Boolean updateWorkflowId(Long id, Long workflowId) {
        return remoteReference.updateWorkflowId(id, workflowId);
    }

    @Override
    public List<Long> updateApplyResolvedByIds(Collection<Long> applyIds) {
        return remoteReference.updateApplyResolvedByIds(applyIds);
    }

    @Override
    public Boolean updateApplyResolved(Long applyId) {
        return remoteReference.updateApplyResolved(applyId);
    }


}
