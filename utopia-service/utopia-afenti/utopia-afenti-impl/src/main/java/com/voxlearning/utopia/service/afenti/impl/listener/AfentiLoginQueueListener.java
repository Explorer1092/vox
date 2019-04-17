package com.voxlearning.utopia.service.afenti.impl.listener;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiAchievementService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * Created by Summer on 2017/7/25.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.afenti.login.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.afenti.login.queue")
        },
        maxPermits = 64
)
public class AfentiLoginQueueListener implements MessageListener {
    @Inject AfentiAchievementService afentiAchievementService;

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                return;
            }
            Date date = new Date(SafeConverter.toLong(param.get("TS")));
            Long studentId = SafeConverter.toLong(param.get("U"));
            Subject subject = Subject.safeParse(SafeConverter.toString(param.get("S")));
            afentiAchievementService.loginNotify(studentId, subject, date);
        }
    }
}
