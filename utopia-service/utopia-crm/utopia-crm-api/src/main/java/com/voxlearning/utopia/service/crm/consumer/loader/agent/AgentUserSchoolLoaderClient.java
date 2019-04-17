package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentUserSchoolLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentUserSchoolLoaderClient
 *
 * @author song.wang
 * @date 2016/12/6
 */
public class AgentUserSchoolLoaderClient implements AgentUserSchoolLoader {

    @ImportService(interfaceClass = AgentUserSchoolLoader.class)
    private AgentUserSchoolLoader remoteReference;

    @Override
    public List<AgentUserSchool> findAll() {
        return remoteReference.findAll();
    }

    @Override
    public List<AgentUserSchool> findByUserId(Long userId) {
        return remoteReference.findByUserId(userId);
    }

    @Override
    public Map<Long, List<AgentUserSchool>> findByUserIds(Collection<Long> userIds) {
        return remoteReference.findByUserIds(userIds);
    }

    @Override
    public List<AgentUserSchool> findBySchoolId(Long schoolId) {
        return remoteReference.findBySchoolId(schoolId);
    }

    @Override
    public List<AgentUserSchool> findBySchoolIds(Collection<Long> schoolIds) {
        return remoteReference.findBySchoolIds(schoolIds);
    }
}
