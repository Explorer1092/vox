package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentModifyDictSchoolApplyLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentModifyDictSchoolApplyPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * AgentModifyDictSchoolApplyLoaderImpl
 *
 * @author song.wang
 * @date 2016/12/28
 */
@Named
@Service(interfaceClass = AgentModifyDictSchoolApplyLoader.class)
@ExposeService(interfaceClass = AgentModifyDictSchoolApplyLoader.class)
public class AgentModifyDictSchoolApplyLoaderImpl extends SpringContainerSupport implements AgentModifyDictSchoolApplyLoader {

    @Inject
    private AgentModifyDictSchoolApplyPersistence persistence;

    @Override
    public List<AgentModifyDictSchoolApply> findBySchoolId(Long schoolId) {
        return persistence.findBySchoolId(schoolId);
    }

    @Override
    public AgentModifyDictSchoolApply findByWorkflowId(Long workflowId) {
        return persistence.findByWorkflowId(workflowId);
    }

    @Override
    public List<AgentModifyDictSchoolApply> findByStatusAndResolved(ApplyStatus status, Boolean resolved) {
        return persistence.findByStatusAndResolved(status, resolved);
    }

    @Override
    public Map<Long, AgentModifyDictSchoolApply> findByIds(List<Long> applyIds) {
        return persistence.loads(applyIds);
    }
}
