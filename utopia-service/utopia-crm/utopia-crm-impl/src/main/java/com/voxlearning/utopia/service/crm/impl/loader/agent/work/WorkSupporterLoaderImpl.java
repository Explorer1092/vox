package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkSupporter;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkSupporterLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkSupporterDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 * WorkSupporterLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkSupporterLoader.class)
@ExposeService(interfaceClass = WorkSupporterLoader.class)
public class WorkSupporterLoaderImpl extends SpringContainerSupport implements WorkSupporterLoader {

    @Inject
    WorkSupporterDao workSupporterDao;

    @Override
    public WorkSupporter load(String id){
        return workSupporterDao.load(id);
    }

    @Override
    public Map<String,WorkSupporter> loads(Collection<String> ids){
        return workSupporterDao.loads(ids);
    }

}
