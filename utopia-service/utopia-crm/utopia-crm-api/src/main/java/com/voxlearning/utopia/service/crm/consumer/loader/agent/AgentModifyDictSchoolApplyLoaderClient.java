package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentModifyDictSchoolApplyLoader;

import java.util.List;
import java.util.Map;

/**
 * AgentModifyDictSchoolApplyLoaderClient
 *
 * @author song.wang
 * @date 2016/12/28
 */
public class AgentModifyDictSchoolApplyLoaderClient implements AgentModifyDictSchoolApplyLoader {

    @ImportService(interfaceClass = AgentModifyDictSchoolApplyLoader.class)
    private AgentModifyDictSchoolApplyLoader remoteReference;

    @Override
    public List<AgentModifyDictSchoolApply> findBySchoolId(Long schoolId) {
        return remoteReference.findBySchoolId(schoolId);
    }

    @Override
    public AgentModifyDictSchoolApply findByWorkflowId(Long workflowId) {
        return remoteReference.findByWorkflowId(workflowId);
    }

    @Override
    public List<AgentModifyDictSchoolApply> findByStatusAndResolved(ApplyStatus status, Boolean resolved) {
        return remoteReference.findByStatusAndResolved(status, resolved);
    }

    public Map<Long, AgentModifyDictSchoolApply> findByIds(List<Long> applyIds) {
        return remoteReference.findByIds(applyIds);
    }
}
