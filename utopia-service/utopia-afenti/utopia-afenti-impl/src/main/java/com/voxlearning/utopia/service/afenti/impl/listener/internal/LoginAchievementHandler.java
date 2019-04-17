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
import java.util.Date;

/**
 * 监听用户登陆，记录用户登陆时间并生成成就
 *
 * @author Ruib
 * @since 2016/8/1
 */
@Named
@Lazy(false)
@AfentiQueueMessageTypeIdentification(AfentiQueueMessageType.LOGIN_AFNETI_ACHIEVEMENT)
public class LoginAchievementHandler extends AbstractAfentiQueueMessageHandler {
    @Inject AfentiAchievementService afentiAchievementService;

    @Override
    public void handle(ObjectMapper mapper, JsonNode root) throws Exception {
        Date date = null;
        JsonNode TS = root.get("TS");
        if (TS != null) {
            try {
                date = new Date(TS.asLong());
            } catch (Exception ignored) {
            }
        }
        if (null == date) {
            logger.error("LoginAchievementHandler no login date specified. {}", JsonUtils.toJson(root));
        }

        Subject subject = null;
        JsonNode S = root.get("S");
        if (S != null) {
            subject = Subject.safeParse(S.asText());
        }
        if (null == subject) {
            logger.error("LoginAchievementHandler no subject specified. {}", JsonUtils.toJson(root));
        }

        Long studentId = null;
        JsonNode U = root.get("U");
        if (U != null) {
            try {
                studentId = U.asLong();
            } catch (Exception ignored) {
            }
        }
        if (null == studentId) {
            logger.error("LoginAchievementHandler no studentId specified. {}", JsonUtils.toJson(root));
        }
        if (studentId == null || subject == null || date == null) {
            return;
        }
        afentiAchievementService.loginNotify(studentId, subject, date);
    }
}
