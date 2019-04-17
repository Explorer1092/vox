package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordTeacherLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordTeacherDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * WorkRecordTeacherLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordTeacherLoader.class)
@ExposeService(interfaceClass = WorkRecordTeacherLoader.class)
public class WorkRecordTeacherLoaderImpl extends SpringContainerSupport implements WorkRecordTeacherLoader {

    @Inject
    WorkRecordTeacherDao workRecordTeacherDao;

    @Override
    public WorkRecordTeacher load(String id){
        return workRecordTeacherDao.load(id);
    }

    @Override
    public Map<String,WorkRecordTeacher> loads(Collection<String> ids){
        return workRecordTeacherDao.loads(ids);
    }

    @Override
    public List<WorkRecordTeacher> loadByTeacherId(Long teacherId) {
        return workRecordTeacherDao.getWorkRecordTeacherByTeacher(teacherId);
    }
}
