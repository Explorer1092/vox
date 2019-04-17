package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.crm.api.loader.agent.TeacherAgentLoader;
import com.voxlearning.utopia.service.crm.cache.CrmCache;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Teacher Agent Loader Client
 * Created by alex on 2017/1/9.
 */
public class TeacherAgentLoaderClient {
    @Getter
    @ImportService(interfaceClass = TeacherAgentLoader.class)
    private TeacherAgentLoader remoteReference;

    public Map<String, Object> getSchoolManager(Long schoolId) {
        Map<String, Object> retInfo = new HashMap<>();
        if (schoolId == null || Objects.equals(schoolId, 0L)) {
            return retInfo;
        }

        String cacheKey = "TEACHER_AGENT_20160109_" + schoolId;

        retInfo = CrmCache.getCrmCache().load(cacheKey);
        if (retInfo != null) {
            return retInfo;
        }

        retInfo = remoteReference.getSchoolManager(schoolId);
        if (MapUtils.isNotEmpty(retInfo)) {
            CrmCache.getCrmCache().set(cacheKey, 1800, retInfo);
        }

        return retInfo;
    }
}
