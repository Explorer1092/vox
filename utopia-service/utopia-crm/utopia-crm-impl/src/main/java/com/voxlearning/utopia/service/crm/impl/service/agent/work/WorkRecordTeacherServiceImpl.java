package com.voxlearning.utopia.service.crm.impl.service.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;
import com.voxlearning.utopia.service.crm.api.service.agent.work.WorkRecordTeacherService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordTeacherDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkRecordTeacherServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordTeacherService.class)
@ExposeService(interfaceClass = WorkRecordTeacherService.class)
public class WorkRecordTeacherServiceImpl extends SpringContainerSupport implements WorkRecordTeacherService {
    @Inject
    WorkRecordTeacherDao workRecordTeacherDao;

    @Override
    public String insert(WorkRecordTeacher workRecordTeacher){
        workRecordTeacherDao.insert(workRecordTeacher);
        return workRecordTeacher.getId();
    }

    @Override
    public List<String> inserts(Collection<WorkRecordTeacher> workRecordTeacherList){
        workRecordTeacherDao.inserts(workRecordTeacherList);
        return workRecordTeacherList.stream().map(WorkRecordTeacher::getId).collect(Collectors.toList());
    }

    @Override
    public WorkRecordTeacher update(WorkRecordTeacher workRecordTeacher) {
        return workRecordTeacherDao.upsert(workRecordTeacher);
    }
}
