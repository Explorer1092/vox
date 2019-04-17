package com.voxlearning.utopia.service.crm.api.service.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;

import java.util.concurrent.TimeUnit;

/**
 * Created by yuechen.wang
 * on 2017/4/10
 */
@ServiceVersion(version = "20170410")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmWechatMessageService extends IPingable {

    /**
     * 保存实体
     */
    MapMessage persist(WechatWfMessage wechatWfMessage);

    int updateMessageStatusByRecord(Long recordId, String status);
}
