package com.voxlearning.utopia.service.crm.impl.loader.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmAppPushLoader;
import com.voxlearning.utopia.service.crm.impl.dao.crm.AppPushWfMessageDao;
import com.voxlearning.utopia.service.crm.impl.support.apppush.AppPushPublisherFactory;
import com.voxlearning.utopia.service.crm.impl.support.apppush.publisher.AppPushPublisher;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yuechen.wang
 * on 2017/3/31.
 */

@Named
@Service(interfaceClass = CrmAppPushLoader.class)
@ExposeService(interfaceClass = CrmAppPushLoader.class)
public class CrmAppPushLoaderImpl implements CrmAppPushLoader {

    @Inject private AppPushWfMessageDao appPushWfMessageDao;
    @Inject private AppPushPublisherFactory appPushPublisherFactory;

    @Override
    public AppPushWfMessage findByRecord(Long recordId) {
        return appPushWfMessageDao.findByRecordId(recordId);
    }

    @Override
    public JPushTag generateTag(AppPushWfMessage appPushWfMessage) {
        if (appPushWfMessage == null || appPushWfMessage.getUserType() == null) {
            return null;
        }

        AppPushPublisher publisher = appPushPublisherFactory.getPublisher(appPushWfMessage.getUserType());
        if (publisher == null) {
            return null;
        }
        return publisher.jpushTag(appPushWfMessage);
    }

    @Override
    public AppPushWfMessage findById(String id) {
        return appPushWfMessageDao.load(id);
    }

}
