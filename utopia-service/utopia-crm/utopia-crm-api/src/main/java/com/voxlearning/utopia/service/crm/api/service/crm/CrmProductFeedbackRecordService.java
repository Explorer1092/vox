package com.voxlearning.utopia.service.crm.api.service.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmProductFeedbackRecord;

import java.util.concurrent.TimeUnit;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
@ServiceVersion(version = "2017.03.03")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmProductFeedbackRecordService extends IPingable {

    String insertProductFeedbackRecord(CrmProductFeedbackRecord record);
}
