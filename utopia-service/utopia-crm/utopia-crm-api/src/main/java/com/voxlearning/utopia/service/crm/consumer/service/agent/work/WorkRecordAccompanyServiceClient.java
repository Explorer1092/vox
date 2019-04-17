package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordAccompany;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordAccompanyService;

/**
 * WorkRecordAccompanyServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordAccompanyServiceClient implements WorkRecordAccompanyService {

    @ImportService(interfaceClass = WorkRecordAccompanyService.class)
    private WorkRecordAccompanyService remoteReference;

    @Override
    public String insert(WorkRecordAccompany workRecordAccompany){
        return remoteReference.insert(workRecordAccompany);
    }

}
