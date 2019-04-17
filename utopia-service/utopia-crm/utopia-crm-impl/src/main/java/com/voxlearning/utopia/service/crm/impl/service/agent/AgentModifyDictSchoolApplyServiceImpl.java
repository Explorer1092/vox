package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentModifyDictSchoolApplyService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentModifyDictSchoolApplyPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * AgentModifyDictSchoolApplyServiceImpl
 *
 * @author song.wang
 * @date 2016/12/28
 */
@Named
@Service(interfaceClass = AgentModifyDictSchoolApplyService.class)
@ExposeService(interfaceClass = AgentModifyDictSchoolApplyService.class)
public class AgentModifyDictSchoolApplyServiceImpl extends SpringContainerSupport implements AgentModifyDictSchoolApplyService {

    @Inject private AgentModifyDictSchoolApplyPersistence persistence;

    @Override
    public Long addApply(AgentModifyDictSchoolApply agentModifyDictSchoolApply) {
        persistence.insert(agentModifyDictSchoolApply);
        return agentModifyDictSchoolApply.getId();
    }

    @Override
    public Boolean updateStatus(Long id, ApplyStatus status) {
        if (id == null || status == null) {
            return false;
        }
        AgentModifyDictSchoolApply item = persistence.load(id);
        if (item == null) {
            return false;
        }
        item.setStatus(status);
        return persistence.replace(item) != null;
    }

    @Override
    public Boolean updateWorkflowId(Long id, Long workflowId) {
        AgentModifyDictSchoolApply item = persistence.load(id);
        if (item == null) {
            return false;
        }
        item.setWorkflowId(workflowId);
        return persistence.replace(item) != null;
    }

    @Override
    public List<Long> updateApplyResolvedByIds(Collection<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }
        List<Long> updatedWorkFlows = new ArrayList<>();
        applyIds.forEach(p -> {
            if (updateApplyResolved(p)) {
                updatedWorkFlows.add(p);
            }
        });
        return updatedWorkFlows;
    }

    @Override
    public Boolean updateApplyResolved(Long applyIds) {
        AgentModifyDictSchoolApply item = persistence.load(applyIds);
        if (item == null) {
            return false;
        }
        item.setResolved(true);
        return persistence.replace(item) != null;
    }
}
