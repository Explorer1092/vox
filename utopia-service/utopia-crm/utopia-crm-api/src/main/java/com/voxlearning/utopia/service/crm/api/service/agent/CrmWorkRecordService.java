package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;

import java.util.concurrent.TimeUnit;

/**
 * CrmWorkRecordService
 *
 * @author song.wang
 * @date 2017/12/4
 */
@ServiceVersion(version = "2017.12.04")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface CrmWorkRecordService extends IPingable {

    String insert(CrmWorkRecord crmWorkRecord);

    CrmWorkRecord update(String id, CrmWorkRecord crmWorkRecord);

}
