package com.voxlearning.utopia.service.crm.api.loader.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
@ServiceVersion(version = "20170303")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface CrmProductFeedbackRecordLoader extends IPingable {
}
