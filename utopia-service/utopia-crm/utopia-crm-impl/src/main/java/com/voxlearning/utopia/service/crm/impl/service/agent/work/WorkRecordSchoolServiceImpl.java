package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordSchoolService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordSchoolDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * WorkRecordSchoolServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordSchoolService.class)
@ExposeService(interfaceClass = WorkRecordSchoolService.class)
public class WorkRecordSchoolServiceImpl extends SpringContainerSupport implements WorkRecordSchoolService {
    @Inject
    WorkRecordSchoolDao workRecordSchoolDao;

    @Override
    public String insert(WorkRecordSchool workRecordSchool){
        workRecordSchoolDao.insert(workRecordSchool);
        return workRecordSchool.getId();
    }
}
