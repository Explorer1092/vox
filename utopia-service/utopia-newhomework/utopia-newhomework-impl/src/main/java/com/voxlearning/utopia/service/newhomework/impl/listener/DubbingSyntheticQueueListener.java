package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.newhomework.impl.dao.DubbingSyntheticHistoryDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.newhomework.dubbing.synthetic.response.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.newhomework.dubbing.synthetic.response.queue")
        },
        maxPermits = 256
)
public class DubbingSyntheticQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private DubbingSyntheticHistoryDao dubbingSyntheticHistoryDao;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = null;
        Object object = message.decodeBody();
        if (object instanceof String) {
            msgMap = JsonUtils.fromJson((String) object);
        }
        if (msgMap == null) {
            return;
        }

        String id = SafeConverter.toString(msgMap.get("id"));
        String videoUrl = SafeConverter.toString(msgMap.get("videoUrl"));
        dubbingSyntheticHistoryDao.updateSyntheticState(id, true);
    }

}
