package com.voxlearning.utopia.service.afenti.impl.listener.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.afenti.api.annotations.AfentiQueueMessageTypeIdentification;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiAchievementService;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 做题知识点记录并成就生成
 *
 * @author Ruib
 * @since 2016/8/1
 */
@Named
@Lazy(false)
@AfentiQueueMessageTypeIdentification(AfentiQueueMessageType.KP_COUNT_ACHIEVEMENT)
public class KpAchievementHandler extends AbstractAfentiQueueMessageHandler {

    @Inject AfentiAchievementService afentiAchievementService;

    @Override
    public void handle(ObjectMapper mapper, JsonNode root) throws Exception {

        try {
            JsonNode U = root.get("U");
            JsonNode S = root.get("S");
            if (U == null || S == null) {
                logger.error("KpAchievementHandler data null. {}", JsonUtils.toJson(root));
                return;
            }
            Subject subject = Subject.safeParse(S.asText());
            Long studentId = U.asLong();
            afentiAchievementService.learningStudyPointNotify(studentId, subject);
        } catch (Exception ignored) {
            logger.error("KpAchievementHandler parse date error. {}", JsonUtils.toJson(root));
            return;
        }


    }
}
