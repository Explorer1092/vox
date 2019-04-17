package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentDictSchoolLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentDictSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AgentDictSchoolLoaderImpl
 *
 * @author song.wang
 * @date 2017/8/3
 */
@Named
@Service(interfaceClass = AgentDictSchoolLoader.class)
@ExposeService(interfaceClass = AgentDictSchoolLoader.class)
public class AgentDictSchoolLoaderImpl extends SpringContainerSupport implements AgentDictSchoolLoader {

    @Inject
    AgentDictSchoolPersistence agentDictSchoolPersistence;

    @Override
    public AgentDictSchool load(Long dictId) {
        return agentDictSchoolPersistence.load(dictId);
    }

    @Deprecated
    @Override
    public List<AgentDictSchool> findAllDictSchool() {
        return agentDictSchoolPersistence.findAllDictSchool();
    }

    @Override
    public AgentDictSchool findBySchoolId(Long schoolId) {
        return agentDictSchoolPersistence.findBySchoolId(schoolId);
    }

    public Map<Long, AgentDictSchool> findBySchoolIds(Collection<Long> schoolIds){
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return agentDictSchoolPersistence.findBySchoolIds(schoolIds);
    }

    @Override
    public List<AgentDictSchool> findByCountyCode(Integer countyCode) {
        return agentDictSchoolPersistence.findByCountyCode(countyCode);
    }

    @Override
    public Map<Integer, List<AgentDictSchool>> findByCountyCodes(Collection<Integer> countyCodes) {
        if(CollectionUtils.isEmpty(countyCodes)){
            return Collections.emptyMap();
        }
        return agentDictSchoolPersistence.findByCountyCodes(countyCodes);
    }
}
