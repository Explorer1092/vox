package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmProductFeedbackRecord;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmProductFeedbackRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.crm.CrmProductFeedbackRecordDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
@Named
@Service(interfaceClass = CrmProductFeedbackRecordService.class)
@ExposeService(interfaceClass = CrmProductFeedbackRecordService.class)
public class CrmProductFeedbackRecordServiceImpl implements CrmProductFeedbackRecordService {
    @Inject
    private CrmProductFeedbackRecordDao crmProductFeedbackRecordDao;

    @Override
    public String insertProductFeedbackRecord(CrmProductFeedbackRecord record) {
        crmProductFeedbackRecordDao.insert(record);
        return record.getId();
    }
}
