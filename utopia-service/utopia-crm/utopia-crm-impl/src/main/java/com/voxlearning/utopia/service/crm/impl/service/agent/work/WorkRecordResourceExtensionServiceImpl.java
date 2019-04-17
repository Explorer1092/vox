package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordResourceExtensionService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordResourceExtensionDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkRecordResourceExtensionServiceImpl
 *
 * @author deliang.che
 * @since  2019/1/19
 */
@Named
@Service(interfaceClass = WorkRecordResourceExtensionService.class)
@ExposeService(interfaceClass = WorkRecordResourceExtensionService.class)
public class WorkRecordResourceExtensionServiceImpl extends SpringContainerSupport implements WorkRecordResourceExtensionService {
    @Inject
    WorkRecordResourceExtensionDao workRecordResourceExtensionDao;

    @Override
    public String insert(WorkRecordResourceExtension workRecordOuterResource){
        workRecordResourceExtensionDao.insert(workRecordOuterResource);
        return workRecordOuterResource.getId();
    }

    @Override
    public List<String> inserts(Collection<WorkRecordResourceExtension> workRecordResourceExtensionList){
        workRecordResourceExtensionDao.inserts(workRecordResourceExtensionList);
        return workRecordResourceExtensionList.stream().map(WorkRecordResourceExtension::getId).collect(Collectors.toList());
    }
}
