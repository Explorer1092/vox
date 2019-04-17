package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;

import java.util.concurrent.TimeUnit;

/**
 * AgentDictSchoolService
 *
 * @author song.wang
 * @date 2017/8/3
 */
@ServiceVersion(version = "2017.11.02")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AgentDictSchoolService extends IPingable {
    int deleteDictDimSchool(Long id);

    AgentDictSchool replace(AgentDictSchool dictSchool);

    void insert(AgentDictSchool agentDictSchool);

    void upsert(AgentDictSchool agentDictSchool);

    /**
     * 将字段置空
     * @param fieldName
     * @param documentId
     */
    void unsetField(String fieldName,Long documentId);
}
