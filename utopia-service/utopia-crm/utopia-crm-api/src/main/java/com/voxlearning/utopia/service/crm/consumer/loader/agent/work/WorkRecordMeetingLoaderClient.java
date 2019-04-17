package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordMeeting;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordMeetingLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * WorkRecordMeetingLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordMeetingLoaderClient implements WorkRecordMeetingLoader {

    @ImportService(interfaceClass = WorkRecordMeetingLoader.class)
    private WorkRecordMeetingLoader remoteReference;

    @Override
    public WorkRecordMeeting load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String,WorkRecordMeeting> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }

    @Override
    public List<WorkRecordMeeting> findByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        return remoteReference.findByWorkersAndTime(userIds,startDate,endDate);
    }
}
