package com.voxlearning.utopia.mizar.listener;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.mizar.listener.handler.UserSettlementHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * MizarCommandQueueListener
 *
 * @author song.wang
 * @date 2017/6/27
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.mizar.command.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.mizar.command.queue"
                )
        }
)
public class MizarCommandQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private UserSettlementHandler userSettlementHandler;

    @Override
    public void onMessage(Message message) {


        Map<String, Object> command = null;
        Object messageBody = message.decodeBody();
        if (messageBody instanceof Map) {
            command = (Map<String, Object>) messageBody;
        }

        if (command == null || !command.containsKey("command")) {
            return;
        }

        String commandName = String.valueOf(command.get("command"));

        // 首页排行榜
        if ("user_settlement".equals(commandName)) {
            Integer date = SafeConverter.toInt(command.get("date"));
            List<Long> schoolIds = StringUtils.toLongList((String)command.get("schoolIds"));
            AlpsThreadPool.getInstance().submit(() -> userSettlementHandler.handle(schoolIds, date));
        }
    }
}
