package com.voxlearning.wechat.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.campaign.client.WechatUserCampaignServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.service.integral.ClazzIntegralService;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.IMessageHandler;
import com.voxlearning.wechat.handler.event.ScanEventHandler_Teacher;
import com.voxlearning.wechat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * @author Xin Xin
 * @since 10/16/15
 */
@Named
@Slf4j
public class MessageProcessor implements InitializingBean, ApplicationContextAware {
    private final Map<String, IMessageHandler> handlers = new HashMap<>();
    private ApplicationContext applicationContext;
    @Inject
    private MessageService messageService;

    @Inject
    private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject
    private WechatUserCampaignServiceClient wechatUserCampaignServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;
    @ImportService(interfaceClass = ClazzIntegralService.class)
    private ClazzIntegralService clazzIntegralService;

    @Override
    public void afterPropertiesSet() throws Exception {
        ServiceLoader<IMessageHandler> loader = ServiceLoader.load(IMessageHandler.class);
        Map<String, Object> map = MiscUtils.m(UserIntegralService.class.getName(), userIntegralService,
                ClazzIntegralService.class.getName(), clazzIntegralService);
        for (IMessageHandler handler : loader) {
            if (handler instanceof ScanEventHandler_Teacher) {
                ((ScanEventHandler_Teacher) handler).setClazzIntegralServiceClient(clazzIntegralServiceClient);
                ((ScanEventHandler_Teacher) handler).setWechatUserCampaignServiceClient(wechatUserCampaignServiceClient);
            }
            handler.setApplicationContext(applicationContext); //注入进去applicationContext
            handler.setExtInstances(map);
            handlers.put(handler.getFingerprint(), handler);
        }
    }

    public String process(MessageContext context, WechatType type) {
        Objects.requireNonNull(context, "context must not be null.");

        IMessageHandler handler = handlers.get(context.getFingerprint(type));
        if (null == handler) {
            throw new IllegalStateException("unknown message type. msg:" + JsonUtils.toJson(context));
        }

        beforeProcess(context);
        String reply = handler.handle(context);
        afterProcess(context);

        return reply;
    }

    private void beforeProcess(MessageContext context) {
        //刷新用户最后交互时间
        //包括发送信息、点击自定义菜单、订阅事件、扫描二维码事件、支付成功事件、用户维权
        if (context.getMsgType().getType().equals(MessageType.EVENT.getType()) && context.getEvent().equals(EventType.LOCATION.getType())) {
            return;
        }
        messageService.updateActiveTime(context.getFromUserName(), context.getCreateTime());
    }

    private void afterProcess(MessageContext context) {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
