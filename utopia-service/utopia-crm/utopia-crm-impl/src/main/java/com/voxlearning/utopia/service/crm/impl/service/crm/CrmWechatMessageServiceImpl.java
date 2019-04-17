package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmWechatMessageService;
import com.voxlearning.utopia.service.crm.impl.dao.crm.WechatWfMessagePersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yuechen.wang
 * on 2017/4/10.
 */
@Named
@Service(interfaceClass = CrmWechatMessageService.class)
@ExposeService(interfaceClass = CrmWechatMessageService.class)
public class CrmWechatMessageServiceImpl extends SpringContainerSupport implements CrmWechatMessageService {

    @Inject private WechatWfMessagePersistence wechatWfMessagePersistence;

    @Override
    public MapMessage persist(WechatWfMessage wechatWfMessage) {
        try {
            wechatWfMessagePersistence.insert(wechatWfMessage);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed insert WechatWorkFlowMessage", ex);
            return MapMessage.errorMessage("保存记录失败");
        }
    }

    @Override
    public int updateMessageStatusByRecord(Long recordId, String status) {
        if (recordId == null || StringUtils.isBlank(status)) {
            return 0;
        }
        return wechatWfMessagePersistence.updateWechatWfMessageStatusByRecordId(recordId, status);
    }

}
