package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordSchoolLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordSchoolDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * WorkRecordSchoolLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordSchoolLoader.class)
@ExposeService(interfaceClass = WorkRecordSchoolLoader.class)
public class WorkRecordSchoolLoaderImpl extends SpringContainerSupport implements WorkRecordSchoolLoader {

    @Inject
    WorkRecordSchoolDao workRecordSchoolDao;

    @Override
    public WorkRecordSchool load(String id){
        return workRecordSchoolDao.load(id);
    }

    @Override
    public Map<String,WorkRecordSchool> loads(Collection<String> ids){
        return workRecordSchoolDao.loads(ids);
    }

    @Override
    public List<WorkRecordSchool> findByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(userIds) || startDate == null || endDate == null){
            return Collections.emptyList();
        }
        return workRecordSchoolDao.findByWorkersAndTime(userIds, startDate, endDate);
    }

    @Override
    public List<WorkRecordSchool> findBySchoolId(Long schoolId){
        if(schoolId == null){
            return Collections.emptyList();
        }
        return workRecordSchoolDao.loadBySchoolId(schoolId);
    }
}
