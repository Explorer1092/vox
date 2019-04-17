package com.voxlearning.utopia.service.crm.api.loader.crm;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;

import java.util.concurrent.TimeUnit;

/**
 * Created by yuechen.wang
 * on 2017/4/10
 */
@ServiceVersion(version = "20170410")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
public interface CrmWechatMessageLoader extends IPingable {

    WechatWfMessage loadByRecordId(Long recordId);

}
