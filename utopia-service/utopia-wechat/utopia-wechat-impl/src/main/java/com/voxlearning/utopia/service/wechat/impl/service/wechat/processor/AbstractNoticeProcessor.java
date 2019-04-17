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

package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.FinishHomeworkMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.constants.XxtMessageType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.impl.dao.UserWechatRefPersistence;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeProcessor;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeProcessorManager;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xin
 * @since 14-5-21 下午5:53
 */
@Named
@NoArgsConstructor
public abstract class AbstractNoticeProcessor extends SpringContainerSupport implements WechatNoticeProcessor {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private WechatNoticeProcessorManager wechatNoticeProcessorManager;
    @Inject private UserWechatRefPersistence userWechatRefPersistence;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        wechatNoticeProcessorManager.register(this);
    }

    @Override
    public void process(Clazz clazz, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazz.getId());
        Map<Long, User> studentMap = userLoaderClient.loadUsers(studentIds);
        processWithSpecificUsers(new ArrayList<>(studentMap.values()), teacher, clazz.getId(), extensionInfo, wechatType);
    }

    @Override
    public void process(Long clazzId, Long groupId, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType) {
        List<User> students;
        if (groupId == null) {
            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazzId);
            students = new ArrayList<>(userLoaderClient.loadUsers(studentIds).values());
        } else {
            students = studentLoaderClient.loadGroupStudents(groupId);
        }
        processWithSpecificUsers(students, teacher, clazzId, extensionInfo, wechatType);
    }

    @Override
    public void processWithStudents(Collection<Long> studentIds, Map<String, Object> extensionInfo, WechatType wechatType) {
        if (CollectionUtils.isEmpty(studentIds) || wechatType == null) {
            return;
        }
        Map<Long, List<StudentParentRef>> studentParentRefMap = studentLoaderClient.loadStudentParentRefs(studentIds);

        //查出所有相关家长的微信绑定数据
        Set<Long> parentIds = new HashSet<>();
        if (MapUtils.isNotEmpty(studentParentRefMap)) {
            studentParentRefMap.values().forEach(lst -> {
                if (CollectionUtils.isNotEmpty(lst)) {
                    parentIds.addAll(lst.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet()));
                }
            });
        }
        Map<Long, List<UserWechatRef>> parentWechatBindMap = userWechatRefPersistence.findByUserIds(parentIds, wechatType.getType());

        for (List<StudentParentRef> parents : studentParentRefMap.values()) {
            if (CollectionUtils.isEmpty(parents)) {
                continue;
            }
            for (StudentParentRef parent : parents) {
                List<UserWechatRef> userWechatRefs = parentWechatBindMap.get(parent.getParentId());
                if (CollectionUtils.isNotEmpty(userWechatRefs)) {
                    for (UserWechatRef ref : userWechatRefs) {
                        processSingleUserOpenId(ref.getUserId(), ref.getOpenId(), extensionInfo);
                    }
                }
            }
        }
    }

    @Override
    public void processWithSpecificUsers(List<User> students, Teacher teacher, Long clazzId, Map<String, Object> extensionInfo, WechatType wechatType) {
        @SuppressWarnings("unchecked")
        List<FinishHomeworkMapper> finishHomeworkMapperList = (List<FinishHomeworkMapper>) extensionInfo.get("finishStudents");
        Set<Long> finishStudentIdSet = new HashSet<>();
        if (finishHomeworkMapperList != null) {
            for (FinishHomeworkMapper finishHomeworkMapper : finishHomeworkMapperList) {
                finishStudentIdSet.add(finishHomeworkMapper.getUserId());
            }
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        Set<Long> uids = students.stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<StudentParentRef>> studentParentRefMap = studentLoaderClient.loadStudentParentRefs(uids);

        //查出所有相关家长的微信绑定数据
        Set<Long> parentIds = new HashSet<>();
        if (MapUtils.isNotEmpty(studentParentRefMap)) {
            studentParentRefMap.values().forEach(lst -> {
                if (CollectionUtils.isNotEmpty(lst)) {
                    parentIds.addAll(lst.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet()));
                }
            });
        }
        Map<Long, List<UserWechatRef>> parentWechatBindMap = userWechatRefPersistence.findByUserIds(parentIds, wechatType.getType());

        for (User student : students) {
            extensionInfo.put("finished", finishStudentIdSet.contains(student.getId()));
            extensionInfo.put("studentId", student.getId());
            extensionInfo.put("teacherId", teacher.getId());
            extensionInfo.put("studentName", student.getProfile().getRealname());
            extensionInfo.put("teacherName", teacher.getProfile().getRealname());
            if (clazz != null) {
                extensionInfo.put("clazzId", clazzId);
                extensionInfo.put("clazzName", clazz.formalizeClazzName());
            }
            List<StudentParentRef> parentList = studentParentRefMap.get(student.getId());
            if (CollectionUtils.isEmpty(parentList)) {
                continue;
            }
            for (StudentParentRef parent : parentList) {
                if (extensionInfo.get("xxtMessageType") != null && XxtMessageType.作业报告留言.equals(extensionInfo.get("xxtMessageType"))) {
                    @SuppressWarnings("unchecked")
                    Map<Long, String> leaveWordsMap = (Map<Long, String>) extensionInfo.get("leaveWordsMap");
                    extensionInfo.put("textContent", leaveWordsMap.get(student.getId()));
                }
                List<UserWechatRef> userWechatRefs = parentWechatBindMap.get(parent.getParentId());
                if (CollectionUtils.isNotEmpty(userWechatRefs)) {
                    for (UserWechatRef ref : userWechatRefs) {
                        processSingleUserOpenId(ref.getUserId(), ref.getOpenId(), extensionInfo);
                    }
                }
            }
        }
    }

    @Override
    public void processSingleUser(Long userId, Map<String, Object> extensionInfo, WechatType wechatType) {
        if (wechatType == null) {
            logger.error("send weixin message error, wechatType is null");
            return;
        }
        if (wechatType == WechatType.TEACHER) {// 包班制支持,需要发给主账号
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(userId);
            if (mainTeacherId != null) {
                userId = mainTeacherId;
            }
        }
        List<UserWechatRef> refs = userWechatRefPersistence.findByUserId(userId, wechatType.getType());
        if (refs != null) {
            for (UserWechatRef ref : refs) {
                processSingleUserOpenId(userId, ref.getOpenId(), extensionInfo);
            }
        }
    }

    @Override
    public void processSingleUserOpenId(Long userId, String openId, Map<String, Object> extensionInfo) {
        processWechat(userId, openId, extensionInfo);
    }

    @Override
    public void processMultiUsersMultiOpenIds(Map<Long, List<String>> user_openId_map, Map<String, Object> extensionInfo) {
        for (Long userId : user_openId_map.keySet()) {
            for (String openId : user_openId_map.get(userId)) {
                processWechat(userId, openId, extensionInfo);
            }
        }
    }

    protected abstract void processWechat(Long userId, String openId, Map extensionInfo);

    protected Date getDefaultExpireTime() {
        return DateUtils.calculateDateDay(new Date(), 1);
    }

    protected Date getExpireTime(Date baseDate) {
        return DateUtils.calculateDateDay(baseDate, 1);
    }
}
