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

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.queue.zone.ZoneEvent;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.UserIni;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Clazz Zone message queue
 * Created by alex on 2017/3/2.
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.queue.ClazzZoneQueueProducer")
public class ClazzZoneQueueProducer {
    @Getter
    @AlpsQueueProducer(queue = "utopia.queue.zone")
    private MessageProducer producer;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;

    // send PostAssignNewHomeworkClazzHeadline mesage to clazz zone
    public void sendPahwClazzHeadline(Teacher teacher, AssignHomeworkContext context) {
        LinkedHashMap<Long, NewHomework> newHomeworkLinkedHashMap = context.getAssignedGroupHomework();
        if (MapUtils.isEmpty(newHomeworkLinkedHashMap)) return;
        Set<Long> groupIds = newHomeworkLinkedHashMap.keySet();
        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(groupIds, false);

        for (Map.Entry<Long, NewHomework> entry : newHomeworkLinkedHashMap.entrySet()) {
            GroupMapper groupMapper = groupMapperMap.get(entry.getValue().getClazzGroupId());
            Map<String, Object> clazzJournal = new LinkedHashMap<>();
            clazzJournal.put("clazzId", groupMapper.getClazzId());
            clazzJournal.put("relevantUserId", teacher.getId());
            clazzJournal.put("relevantUserType", UserType.TEACHER);
            clazzJournal.put("journalType", ClazzJournalType.HOMEWORK_HEADLINE);
            clazzJournal.put("journalCategory", ClazzJournalCategory.APPLICATION_STD);
            clazzJournal.put("journalJson", JsonUtils.toJson(MiscUtils.m("subject", entry.getValue().getSubject().name(),
                    "createAt", entry.getValue().getCreateAt(),
                    "homeworkId", entry.getValue().getId())));

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("TS", System.currentTimeMillis());
            message.put("CJ", clazzJournal);
            message.put("T", ZoneEventType.CLAZZ_JOURNAL);

            Message msg = Message.newMessage().withStringBody(JsonUtils.toJson(message));
            producer.produce(msg);
        }
    }

    // send increase student study master count message to clazz zone
    public void sendIncStudyMasterCount(Long userId) {
        if (userId == null || userId <= 0) return;

        ZoneEvent event = new ZoneEvent();
        event.setType(ZoneEventType.IncreaseStudyMasterCountByOne);
        event.getAttributes().put("studentId", userId);

        producer.produce(event.toMessage());
    }

    // send increase month study master count message
    public void sendIncMonthStudyMasterCount(Collection<Long> userIds, HomeworkType type) {
        if (CollectionUtils.isEmpty(userIds) || type == null) return;

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("T", ZoneEventType.INC_MONTH_STUDY_MASTER_COUNT);
        message.put("userIds", userIds);
        message.put("homeworkType", type);
        Message msg = Message.newMessage().withStringBody(JsonUtils.toJson(message));
        producer.produce(msg);
    }

    public void sendCreateTeacherLatest(Teacher teacher, Clazz clazz, List<UserIni> masters, HomeworkType type) {
        if (teacher == null || clazz == null || CollectionUtils.isEmpty(masters) || type == null) return;

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("T", ZoneEventType.CREATE_TEACHER_LATEST);
        message.put("userId", teacher.getId());
        message.put("clazzId", clazz.getId());
        message.put("masters", masters);
        message.put("homeworkType", type.name());
        Message msg = Message.newMessage().withStringBody(JsonUtils.toJson(message));
        producer.produce(msg);
    }
}
