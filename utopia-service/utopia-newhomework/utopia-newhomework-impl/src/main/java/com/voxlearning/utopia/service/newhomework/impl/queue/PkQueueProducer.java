/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.api.constant.ExperienceType;
import com.voxlearning.utopia.api.constant.VitalityType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

@Named("com.voxlearning.utopia.service.newhomework.impl.queue.PkQueueProducer")
public class PkQueueProducer {

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
