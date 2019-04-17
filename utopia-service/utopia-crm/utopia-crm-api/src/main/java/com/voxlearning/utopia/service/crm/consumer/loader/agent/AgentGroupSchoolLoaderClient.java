package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupSchoolLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupSchoolLoaderClient
 *
 * @author song.wang
 * @date 2017/8/8
 */
public class AgentGroupSchoolLoaderClient implements AgentGroupSchoolLoader {

    @ImportService(interfaceClass = AgentGroupSchoolLoader.class)
    private AgentGroupSchoolLoader remoteReference;

    @Override
    public List<AgentGroupSchool> findByGroupId(Long groupId) {
        return remoteReference.findByGroupId(groupId);
    }

    @Override
    public Map<Long, List<AgentGroupSchool>> findByGroupIds(Collection<Long> groupIds) {
        return remoteReference.findByGroupIds(groupIds);
    }

    @Override
    public AgentGroupSchool findBySchoolId(Long schoolId) {
        return remoteReference.findBySchoolId(schoolId);
    }

    @Override
    public Map<Long, AgentGroupSchool> findBySchoolIds(Collection<Long> schoolIds) {
        return remoteReference.findBySchoolIds(schoolIds);
    }

}
