package com.voxlearning.utopia.service.crm.consumer.service.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordMeeting;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordMeetingService;

/**
 * WorkRecordMeetingServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordMeetingServiceClient implements WorkRecordMeetingService {

    @ImportService(interfaceClass = WorkRecordMeetingService.class)
    private WorkRecordMeetingService remoteReference;

    @Override
    public String insert(WorkRecordMeeting workRecordMeeting){
        return remoteReference.insert(workRecordMeeting);
    }

}
