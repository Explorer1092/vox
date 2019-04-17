package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordAccompany;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordAccompanyService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordAccompanyDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * WorkRecordAccompanyServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordAccompanyService.class)
@ExposeService(interfaceClass = WorkRecordAccompanyService.class)
public class WorkRecordAccompanyServiceImpl extends SpringContainerSupport implements WorkRecordAccompanyService {
    @Inject
    WorkRecordAccompanyDao workRecordAccompanyDao;

    @Override
    public String insert(WorkRecordAccompany workRecordAccompany){
        workRecordAccompanyDao.insert(workRecordAccompany);
        return workRecordAccompany.getId();
    }
}
