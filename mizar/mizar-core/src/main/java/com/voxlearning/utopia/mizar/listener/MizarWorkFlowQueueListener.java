package com.voxlearning.utopia.mizar.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.mizar.listener.handler.MizarAlbumNewsCheckHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

/**
 * @author shiwei.liao
 * @since 2016-12-27
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.workflow.mizar.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.workflow.mizar.queue"
                )
        }
)
public class MizarWorkFlowQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private MizarAlbumNewsCheckHandler mizarAlbumNewsCheckHandler;

    @Override
    public void onMessage(Message message) {
        Object decoded = message.decodeBody();
        if (decoded instanceof Map) {
            Map<String, String> map = (Map<String, String>) decoded;
            String configName = map.get("configName");
            String mqmsg = map.get("mqmsg");
            String status = map.get("status");
            Long recordId = Long.valueOf(map.get("recordId"));
            if (Objects.equals(configName, "mizar_admin_album_news_check")) {
                mizarAlbumNewsCheckHandler.handle(mqmsg, recordId, status);
            }
        }
    }
}
