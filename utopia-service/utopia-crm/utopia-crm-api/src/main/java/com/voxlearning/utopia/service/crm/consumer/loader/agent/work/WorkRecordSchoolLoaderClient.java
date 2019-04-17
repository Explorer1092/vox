package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordSchoolLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * WorkRecordSchoolLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordSchoolLoaderClient implements WorkRecordSchoolLoader {

    @ImportService(interfaceClass = WorkRecordSchoolLoader.class)
    private WorkRecordSchoolLoader remoteReference;

    @Override
    public WorkRecordSchool load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String,WorkRecordSchool> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }


    @Override
    public List<WorkRecordSchool> findByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        return remoteReference.findByWorkersAndTime(userIds,startDate,endDate);
    }

    @Override
    public List<WorkRecordSchool> findBySchoolId(Long schoolId){
        return remoteReference.findBySchoolId(schoolId);
    }

}
