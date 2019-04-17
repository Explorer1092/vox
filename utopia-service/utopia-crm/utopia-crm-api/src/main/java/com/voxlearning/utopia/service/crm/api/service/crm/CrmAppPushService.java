package com.voxlearning.utopia.service.crm.api.service.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuechen.wang
 * on 2017/3/31
 */
@ServiceVersion(version = "2018.09.03")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmAppPushService extends IPingable {

    /**
     * 保存实体
     */
    MapMessage insert(AppPushWfMessage appPushWfMessage);

    /**
     * 找到需要发送的消息列表
     */
    MapMessage publish(AppPushWfMessage appPushWfMessage);

    /**
     * 发送消息给指定的userIds
     */
    MapMessage publish(AppPushWfMessage appPushWfMessage, List<Long> userIds);

    int updateAppPushWfMessageStatus(Long recordId, String status);

    void updateSendStatus(String id);

    void updateIsTopStatus(String id, Boolean IsTopStatus, String topEndTime);
}
