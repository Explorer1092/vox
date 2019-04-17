package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentUserSchoolLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentUserSchoolPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AgentUserSchoolLoaderImpl
 *
 * @author song.wang
 * @date 2016/12/6
 */
@Named
@Service(interfaceClass = AgentUserSchoolLoader.class)
@ExposeService(interfaceClass = AgentUserSchoolLoader.class)
public class AgentUserSchoolLoaderImpl extends SpringContainerSupport implements AgentUserSchoolLoader {

    @Inject
    private AgentUserSchoolPersistence agentUserSchoolPersistence;

    @Override
    public List<AgentUserSchool> findAll() {
        return agentUserSchoolPersistence.findAll();
    }

    @Override
    public List<AgentUserSchool> findByUserId(Long userId) {
        if (userId == null || userId <= 0) return Collections.emptyList();
        return agentUserSchoolPersistence.findByUser(userId);
    }

    @Override
    public Map<Long, List<AgentUserSchool>> findByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyMap();

        return agentUserSchoolPersistence.findByUsers(userIds);
    }

    @Override
    public List<AgentUserSchool> findBySchoolId(Long schoolId) {
        if (schoolId == null || schoolId <= 0L) return Collections.emptyList();
        return agentUserSchoolPersistence.findBySchool(schoolId);
    }

    @Override
    public List<AgentUserSchool> findBySchoolIds(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) return Collections.emptyList();

        Map<Long, List<AgentUserSchool>> userSchools = agentUserSchoolPersistence.findBySchools(schoolIds);
        List<AgentUserSchool> retList = new ArrayList<>();

        for (List<AgentUserSchool> item : userSchools.values()) {
            for (AgentUserSchool school : item) {
                if (!retList.contains(school)) {
                    retList.add(school);
                }
            }
        }

        return retList;
    }
}
