package com.voxlearning.utopia.service.parent.homework.impl.mq.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.parent.homework.api.mapper.UserPreference;
import com.voxlearning.utopia.service.parent.homework.impl.HomeworkUserPreferencesServiceImpl;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "platform.queue.parent.homework"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "platform.queue.parent.homework")
        }
)
public class HomeworkUserPreferencesListener implements MessageListener {
    @Inject
    private HomeworkUserPreferencesServiceImpl homeworkUserPreferencesService;
    @Override
    public void onMessage(Message message) {
        Map<String, Object> messageMap = JsonUtils.fromJson(message.getBodyAsString());
        if (messageMap != null &&Objects.equals(messageMap.get("messageType"), "assign")) {
            if (StringUtils.equalsAny(SafeConverter.toString(messageMap.get("bizType")),
                    ObjectiveConfigType.MENTAL_ARITHMETIC.name())) {
                long studentId = SafeConverter.toLong(messageMap.get("studentId"));
                String subject = SafeConverter.toString(messageMap.get("subject"));
                String unitId = SafeConverter.toString(messageMap.get("unitId"));
                UserPreference userPreference = new UserPreference();
                userPreference.setUserId(studentId);
                userPreference.setSubject(subject);
                userPreference.setBookId(SafeConverter.toString(messageMap.get("bookId")));
                // 设置教材
                homeworkUserPreferencesService.upsertUserPreferencesNoMessage(Collections.singletonList(userPreference));
                // 缓存单元 和 Exam 同步习题 一致
                HomeWorkCache.set(unitId, CacheKey.UNIT, studentId, subject);
            }
        }
    }
}
