package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordResourceExtensionService;

import java.util.Collection;
import java.util.List;

/**
 * WorkRecordResourceExtensionServiceClient
 *
 * @author deliang.che
 * @since  2019/1/19
 */
public class WorkRecordResourceExtensionServiceClient implements WorkRecordResourceExtensionService {

    @ImportService(interfaceClass = WorkRecordResourceExtensionService.class)
    private WorkRecordResourceExtensionService remoteReference;

    @Override
    public String insert(WorkRecordResourceExtension workRecordResourceExtension){
        return remoteReference.insert(workRecordResourceExtension);
    }

    @Override
    public List<String> inserts(Collection<WorkRecordResourceExtension> workRecordResourceExtensionList){
        return remoteReference.inserts(workRecordResourceExtensionList);
    }

}
