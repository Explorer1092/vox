package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordAccompany;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordAccompanyLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordAccompanyDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * WorkRecordAccompanyLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordAccompanyLoader.class)
@ExposeService(interfaceClass = WorkRecordAccompanyLoader.class)
public class WorkRecordAccompanyLoaderImpl extends SpringContainerSupport implements WorkRecordAccompanyLoader {

    @Inject
    WorkRecordAccompanyDao workRecordAccompanyDao;

    @Override
    public WorkRecordAccompany load(String id){
        return workRecordAccompanyDao.load(id);
    }

    @Override
    public List<WorkRecordAccompany> loadByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        if(CollectionUtils.isEmpty(userIds) || startDate == null || endDate == null){
            return Collections.emptyList();
        }
        return workRecordAccompanyDao.loadByWorkersAndTime(userIds, startDate, endDate);
    }

    @Override
    public List<WorkRecordAccompany> loadByBusinessRecordId(String businessRecordId){
        if(StringUtils.isBlank(businessRecordId)){
            return Collections.emptyList();
        }
        return workRecordAccompanyDao.loadByBusinessRecordId(businessRecordId);
    }
}
