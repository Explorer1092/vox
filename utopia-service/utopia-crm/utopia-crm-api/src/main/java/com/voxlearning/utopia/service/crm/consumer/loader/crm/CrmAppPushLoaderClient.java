package com.voxlearning.utopia.service.crm.consumer.loader.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmAppPushLoader;

/**
 * Created by yuechen.wang
 * on 2017/3/31
 */
public class CrmAppPushLoaderClient implements CrmAppPushLoader {

    @ImportService(interfaceClass = CrmAppPushLoader.class)
    private CrmAppPushLoader remoteReference;

    @Override
    public AppPushWfMessage findByRecord(Long recordId) {
        return remoteReference.findByRecord(recordId);
    }

    @Override
    public JPushTag generateTag(AppPushWfMessage appPushWfMessage) {
        return remoteReference.generateTag(appPushWfMessage);
    }

    @Override
    public AppPushWfMessage findById(String id) {
        return remoteReference.findById(id);
    }
}
