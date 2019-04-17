package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.impl.service.ChipsUserVideoServiceImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author guangqing
 * @since 2019/3/21
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.active.remark.video.message.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.active.remark.video.message.queue")
        },
        maxPermits = 4
)
public class ChipsVideoRemarkListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsUserVideoServiceImpl chipsUserVideoService;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("video remark handle share queue no message");
            return;
        }
        Object body = message.decodeBody();
        if (body == null || !(body instanceof String)) {
            return;
        }
        String json = (String) body;
        Map<String, Object> param = JsonUtils.fromJson(json);
        if (param == null) {
            logger.error("ChipsVideoRemarkListener error. message:{}", body);
            return;
        }
        long userId = SafeConverter.toLong(param.get("userId"));
        String unitId = SafeConverter.toString(param.get("unitId"));
        if (Long.compare(userId, 0L) < 0 || StringUtils.isBlank(unitId)) {
            return;
        }
        chipsUserVideoService.filterVideo(userId, unitId);
    }
}
