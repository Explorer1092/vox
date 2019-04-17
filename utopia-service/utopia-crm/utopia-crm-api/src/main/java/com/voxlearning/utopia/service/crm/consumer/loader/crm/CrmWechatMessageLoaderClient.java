package com.voxlearning.utopia.service.crm.consumer.loader.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmWechatMessageLoader;

/**
 * Created by yuechen.wang
 * on 2017/4/10
 */
public class CrmWechatMessageLoaderClient implements CrmWechatMessageLoader {

    @ImportService(interfaceClass = CrmWechatMessageLoader.class)
    private CrmWechatMessageLoader remoteReference;


    @Override
    public WechatWfMessage loadByRecordId(Long recordId) {
        if (recordId == null || recordId == 0L) {
            return null;
        }
        return remoteReference.loadByRecordId(recordId);
    }
}
