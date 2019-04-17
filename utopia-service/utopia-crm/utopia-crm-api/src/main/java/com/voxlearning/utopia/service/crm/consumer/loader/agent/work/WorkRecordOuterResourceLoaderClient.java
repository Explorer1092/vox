package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordOuterResourceLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * WorkRecordOuterResourceLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkRecordOuterResourceLoaderClient implements WorkRecordOuterResourceLoader {

    @ImportService(interfaceClass = WorkRecordOuterResourceLoader.class)
    private WorkRecordOuterResourceLoader remoteReference;

    @Override
    public WorkRecordOuterResource load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String,WorkRecordOuterResource> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }

    @Override
    public List<Map<String, Object>> resourceVisitList(Long resourceId) {
        return remoteReference.resourceVisitList(resourceId);
    }
}
