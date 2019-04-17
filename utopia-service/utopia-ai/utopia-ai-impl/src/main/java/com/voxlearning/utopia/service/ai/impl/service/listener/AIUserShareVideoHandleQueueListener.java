package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsShareVideoRankCacheManager;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserVideoDao;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.share.video.count.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.share.video.count.queue")
        },
        maxPermits = 64
)
public class AIUserShareVideoHandleQueueListener extends AbstractAiSupport implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private ChipsShareVideoRankCacheManager chipsShareVideoRankCacheManager;

    @Inject
    private AIUserVideoDao aiUserVideoDao;
    @Inject
    private ChipsUserService chipsUserService;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("video handle share queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("AIUserShareVideoHandleQueueListener error. message:{}", body);
                return;
            }

            String id = SafeConverter.toString(param.get("ID"));
            if (StringUtils.isNotBlank(id)) {
                AIUserVideo aiUserVideo = aiUserVideoDao.load(id);
                if (aiUserVideo != null && aiUserVideo.getCategory() != AIUserVideo.Category.Bad) {
                    ChipsEnglishClass chipsEnglishClass = chipsUserService.loadClazzByUserAndBook(aiUserVideo.getUserId(), aiUserVideo.getBookId());
                    if (chipsEnglishClass == null) {
                        return;
                    }
                    chipsShareVideoRankCacheManager.updateRank(aiUserVideo.getUserId(), aiUserVideo.getUnitId(), chipsEnglishClass.getId().toString(), 1L);
                }
                //更新分享状态
                aiUserVideoDao.updateForShare(id);
            }
        }
    }
}
