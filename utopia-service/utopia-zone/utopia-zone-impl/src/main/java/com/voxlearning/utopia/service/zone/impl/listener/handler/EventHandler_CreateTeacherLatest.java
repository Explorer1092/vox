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

package com.voxlearning.utopia.service.zone.impl.listener.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkCacheServiceClient;
import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_CheckHomeworkSM;
import com.voxlearning.utopia.service.user.api.mappers.UserIni;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.service.ZoneStudyMasterServiceImpl;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Create teacher latest
 * Created by alex on 2017/3/2.
 */
@Named
public class EventHandler_CreateTeacherLatest extends SpringContainerSupport implements ZoneEventHandler {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private NewHomeworkCacheServiceClient newHomeworkCacheServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private ZoneStudyMasterServiceImpl zoneStudyMasterService;

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.CREATE_TEACHER_LATEST;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        JsonNode userNode = root.get("userId");
        JsonNode clazzNode = root.get("clazzId");
        JsonNode mastersNode = root.get("masters");
        JsonNode homeworkTypeNode = root.get("homeworkType");

        if (userNode == null || clazzNode == null || mastersNode == null || homeworkTypeNode == null) {
            logger.warn("Error CREATE_TEACHER_LATEST message received. {}", JsonUtils.toJson(root));
            return;
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(userNode.asLong());
        if (teacher == null) {
            logger.warn("Error CREATE_TEACHER_LATEST message received. {}", JsonUtils.toJson(root));
            return;
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzNode.asLong());
        if (clazz == null) {
            logger.warn("Error CREATE_TEACHER_LATEST message received. {}", JsonUtils.toJson(root));
            return;
        }

        List<UserIni> masters = new ArrayList<>();
        if (mastersNode.isArray()) {
            ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
            for (JsonNode masterNode : mastersNode) {
                UserIni userIni = mapper.readValue(masterNode.traverse(), UserIni.class);
                masters.add(userIni);
            }
        }
        if (CollectionUtils.isEmpty(masters)) {
            logger.warn("Error CREATE_TEACHER_LATEST message received. {}", JsonUtils.toJson(root));
            return;
        }

        HomeworkType homeworkType = HomeworkType.parse(homeworkTypeNode.asText());
        if (homeworkType == null) {
            logger.warn("Error CREATE_TEACHER_LATEST message received. {}", JsonUtils.toJson(root));
            return;
        }

        sendTeacherLatest(teacher, clazz, masters, homeworkType);

    }

    private void sendTeacherLatest(Teacher teacher, Clazz clazz, List<UserIni> masters, HomeworkType type) {
        if (type == null) return;

        final Latest_CheckHomeworkSM detail = new Latest_CheckHomeworkSM();
        detail.setUserId(teacher.getId());
        detail.setUserName(teacher.fetchRealname());
        detail.setUserImg(teacher.fetchImageUrl());
        detail.setClazzId(clazz.getId());
        detail.setClazzName(clazz.formalizeClazzName());
        if (CollectionUtils.isNotEmpty(masters)) {
            detail.setMasters(masters);
        }

        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazz.getId());
        Map<Long, User> studentMap = userLoaderClient.loadUsers(studentIds);
        if (!studentMap.isEmpty()) {
            Map<Long, Integer> id_sm_count_map = zoneStudyMasterService.getMonthStudyMasterCount(studentMap.keySet(), type);
            Map<Long, Integer> id_fhw_count_map = newHomeworkCacheServiceClient.getNewHomeworkCacheService().monthFinishHomeworkCountManager_currentCount(studentMap.keySet(), type);
            List<StudentDataB> datas = new LinkedList<>();
            for (User student : studentMap.values()) {
                StudentDataB sd = new StudentDataB();
                sd.setStudentId(student.getId());
                sd.setStudentName(student.fetchRealname());
                sd.setSmCount(id_sm_count_map.getOrDefault(student.getId(), 0));
                sd.setFhwCount(id_fhw_count_map.getOrDefault(student.getId(), 0));
                datas.add(sd);
            }
            Collections.sort(datas, (o1, o2) -> Integer.compare(o2.getSmCount(), o1.getSmCount()));
            List<KeyValuePair<String, Integer>> smRank = new LinkedList<>();
            for (int i = 0, j = 0; (i < datas.size() && j < 3); i++, j++) {
                StudentDataB sd = datas.get(i);
                KeyValuePair<String, Integer> kvp = new KeyValuePair<>();
                kvp.setKey(sd.getStudentName());
                kvp.setValue(sd.getSmCount());
                smRank.add(kvp);
            }
            detail.setSmRank(smRank);

            Collections.sort(datas, (o1, o2) -> Integer.compare(o2.getFhwCount(), o1.getFhwCount()));
            List<KeyValuePair<String, Integer>> fhwcRank = new LinkedList<>();
            for (int i = 0, j = 0; (i < datas.size() && j < 3); i++, j++) {
                StudentDataB sd = datas.get(i);
                KeyValuePair<String, Integer> kvp = new KeyValuePair<>();
                kvp.setKey(sd.getStudentName());
                kvp.setValue(sd.getFhwCount());
                fhwcRank.add(kvp);
            }
            detail.setFhwcRank(fhwcRank);
        }
        userServiceClient.createTeacherLatest(teacher.getId(), LatestType.CHECK_HOMEWORK_STUDY_MASTER)
                .withDetail(detail).send();
    }

    @Data
    private static class StudentDataB {
        Long studentId;
        String studentName;
        Integer smCount;
        Integer fhwCount;
    }
}
