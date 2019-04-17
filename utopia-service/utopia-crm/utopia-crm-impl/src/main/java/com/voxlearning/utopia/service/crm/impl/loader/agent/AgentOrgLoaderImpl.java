package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentOrgLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentDictSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author song.wang
 * @date 2016/12/23
 */

@Named
@Service(interfaceClass = AgentOrgLoader.class)
@ExposeService(interfaceClass = AgentOrgLoader.class)
public class AgentOrgLoaderImpl extends SpringContainerSupport implements AgentOrgLoader {

    @Inject
    private AgentDictSchoolPersistence agentDictSchoolPersistence;
    @Inject
    private AgentGroupLoaderImpl agentGroupLoader;
    @Inject
    private AgentGroupUserLoaderImpl agentGroupUserLoader;

    @Override
    public Boolean isDictSchool(Long schoolId) {
        if (schoolId == null) {
            return false;
        }
        AgentDictSchool dictSchool = agentDictSchoolPersistence.findBySchoolId(schoolId);
        return dictSchool != null;
    }

    /**
     *  取一个用户的第一个部门
     * @param userId agentUserId
     * @return
     */
    @Override
    public AgentGroup loadAgentGroupByUserId(Long userId) {
        List<AgentGroupUser> agentGroupUsers = agentGroupUserLoader.findByUserId(userId);
        if (CollectionUtils.isEmpty(agentGroupUsers)) {
            return null;
        }
        AgentGroupUser agentGroupUser = agentGroupUsers.get(0);
        return agentGroupLoader.load(agentGroupUser.getGroupId());
    }

    @Override
    public List<SchoolLevel> fetchUserServeSchoolLevels(Long userId) {
        List<SchoolLevel> schoolLevelList = new ArrayList<>();
        AgentGroup group = loadAgentGroupByUserId(userId);
        if(group != null && CollectionUtils.isNotEmpty(group.fetchServiceTypeList())){
            for(AgentServiceType serviceType : group.fetchServiceTypeList()){
                if(serviceType == AgentServiceType.PRE_SCHOOL){
                    schoolLevelList.add(SchoolLevel.INFANT);
                }else if(serviceType == AgentServiceType.JUNIOR_SCHOOL){
                    schoolLevelList.add(SchoolLevel.JUNIOR);
                }else if(serviceType == AgentServiceType.MIDDLE_SCHOOL){
                    schoolLevelList.add(SchoolLevel.MIDDLE);
                }else if(serviceType == AgentServiceType.SENIOR_SCHOOL){
                    schoolLevelList.add(SchoolLevel.HIGH);
                }
            }
        }
        return schoolLevelList;
    }


}
