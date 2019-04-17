package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordMeeting;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordMeetingLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordMeetingDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * WorkRecordMeetingLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordMeetingLoader.class)
@ExposeService(interfaceClass = WorkRecordMeetingLoader.class)
public class WorkRecordMeetingLoaderImpl extends SpringContainerSupport implements WorkRecordMeetingLoader {

    @Inject
    WorkRecordMeetingDao workRecordMeetingDao;

    @Override
    public WorkRecordMeeting load(String id){
        return workRecordMeetingDao.load(id);
    }

    @Override
    public Map<String,WorkRecordMeeting> loads(Collection<String> ids){
        return workRecordMeetingDao.loads(ids);
    }

    @Override
    public List<WorkRecordMeeting> findByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(userIds) || startDate == null || endDate == null){
            return Collections.emptyList();
        }
        return workRecordMeetingDao.findByWorkersAndTime(userIds, startDate, endDate);
    }
}
