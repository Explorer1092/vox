/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.user.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.user.impl.dao.CrmTaskRecordDao;
import com.voxlearning.utopia.service.user.impl.dao.CrmTeacherFakeDao;
import com.voxlearning.utopia.service.user.impl.dao.CrmTeacherSummaryDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;

/**
 * Created by Summer Yang on 2016/5/10.
 */
@Named
@Service(interfaceClass = CrmSummaryService.class)
@ExposeService(interfaceClass = CrmSummaryService.class)
public class CrmSummaryServiceImpl extends SpringContainerSupport implements CrmSummaryService {

    @Inject private CrmTeacherFakeDao crmTeacherFakeDao;
    @Inject private CrmTeacherSummaryDao crmTeacherSummaryDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserManagementClient userManagementClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private CrmTaskRecordDao crmTaskRecordDao;

    @Override
    public MapMessage updateTeacherFakeType(Long teacherId, CrmTeacherFakeValidationType type, String desc) {

        // 处理包班制老师，主副账号一起处理
        Set<Long> relatedIds = teacherLoaderClient.loadRelTeacherIds(teacherId);

        for (Long relId : relatedIds) {
            CrmTeacherSummary summary = crmTeacherSummaryDao.findByTeacherId(relId);
            TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(relId);

            // 如果两个账号都被人工判假了，直接跳过
            if (summary != null && !summary.isNotManualFakeTeacher() && extAttribute != null && extAttribute.isFakeTeacher()) {
                continue;
            }

            userManagementClient.setTeacherFake(relId, true, null, type.name);

            crmTeacherSummaryDao.updateTeacherFakeType(relId, type, desc);
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage removeTeacherFakeType(Long teacherId) {
        return crmTeacherSummaryDao.removeTeacherFakeType(teacherId);
    }

    @Override
    public MapMessage saveCrmTeacherFake(CrmTeacherFake fake) {
        crmTeacherFakeDao.insert(fake);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateCrmTeacherFake(String id, CrmTeacherFake fake) {
        crmTeacherFakeDao.update(id, fake);
        return MapMessage.successMessage();
    }

    @Override
    public CrmTeacherFake loadCrmTeacherFake(String id) {
        return id == null ? null : crmTeacherFakeDao.load(id);
    }

    @Override
    public List<CrmTeacherFake> findFakedTeacher(Long teacherId) {
        return crmTeacherFakeDao.findFakedTeacher(teacherId);
    }

    @Override
    public List<CrmTeacherFake> findFakerIdIs(Long fakerId) {
        return crmTeacherFakeDao.findFakerIdIs(fakerId);
    }

    @Override
    public List<CrmTeacherFake> findTeacherIdIs(Long teacherId) {
        return crmTeacherFakeDao.findTeacherIdIs(teacherId);
    }

    @Override
    public Page<CrmTeacherFake> smartFindCrmTeacherFake(ReviewStatus reviewStatus, Long teacherId, Pageable pageable) {
        return crmTeacherFakeDao.smartFind(reviewStatus, teacherId, pageable);
    }

    @Override
    public MapMessage addWechatTaskRecord(Long userId, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, String content) {
        try {
            User user = userLoaderClient.loadUser(userId);
            if (user == null) {
                logger.error("Null user with userId = {}", userId);
                return MapMessage.errorMessage("保存工作记录失败，无此用户");
            }
            saveTaskRecord(user, recorder, recorderName, recordCategory, CrmContactType.微信咨询, null, content, null, null, null);
        } catch (Exception e) {
            logger.error("add wechat task record error : " + e.getMessage());
            return MapMessage.errorMessage("保存工作记录失败");
        }
        return MapMessage.successMessage("保存成功");
    }

    @Override
    public MapMessage addUserTaskRecord(Long userId, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, CrmContactType contactType, String title, String content, Integer callTime, String audioUrl, String callerId) {
        try {
            User user = userLoaderClient.loadUser(userId);
            if (user == null) {
                logger.error("Null user with userId = {}", userId);
                return MapMessage.errorMessage("保存工作记录失败，无此用户");
            }
            saveTaskRecord(user, recorder, recorderName, recordCategory, contactType, title, content, callTime, audioUrl, callerId);
        } catch (Exception e) {
            logger.error("add user task record error : " + e.getMessage());
            return MapMessage.errorMessage("保存工作记录失败");
        }
        return MapMessage.successMessage("保存成功");
    }

    private CrmTaskRecord saveTaskRecord(User user, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, CrmContactType contactType, String title, String content, Integer callTime, String audioUrl, String callerId) {
        CrmTaskRecord taskRecord = new CrmTaskRecord();
        taskRecord.setRecorder(recorder);
        taskRecord.setRecorderName(recorderName);
        taskRecord.setUserId(user.getId());
        taskRecord.setUserName(user.fetchRealname());
        taskRecord.setUserType(user.fetchUserType());
        taskRecord.touchCategory(recordCategory);
        taskRecord.setContactType(contactType);
        taskRecord.setTitle(title);
        taskRecord.setContent(content);
        taskRecord.setCallTime(callTime);
        taskRecord.setAudioUrl(audioUrl);
        taskRecord.setCallerId(callerId);
        String taskRecordId = crmTaskRecordDao.insert(taskRecord);
        try {
            if (UserType.TEACHER == user.fetchUserType()) {
                UserServiceRecord userRecord = new UserServiceRecord();
                userRecord.setUserId(user.getId());
                userRecord.setUserName(user.fetchRealname());
                userRecord.setOperatorId(recorder);
                userRecord.setOperatorName(recorderName);
                userRecord.setOperationType(contactType == CrmContactType.电话呼出 ? UserServiceRecordOperationType.客服外呼.name() : UserServiceRecordOperationType.用户咨询.name());
                userRecord.setOperationContent(ConversionUtils.toString(contactType.name()));
                userRecord.setComments(String.format("%s受理咨询,%s", recorderName, content));
                userRecord.setAdditions("CrmTaskRecord:" + taskRecordId);
                userServiceClient.saveUserServiceRecord(userRecord);
            }
        } catch (Exception e) {
            logger.error("userRecord is failed", e);
        }
        return taskRecord;
    }
}
