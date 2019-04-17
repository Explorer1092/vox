package com.voxlearning.utopia.service.crm.consumer.service.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmProductFeedbackRecord;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmProductFeedbackRecordService;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
public class CrmProductFeedbackRecordServiceClient implements CrmProductFeedbackRecordService {

    @ImportService(interfaceClass = CrmProductFeedbackRecordService.class)
    private CrmProductFeedbackRecordService remoteReference;


    @Override
    public String insertProductFeedbackRecord(CrmProductFeedbackRecord record) {
        return remoteReference.insertProductFeedbackRecord(record);
    }
}
