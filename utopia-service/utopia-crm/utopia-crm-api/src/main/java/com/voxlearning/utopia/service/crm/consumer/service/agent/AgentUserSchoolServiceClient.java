package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentUserSchoolService;

/**
 * AgentUserSchoolServiceClient
 *
 * @author song.wang
 * @date 2016/12/5
 */
public class AgentUserSchoolServiceClient implements AgentUserSchoolService {

    @ImportService(interfaceClass = AgentUserSchoolService.class)
    private AgentUserSchoolService remoteReference;

    @Override
    public Long persist(AgentUserSchool userSchool) {
        return remoteReference.persist(userSchool);
    }

    @Override
    public Boolean update(Long userSchoolId, AgentUserSchool userSchool) {
        return remoteReference.update(userSchoolId, userSchool);
    }


    @Override
    public int delete(Long id) {
        return remoteReference.delete(id);
    }
}
