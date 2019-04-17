package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentGroupSchoolLoader;
import com.voxlearning.utopia.service.crm.impl.persistence.agent.AgentGroupSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AgentGroupSchoolLoaderImpl
 *
 * @author song.wang
 * @date 2017/8/7
 */

@Named
@Service(interfaceClass = AgentGroupSchoolLoader.class)
@ExposeService(interfaceClass = AgentGroupSchoolLoader.class)
public class AgentGroupSchoolLoaderImpl extends SpringContainerSupport implements AgentGroupSchoolLoader {

    @Inject private AgentGroupSchoolPersistence agentGroupSchoolPersistence;

    @Override
    public List<AgentGroupSchool> findByGroupId(Long groupId) {
        return agentGroupSchoolPersistence.findByGroupId(groupId);
    }

    @Override
    public Map<Long, List<AgentGroupSchool>> findByGroupIds(Collection<Long> groupIds) {
        if(CollectionUtils.isEmpty(groupIds)){
            return Collections.emptyMap();
        }
        return agentGroupSchoolPersistence.findByGroupIds(groupIds);
    }

    @Override
    public AgentGroupSchool findBySchoolId(Long schoolId) {
        return agentGroupSchoolPersistence.findBySchoolId(schoolId);
    }

    @Override
    public Map<Long, AgentGroupSchool> findBySchoolIds(Collection<Long> schoolIds) {
        return agentGroupSchoolPersistence.findBySchoolIds(schoolIds);
    }

}
