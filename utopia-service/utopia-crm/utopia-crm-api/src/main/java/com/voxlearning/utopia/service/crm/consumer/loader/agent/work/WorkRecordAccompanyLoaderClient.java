package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordAccompany;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordAccompanyLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * WorkRecordAccompanyLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordAccompanyLoaderClient implements WorkRecordAccompanyLoader {

    @ImportService(interfaceClass = WorkRecordAccompanyLoader.class)
    private WorkRecordAccompanyLoader remoteReference;

    @Override
    public WorkRecordAccompany load(String id){
        return remoteReference.load(id);
    }

    @Override
    public List<WorkRecordAccompany> loadByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        return remoteReference.loadByWorkersAndTime(userIds,startDate,endDate);
    }

    @Override
    public List<WorkRecordAccompany> loadByBusinessRecordId(String businessRecordId){
        return remoteReference.loadByBusinessRecordId(businessRecordId);
    }

}
