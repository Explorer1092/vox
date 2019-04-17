package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * Agent`s workRecord  service
 * Created by yaguang.wang on 2016/11/22.
 */
@Named
public class CrmWorkRecordService extends AbstractAdminService {
    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;

    public List<CrmWorkRecord> loadCrmWorkRecordBySchoolId(Long schoolId) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        return crmWorkRecordLoaderClient.findBySchool(schoolId);
    }
}
