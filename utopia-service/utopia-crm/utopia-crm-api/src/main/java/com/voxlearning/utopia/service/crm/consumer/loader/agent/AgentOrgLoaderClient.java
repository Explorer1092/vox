package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentOrgLoader;

import java.util.List;

/**
 * @author song.wang
 * @date 2016/12/23
 */
public class AgentOrgLoaderClient implements AgentOrgLoader {

    @ImportService(interfaceClass = AgentOrgLoader.class)
    private AgentOrgLoader remoteReference;

    @Override
    public Boolean isDictSchool(Long schoolId) {
        return remoteReference.isDictSchool(schoolId);
    }

    @Override
    public AgentGroup loadAgentGroupByUserId(Long userId) {
        return remoteReference.loadAgentGroupByUserId(userId);
    }

    @Override
    public List<SchoolLevel> fetchUserServeSchoolLevels(Long userId) {
        return remoteReference.fetchUserServeSchoolLevels(userId);
    }


}
