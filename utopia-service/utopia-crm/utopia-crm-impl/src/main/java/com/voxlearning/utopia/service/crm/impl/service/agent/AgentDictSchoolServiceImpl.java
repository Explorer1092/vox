package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentDictSchoolService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentDictSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentDictSchoolServiceImpl
 *
 * @author song.wang
 * @date 2017/8/3
 */
@Named
@Service(interfaceClass = AgentDictSchoolService.class)
@ExposeService(interfaceClass = AgentDictSchoolService.class)
public class AgentDictSchoolServiceImpl extends SpringContainerSupport implements AgentDictSchoolService {
    @Inject
    AgentDictSchoolPersistence agentDictSchoolPersistence;

    @Override
    public int deleteDictDimSchool(Long id) {
        return agentDictSchoolPersistence.deleteDictDimSchool(id);
    }

    @Override
    public AgentDictSchool replace(AgentDictSchool dictSchool) {
        return agentDictSchoolPersistence.replace(dictSchool);
    }

    @Override
    public void insert(AgentDictSchool agentDictSchool) {
        agentDictSchoolPersistence.insert(agentDictSchool);
    }

    @Override
    public void upsert(AgentDictSchool agentDictSchool) {
        agentDictSchoolPersistence.upsert(agentDictSchool);
    }

    @Override
    public void unsetField(String fieldName, Long documentId) {
        agentDictSchoolPersistence.unsetField(fieldName,documentId);
    }
}
