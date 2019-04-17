package com.voxlearning.utopia.service.crm.consumer.service.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmWechatMessageService;

/**
 * Created by yuechen.wang
 * on 2017/04/10.
 */
public class CrmWechatMessageServiceClient implements CrmWechatMessageService {

    @ImportService(interfaceClass = CrmWechatMessageService.class)
    private CrmWechatMessageService remoteReference;

    @Override
    public MapMessage persist(WechatWfMessage wechatWfMessage) {
        if (wechatWfMessage == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        return remoteReference.persist(wechatWfMessage);
    }

    @Override
    public int updateMessageStatusByRecord(Long recordId, String status) {
        if (recordId == null || StringUtils.isBlank(status)) {
            return 0;
        }
        return remoteReference.updateMessageStatusByRecord(recordId, status);
    }

}
