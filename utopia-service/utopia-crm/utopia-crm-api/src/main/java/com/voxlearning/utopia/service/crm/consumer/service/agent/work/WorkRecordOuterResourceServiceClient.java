package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordOuterResourceService;

import java.util.Collection;
import java.util.List;

/**
 * WorkRecordOuterResourceServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordOuterResourceServiceClient implements WorkRecordOuterResourceService {

    @ImportService(interfaceClass = WorkRecordOuterResourceService.class)
    private WorkRecordOuterResourceService remoteReference;

    @Override
    public String insert(WorkRecordOuterResource workRecordOuterResource){
        return remoteReference.insert(workRecordOuterResource);
    }

    @Override
    public List<String> inserts(Collection<WorkRecordOuterResource> workRecordOuterResourceList){
        return remoteReference.inserts(workRecordOuterResourceList);
    }

    @Override
    public void updateWorkRecordIdAndType(Collection<String> ids, String workRecordId, AgentWorkRecordType workRecordType){
        remoteReference.updateWorkRecordIdAndType(ids,workRecordId,workRecordType);
    }
}
