package com.voxlearning.utopia.service.crm.consumer.service.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmAppPushService;

import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
public class CrmAppPushServiceClient implements CrmAppPushService {

    @ImportService(interfaceClass = CrmAppPushService.class)
    private CrmAppPushService remoteReference;

    @Override
    public MapMessage insert(AppPushWfMessage appPushWfMessage) {
        if (appPushWfMessage == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.insert(appPushWfMessage);
    }

    @Override
    public MapMessage publish(AppPushWfMessage appPushWfMessage) {
        if (appPushWfMessage == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.publish(appPushWfMessage);
    }

    @Override
    public MapMessage publish(AppPushWfMessage appPushWfMessage, List<Long> userIds) {
        if (appPushWfMessage == null || CollectionUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.publish(appPushWfMessage, userIds);
    }

    @Override
    public int updateAppPushWfMessageStatus(Long recordId, String status) {
        if (recordId == null || StringUtils.isBlank(status)) {
            return 0;
        }
        return remoteReference.updateAppPushWfMessageStatus(recordId, status);
    }

    public void updateSendStatus(String id) {
        remoteReference.updateSendStatus(id);
    }

    public void updateIsTopStatus(String id, Boolean IsTopStatus, String topEndTime) {
        remoteReference.updateIsTopStatus(id, IsTopStatus, topEndTime);
    }
}
