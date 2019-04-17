package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordSchoolService;

/**
 * WorkRecordSchoolServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordSchoolServiceClient implements WorkRecordSchoolService {

    @ImportService(interfaceClass = WorkRecordSchoolService.class)
    private WorkRecordSchoolService remoteReference;

    @Override
    public String insert(WorkRecordSchool workRecordSchool){
        return remoteReference.insert(workRecordSchool);
    }

}
