package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordOuterResourceService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordOuterResourceDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkRecordOuterResourceServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordOuterResourceService.class)
@ExposeService(interfaceClass = WorkRecordOuterResourceService.class)
public class WorkRecordOuterResourceServiceImpl extends SpringContainerSupport implements WorkRecordOuterResourceService {
    @Inject
    WorkRecordOuterResourceDao workRecordOuterResourceDao;

    @Override
    public String insert(WorkRecordOuterResource workRecordOuterResource){
        workRecordOuterResourceDao.insert(workRecordOuterResource);
        return workRecordOuterResource.getId();
    }

    @Override
    public List<String> inserts(Collection<WorkRecordOuterResource> workRecordOuterResourceList){
        workRecordOuterResourceDao.inserts(workRecordOuterResourceList);
        return workRecordOuterResourceList.stream().map(WorkRecordOuterResource::getId).collect(Collectors.toList());
    }

    @Override
    public void updateWorkRecordIdAndType(Collection<String> ids, String workRecordId, AgentWorkRecordType workRecordType){
        workRecordOuterResourceDao.updateWorkRecordIdAndType(ids,workRecordId,workRecordType);
    }
}
