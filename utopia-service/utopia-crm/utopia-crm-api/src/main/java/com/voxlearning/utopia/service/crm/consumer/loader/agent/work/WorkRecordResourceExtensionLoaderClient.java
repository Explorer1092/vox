package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordResourceExtensionLoader;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * WorkRecordResourceExtensionLoaderClient
 *
 * @author deliang.che
 * @since  2019/1/18
 */
public class WorkRecordResourceExtensionLoaderClient implements WorkRecordResourceExtensionLoader {

    @ImportService(interfaceClass = WorkRecordResourceExtensionLoader.class)
    private WorkRecordResourceExtensionLoader remoteReference;

    @Override
    public WorkRecordResourceExtension load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String,WorkRecordResourceExtension> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }

    @Override
    public List<WorkRecordResourceExtension> loadByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        return remoteReference.loadByWorkersAndTime(userIds,startDate,endDate);
    }
}
