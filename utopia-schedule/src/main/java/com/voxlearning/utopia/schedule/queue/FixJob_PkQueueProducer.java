package com.voxlearning.utopia.schedule.queue;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.api.constant.ExperienceType;
import com.voxlearning.utopia.api.constant.VitalityType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixJob_PkQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.pk.queue")
    private MessageProducer producer;

    public void addVitality(User user, int vitality, VitalityType vitalityType) {
        if (user == null || vitalityType == null) {
            return;
        }
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("event", "ADD_VITALITY");
        message.put("requestor", generateRequestor(user));
        message.put("vitality", vitality);
        message.put("vitalityType", vitalityType);
        message.put("unique", false);

        Message msg = Message.newMessage().withStringBody(JsonUtils.toJson(message));
        producer.produce(msg);
    }

    public void upgrade(User user, int experience, Integer experienceType) {
        if (user == null || experience < 0) {
            return;
        }
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("event", "ADD_EXPERIENCE");
        message.put("requestor", generateRequestor(user));
        message.put("experience", experience);
        message.put("experienceType", ExperienceType.of(experienceType));

        Message msg = Message.newMessage().withStringBody(JsonUtils.toJson(message));
        producer.produce(msg);
    }

    private Map<String, Object> generateRequestor(User user) {
        Map<String, Object> requestor = new LinkedHashMap<>();
        requestor.put("userId", user.getId());
        requestor.put("userType", user.fetchUserType().getType());
        if (user.getCreateTime() != null) {
            requestor.put("regTimestamp", user.getCreateTime().getTime());
        }
        if (user.getProfile() != null) {
            requestor.put("userName", StringUtils.defaultString(user.getProfile().getRealname()));
            requestor.put("userImageUrl", StringUtils.defaultString(user.getProfile().getImgUrl()));
        }
        return requestor;
    }

}
