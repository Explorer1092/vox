package com.voxlearning.utopia.service.zone.impl.listener.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.support.MonthStudyMasterCountCacheManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 2017/3/2.
 */
@Named
public class EventHandler_IncreateMonthStudyMasterCount  extends SpringContainerSupport implements ZoneEventHandler {

    @Inject
    private MonthStudyMasterCountCacheManager monthStudyMasterCountCacheManager;

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.INC_MONTH_STUDY_MASTER_COUNT;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        JsonNode userIdsNode = root.get("userIds");
        if (userIdsNode == null) {
            logger.warn("Empty INC_MONTH_STUDY_MASTER_COUNT message received. {}", JsonUtils.toJson(root));
            return;
        }

        JsonNode homeworkTypeNode = root.get("homeworkType");
        if (homeworkTypeNode == null) {
            logger.warn("No homework type INC_MONTH_STUDY_MASTER_COUNT message received. {}", JsonUtils.toJson(root));
            return;
        }

        HomeworkType homeworkType = HomeworkType.parse(homeworkTypeNode.asText());
        if (homeworkType == null) {
            logger.warn("Unknown homework type INC_MONTH_STUDY_MASTER_COUNT message received. {}", JsonUtils.toJson(root));
            return;
        }

        List<Long> userIds = new ArrayList<>();
        if (userIdsNode.isArray()) {
            for (JsonNode userIdNode : userIdsNode) {
                userIds.add(userIdNode.asLong());
            }
        }

        if (CollectionUtils.isNotEmpty(userIds)) {
            monthStudyMasterCountCacheManager.increase(userIds, homeworkType);
        }
    }
}
