package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupSchoolService;

import java.util.Collection;

/**
 * AgentGroupSchoolServiceClient
 *
 * @author song.wang
 * @date 2017/8/8
 */
public class AgentGroupSchoolServiceClient implements AgentGroupSchoolService {

    @ImportService(interfaceClass = AgentGroupSchoolService.class)
    private AgentGroupSchoolService remoteReference;

    @Override
    public Integer deleteByGroupId(Long groupId) {
        return remoteReference.deleteByGroupId(groupId);
    }

    @Override
    public Integer deleteByGroupAndRegion(Long groupId, Integer regionCode) {
        return remoteReference.deleteByGroupAndRegion(groupId, regionCode);
    }

    @Override
    public Integer deleteBySchoolId(Long schoolId) {
        return remoteReference.deleteBySchoolId(schoolId);
    }

    @Override
    public MapMessage insert(AgentGroupSchool agentGroupSchool) {
        return remoteReference.insert(agentGroupSchool);
    }

    @Override
    public MapMessage update(AgentGroupSchool agentGroupSchool) {
        return remoteReference.update(agentGroupSchool);
    }

    @Override
    public Integer deleteByGroupAndRegions(Long groupId, Collection<Integer> regionCodes) {
        return remoteReference.deleteByGroupAndRegions(groupId, regionCodes);
    }

}
