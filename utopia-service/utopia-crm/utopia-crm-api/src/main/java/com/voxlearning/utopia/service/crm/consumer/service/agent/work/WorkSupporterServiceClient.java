package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkSupporter;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkSupporterService;

import java.util.Collection;
import java.util.List;

/**
 * WorkSupporterServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkSupporterServiceClient implements WorkSupporterService {

    @ImportService(interfaceClass = WorkSupporterService.class)
    private WorkSupporterService remoteReference;

    @Override
    public String insert(WorkSupporter workSupporter){
        return remoteReference.insert(workSupporter);
    }

    @Override
    public List<String> inserts(Collection<WorkSupporter> workSupporterList){
        return remoteReference.inserts(workSupporterList);
    }

}
