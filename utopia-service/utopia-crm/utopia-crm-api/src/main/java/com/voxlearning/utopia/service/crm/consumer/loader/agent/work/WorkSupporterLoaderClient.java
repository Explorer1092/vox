package com.voxlearning.utopia.service.crm.consumer.loader.agent.work;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkSupporter;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkSupporterLoader;

import java.util.Collection;
import java.util.Map;

/**
 * WorkSupporterLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class WorkSupporterLoaderClient implements WorkSupporterLoader {

    @ImportService(interfaceClass = WorkSupporterLoader.class)
    private WorkSupporterLoader remoteReference;

    @Override
    public WorkSupporter load(String id){
        return remoteReference.load(id);
    }

    @Override
    public Map<String,WorkSupporter> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }
}
