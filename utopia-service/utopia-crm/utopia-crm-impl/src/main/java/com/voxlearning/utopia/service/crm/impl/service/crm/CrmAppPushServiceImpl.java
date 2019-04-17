package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmAppPushService;
import com.voxlearning.utopia.service.crm.impl.dao.crm.AppPushWfMessageDao;
import com.voxlearning.utopia.service.crm.impl.support.apppush.AppPushPublisherFactory;
import com.voxlearning.utopia.service.crm.impl.support.apppush.AppPushWorkflowContext;
import com.voxlearning.utopia.service.crm.impl.support.apppush.publisher.AppPushPublisher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by yuechen.wang
 * on 2017/3/31.
 */
@Named
@Service(interfaceClass = CrmAppPushService.class)
@ExposeServices({
        @ExposeService(interfaceClass = CrmAppPushService.class, version = @ServiceVersion(version = "2017.03.31")),
        @ExposeService(interfaceClass = CrmAppPushService.class, version = @ServiceVersion(version = "2018.09.03"))
})
public class CrmAppPushServiceImpl extends SpringContainerSupport implements CrmAppPushService {

    @Inject private AppPushPublisherFactory appPushPublisherFactory;
    @Inject private AppPushWfMessageDao appPushWfMessageDao;

    @Override
    public MapMessage insert(AppPushWfMessage appPushWfMessage) {
        try {
            appPushWfMessageDao.insert(appPushWfMessage);
            MapMessage message = MapMessage.successMessage();
            message.add("id", appPushWfMessage.getId());
            return message;
        } catch (Exception ex) {
            logger.error("Failed insert AppPushWorkFlowMessage", ex);
            return MapMessage.errorMessage("保存记录失败");
        }
    }

    @Override
    public MapMessage publish(AppPushWfMessage appPushWfMessage) {
        if (appPushWfMessage == null) {
            return MapMessage.errorMessage("批量发送AppPush消息失败 admin_send_app_push:{} because WechatWfMessage is null");
        }
        AppPushWorkflowContext context = new AppPushWorkflowContext(appPushWfMessage);
        // 处理业务逻辑
        AppPushPublisher publisher = appPushPublisherFactory.getPublisher(appPushWfMessage.getUserType());
        if (publisher == null) {
            return MapMessage.errorMessage("无法处理消息类型：" + appPushWfMessage.getUserType());
        }
        return publisher.publish(context);

    }

    @Override
    public MapMessage publish(AppPushWfMessage appPushWfMessage, List<Long> userIds) {
        if (appPushWfMessage == null) {
            return MapMessage.errorMessage("批量发送AppPush消息失败 admin_send_app_push:{} because WechatWfMessage is null");
        }
        AppPushWorkflowContext context = new AppPushWorkflowContext(appPushWfMessage);
        context.setTargetUserIds(userIds);
        // 处理业务逻辑
        AppPushPublisher publisher = appPushPublisherFactory.getPublisher(appPushWfMessage.getUserType());
        if (publisher == null) {
            return MapMessage.errorMessage("无法处理消息类型：" + appPushWfMessage.getUserType());
        }

        MapMessage publishResult = publisher.publish(context);

        //logger.info("publish result is:{}", JsonUtils.toJson(publishResult));
        return publishResult;
    }

    @Override
    public int updateAppPushWfMessageStatus(Long recordId, String status) {
        return appPushWfMessageDao.updateStatus(recordId, status);
    }

    @Override
    public void updateSendStatus(String id) {
        AppPushWfMessage load = appPushWfMessageDao.load(id);
        if (load != null) {
            load.setSendStatus("success");
            load.setStatus("processed");
            appPushWfMessageDao.upsert(load);
        }
    }

    @Override
    public void updateIsTopStatus(String id, Boolean IsTopStatus, String topEndTime) {
        AppPushWfMessage load = appPushWfMessageDao.load(id);
        if (load != null) {
            load.setIsTop(IsTopStatus);
            load.setTopEndTimeStr(topEndTime);
            appPushWfMessageDao.upsert(load);
        }
    }

}
