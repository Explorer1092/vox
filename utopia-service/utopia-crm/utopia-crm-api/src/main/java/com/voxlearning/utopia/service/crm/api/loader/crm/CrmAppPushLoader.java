package com.voxlearning.utopia.service.crm.api.loader.crm;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;

import java.util.concurrent.TimeUnit;

/**
 * Created by yuechen.wang
 * on 2017/3/31
 */
@ServiceVersion(version = "2017.03.31")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
public interface CrmAppPushLoader extends IPingable {

    AppPushWfMessage findByRecord(Long recordId);

    JPushTag generateTag(AppPushWfMessage appPushWfMessage);

    AppPushWfMessage findById(String id);

}
