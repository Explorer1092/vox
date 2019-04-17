package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkSupporter;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkSupporterService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkSupporterDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkSupporterServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkSupporterService.class)
@ExposeService(interfaceClass = WorkSupporterService.class)
public class WorkSupporterServiceImpl extends SpringContainerSupport implements WorkSupporterService {
    @Inject
    WorkSupporterDao workSupporterDao;

    @Override
    public String insert(WorkSupporter workSupporter){
        workSupporterDao.insert(workSupporter);
        return workSupporter.getId();
    }

    @Override
    public List<String> inserts(Collection<WorkSupporter> workSupporterList){
        workSupporterDao.inserts(workSupporterList);
        return workSupporterList.stream().map(WorkSupporter::getId).collect(Collectors.toList());
    }
}
