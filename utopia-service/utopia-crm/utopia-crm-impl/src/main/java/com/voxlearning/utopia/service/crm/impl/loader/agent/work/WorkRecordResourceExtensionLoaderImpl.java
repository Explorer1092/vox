package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordResourceExtensionLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordResourceExtensionDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * WorkRecordResourceExtensionLoaderImpl
 *
 * @author deliang.che
 * @since  2019/1/18
 */
@Named
@Service(interfaceClass = WorkRecordResourceExtensionLoader.class)
@ExposeService(interfaceClass = WorkRecordResourceExtensionLoader.class)
public class WorkRecordResourceExtensionLoaderImpl extends SpringContainerSupport implements WorkRecordResourceExtensionLoader {

    @Inject
    WorkRecordResourceExtensionDao workRecordResourceExtensionDao;

    @Override
    public WorkRecordResourceExtension load(String id){
        return workRecordResourceExtensionDao.load(id);
    }

    @Override
    public Map<String,WorkRecordResourceExtension> loads(Collection<String> ids){
        return workRecordResourceExtensionDao.loads(ids);
    }

    @Override
    public List<WorkRecordResourceExtension> loadByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(userIds) || startDate == null || endDate == null){
            return Collections.emptyList();
        }
        return workRecordResourceExtensionDao.loadByWorkersAndTime(userIds, startDate, endDate);
    }

}
