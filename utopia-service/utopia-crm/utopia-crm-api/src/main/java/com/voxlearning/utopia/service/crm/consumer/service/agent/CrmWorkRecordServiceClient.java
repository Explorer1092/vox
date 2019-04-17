package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.CrmWorkRecordService;

/**
 * CrmWorkRecordServiceClient
 *
 * @author song.wang
 * @date 2017/12/4
 */
public class CrmWorkRecordServiceClient implements CrmWorkRecordService {

    @ImportService(interfaceClass = CrmWorkRecordService.class)
    private CrmWorkRecordService remoteReference;

    @Override
    public String insert(CrmWorkRecord crmWorkRecord) {
        return remoteReference.insert(crmWorkRecord);
    }

    @Override
    public CrmWorkRecord update(String id, CrmWorkRecord crmWorkRecord) {
        return remoteReference.update(id, crmWorkRecord);
    }

}
