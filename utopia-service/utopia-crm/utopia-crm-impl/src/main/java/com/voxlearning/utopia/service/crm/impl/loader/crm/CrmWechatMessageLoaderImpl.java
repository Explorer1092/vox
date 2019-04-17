package com.voxlearning.utopia.service.crm.impl.loader.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmWechatMessageLoader;
import com.voxlearning.utopia.service.crm.impl.dao.crm.WechatWfMessagePersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yuechen.wang
 * on 2017/4/10.
 */

@Named
@Service(interfaceClass = CrmWechatMessageLoader.class)
@ExposeService(interfaceClass = CrmWechatMessageLoader.class)
public class CrmWechatMessageLoaderImpl implements CrmWechatMessageLoader {

    @Inject private WechatWfMessagePersistence wechatWfMessagePersistence;

    @Override
    public WechatWfMessage loadByRecordId(Long recordId) {
        return wechatWfMessagePersistence.loadByRecordId(recordId);
    }
}
