package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentUserSchoolService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentUserSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * AgentUserSchoolServiceImpl
 *
 * @author song.wang
 * @date 2016/12/5
 */
@Named
@Service(interfaceClass = AgentUserSchoolService.class)
@ExposeService(interfaceClass = AgentUserSchoolService.class)
public class AgentUserSchoolServiceImpl extends SpringContainerSupport implements AgentUserSchoolService {

    @Inject
    private AgentUserSchoolPersistence agentUserSchoolPersistence;

    @Override
    public Long persist(AgentUserSchool userSchool) {
        return agentUserSchoolPersistence.persist(userSchool);
    }

    @Override
    public Boolean update(Long userSchoolId, AgentUserSchool userSchool) {
        return agentUserSchoolPersistence.update(userSchoolId, userSchool);
    }

    @Override
    public int delete(Long id) {
        return agentUserSchoolPersistence.delete(id);
    }
}
