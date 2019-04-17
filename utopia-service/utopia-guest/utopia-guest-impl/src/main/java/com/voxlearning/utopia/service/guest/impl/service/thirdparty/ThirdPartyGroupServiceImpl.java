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

package com.voxlearning.utopia.service.guest.impl.service.thirdparty;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.guest.impl.dao.ThirdPartyGroupStudentRefPersistence;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupStudentRef;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupTeacherRef;
import com.voxlearning.utopia.service.user.api.service.thirdparty.ThirdPartyGroupService;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2016/9/26
 */
@Named
@Service(interfaceClass = ThirdPartyGroupService.class)
@ExposeService(interfaceClass = ThirdPartyGroupService.class)
public class ThirdPartyGroupServiceImpl extends SpringContainerSupport implements ThirdPartyGroupService {

    @Inject private RaikouSDK raikouSDK;
    @Inject private ThirdPartyGroupStudentRefPersistence thirdPartyGroupStudentRefPersistence;
    @Inject private ThirdPartyGroupStudentRefProcessor thirdPartyGroupStudentRefProcessor;
    @Inject private ThirdPartyGroupTeacherRefProcessor thirdPartyGroupTeacherRefProcessor;
    @Inject private UserLoaderClient userLoaderClient;

    @Override
    public MapMessage createTeacherGroup(Long userId, Subject subject, ThirdPartyGroupType groupType) {
        if (userId == null) {
            return MapMessage.errorMessage("找不到老师信息");
        }

        User u = userLoaderClient.loadUser(userId);
        if (u == null) {
            return null;
        }

        Long groupId = internalCreateGroup(subject, groupType);
        if (groupId == null) {
            return MapMessage.errorMessage("创建分组失败");
        }

        ThirdPartyGroupTeacherRef ref = ThirdPartyGroupTeacherRef.newInstance(groupId, userId);
        thirdPartyGroupTeacherRefProcessor.persist(ref);

        return MapMessage.successMessage().add("id", groupId);
    }


    @Override
    public MapMessage createGroup(Subject subject, ThirdPartyGroupType groupType) {
        Long groupId = internalCreateGroup(subject, groupType);
        if (groupId == null) {
            return MapMessage.errorMessage("创建分组失败");
        }

        return MapMessage.successMessage().add("id", groupId);
    }

    public MapMessage addStudentToGroup(Long studentId, Long groupId) {
        if (studentId == null || groupId == null) {
            return MapMessage.errorMessage();
        }

        List<ThirdPartyGroupStudentRef> refs = thirdPartyGroupStudentRefPersistence.findByUserId(studentId);
        if (refs.stream().map(ThirdPartyGroupStudentRef::getGroupId).anyMatch(e -> Objects.equals(e, groupId))) {
            return MapMessage.errorMessage("学生已在该group中");
        }

        ThirdPartyGroupStudentRef thirdPartyGroupStudentRef = ThirdPartyGroupStudentRef.newInstance(groupId, studentId);
        thirdPartyGroupStudentRefProcessor.persist(thirdPartyGroupStudentRef);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage changeGroupTeacher(Long groupId, Long teacherId) {
        if (groupId == null || teacherId == null) {
            return MapMessage.errorMessage();
        }

        thirdPartyGroupTeacherRefProcessor.disableByGroupId(groupId);
        thirdPartyGroupTeacherRefProcessor.persist(ThirdPartyGroupTeacherRef.newInstance(groupId, teacherId));

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage removeGroupStudents(Long groupId, Collection<Long> studentIds) {
        if (groupId == null || CollectionUtils.isEmpty(studentIds)) {
            return MapMessage.errorMessage();
        }
        studentIds.forEach(s -> thirdPartyGroupStudentRefProcessor.disableByUserIdAndGroupId(s, groupId));

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage changeGroupName(Long groupId, String groupName) {
        if (groupId == null || groupId <= 0 || groupName == null) {
            return MapMessage.errorMessage();
        }
        Boolean ret = raikouSDK.getClazzClient()
                .getThirdPartyGroupServiceClient()
                .getThirdPartyGroupService()
                .updateGroupName(groupId, groupName)
                .getUninterruptibly();
        return new MapMessage().setSuccess(SafeConverter.toBoolean(ret));
    }

    private Long internalCreateGroup(Subject subject, ThirdPartyGroupType groupType) {
        ThirdPartyGroup thirdPartyGroup = new ThirdPartyGroup();
        thirdPartyGroup.setSubject(subject);
        thirdPartyGroup.setClazzId(0L);
        thirdPartyGroup.setGroupName(null);
        thirdPartyGroup.setGroupType(groupType);
        thirdPartyGroup = raikouSDK.getClazzClient()
                .getThirdPartyGroupServiceClient()
                .getThirdPartyGroupService()
                .createGroup(thirdPartyGroup)
                .getUninterruptibly();
        return thirdPartyGroup.getId();
    }

}
