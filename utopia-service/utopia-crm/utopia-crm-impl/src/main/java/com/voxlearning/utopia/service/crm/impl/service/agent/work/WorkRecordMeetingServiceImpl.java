package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordMeeting;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordMeetingService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordMeetingDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * WorkRecordMeetingServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordMeetingService.class)
@ExposeService(interfaceClass = WorkRecordMeetingService.class)
public class WorkRecordMeetingServiceImpl extends SpringContainerSupport implements WorkRecordMeetingService {
    @Inject
    WorkRecordMeetingDao workRecordMeetingDao;

    @Override
    public String insert(WorkRecordMeeting workRecordMeeting){
        workRecordMeetingDao.insert(workRecordMeeting);
        return workRecordMeeting.getId();
    }
}
