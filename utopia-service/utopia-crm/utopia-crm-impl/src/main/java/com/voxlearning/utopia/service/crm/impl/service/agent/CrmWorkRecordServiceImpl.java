package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.CrmWorkRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.CrmWorkRecordDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * CrmWorkRecordServiceImpl
 *
 * @author song.wang
 * @date 2017/12/4
 */
@Named
@Service(interfaceClass = CrmWorkRecordService.class)
@ExposeService(interfaceClass = CrmWorkRecordService.class)
public class CrmWorkRecordServiceImpl extends SpringContainerSupport implements CrmWorkRecordService {

    @Inject
    CrmWorkRecordDao crmWorkRecordDao;

    @Override
    public String insert(CrmWorkRecord crmWorkRecord) {
        return crmWorkRecordDao.insert(crmWorkRecord);
    }

    @Override
    public CrmWorkRecord update(String id, CrmWorkRecord crmWorkRecord) {
        return crmWorkRecordDao.update(id,crmWorkRecord);
    }
}
