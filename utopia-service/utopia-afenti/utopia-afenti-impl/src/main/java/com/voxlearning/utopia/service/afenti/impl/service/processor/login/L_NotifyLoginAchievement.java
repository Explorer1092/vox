package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登陆通知，用于计算用户登陆成就
 *
 * @author peng.zhang.a
 * @since 16-7-26
 */
@Named
public class L_NotifyLoginAchievement extends SpringContainerSupport implements IAfentiTask<LoginContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject private AfentiQueueProducer afentiQueueProducer;

    @Override
    public void execute(LoginContext context) {
        Long studentId = context.getStudent().getId();
        Subject subject = context.getSubject();

        if (asyncAfentiCacheService.AfentiLoginCacheManager_notified(studentId, subject).take()) return;

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("TS", System.currentTimeMillis());
        message.put("U", studentId);
        message.put("S", subject);
        afentiQueueProducer.getLoginProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
    }
}
