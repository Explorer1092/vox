package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentGroupSchoolService;
import com.voxlearning.utopia.service.crm.impl.persistence.agent.AgentGroupSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * AgentGroupSchoolServiceImpl
 *
 * @author song.wang
 * @date 2017/8/7
 */
@Named
@Service(interfaceClass = AgentGroupSchoolService.class)
@ExposeService(interfaceClass = AgentGroupSchoolService.class)
public class AgentGroupSchoolServiceImpl extends SpringContainerSupport implements AgentGroupSchoolService {

    @Inject
    AgentGroupSchoolPersistence agentGroupSchoolPersistence;

    @Override
    public Integer deleteByGroupId(Long groupId) {
        return agentGroupSchoolPersistence.deleteByGroupId(groupId);
    }

    @Override
    public Integer deleteByGroupAndRegion(Long groupId, Integer regionCode) {
        return agentGroupSchoolPersistence.deleteByGroupAndRegion(groupId, regionCode);
    }

    @Override
    public Integer deleteBySchoolId(Long schoolId) {
        return agentGroupSchoolPersistence.deleteBySchoolId(schoolId);
    }

    @Override
    public MapMessage insert(AgentGroupSchool agentGroupSchool) {
        if (null == agentGroupSchool || null == agentGroupSchool.getGroupId() || null == agentGroupSchool.getSchoolId() || null == agentGroupSchool.getRegionCode()){
            return MapMessage.errorMessage("agentGroupSchool参数不全");
        }
        agentGroupSchoolPersistence.insert(agentGroupSchool);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage update(AgentGroupSchool agentGroupSchool) {
        if ( null == agentGroupSchool || agentGroupSchool.getId() == null || null == agentGroupSchool.getGroupId() || null == agentGroupSchool.getSchoolId() || null == agentGroupSchool.getRegionCode()){
            return MapMessage.errorMessage("agentGroupSchool参数不全");
        }

        agentGroupSchoolPersistence.replace(agentGroupSchool);
        return MapMessage.successMessage();
    }


    @Override
    public Integer deleteByGroupAndRegions(Long groupId, Collection<Integer> regionCodes) {
        return agentGroupSchoolPersistence.deleteByGroupAndRegions(groupId, regionCodes);
    }

}
