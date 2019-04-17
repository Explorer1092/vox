package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentDictSchoolService;

/**
 * AgentDictSchoolServiceClient
 *
 * @author song.wang
 * @date 2017/8/3
 */
public class AgentDictSchoolServiceClient implements AgentDictSchoolService {

    @ImportService(interfaceClass = AgentDictSchoolService.class)
    private AgentDictSchoolService remoteReference;

    @Override
    public int deleteDictDimSchool(Long id) {
        return remoteReference.deleteDictDimSchool(id);
    }

    @Override
    public AgentDictSchool replace(AgentDictSchool dictSchool) {
        return remoteReference.replace(dictSchool);
    }

    @Override
    public void insert(AgentDictSchool agentDictSchool) {
        remoteReference.insert(agentDictSchool);
    }

    @Override
    public void upsert(AgentDictSchool agentDictSchool) {
        remoteReference.upsert(agentDictSchool);
    }

    @Override
    public void unsetField(String fieldName, Long documentId) {
        remoteReference.unsetField(fieldName,documentId);
    }
}
