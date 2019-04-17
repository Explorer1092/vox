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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType.MANUAL_VALIDATION;

/**
 * @author Jia HuanYin
 * @since 2015/12/3
 */
@Named
public class CrmTeacherFakeService extends AbstractAdminService {

    private static final String TEACHER_DEFAKE_NOTE = "【管理员解除判假】";
    private static final String REVIEW_PASS_NOTE = "【老师判假审核通过】";
    private static final String REVIEW_REJECT_NOTE = "【老师判假审核驳回】";

    @Inject private MiscServiceClient miscServiceClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    public CrmTeacherFake load(String id) {
        return crmSummaryServiceClient.loadCrmTeacherFake(id);
    }

    public CrmTeacherFake loadFakedTeacher(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        List<CrmTeacherFake> fakedTeachers = crmSummaryServiceClient.findFakedTeacher(teacherId);
        return CollectionUtils.isEmpty(fakedTeachers) ? null : fakedTeachers.get(0);
    }

    public Page<CrmTeacherFake> loadTeacherFakes(ReviewStatus reviewStatus, Long teacherId, Pageable pageable) {
        return crmSummaryServiceClient.smartFindCrmTeacherFake(reviewStatus, teacherId, pageable);
    }

    public CrmTeacherFake reviewTeacherFake(String id, ReviewStatus reviewStatus, String reviewNote, AuthCurrentAdminUser adminUser) {
        if (StringUtils.isBlank(id) || reviewStatus == null || adminUser == null) {
            return null;
        }
        CrmTeacherFake teacherFake = load(id);
        if (teacherFake == null) {
            return null;
        }
        Long teacherId = teacherFake.getTeacherId();
        String adminName = adminUser.getAdminUserName();
        teacherFake.setReviewStatus(reviewStatus);
        teacherFake.setReviewer(adminName);
        teacherFake.setReviewerName(adminUser.getRealName());
        teacherFake.setReviewTime(new Date());
        String operation = REVIEW_PASS_NOTE;
        if (reviewStatus == ReviewStatus.REJECT) {
            teacherFake.setReviewNote(reviewNote);
            operation = REVIEW_REJECT_NOTE;
        } else if (reviewStatus == ReviewStatus.PASS) {
            fakeTeacher(teacherId, teacherFake.getFakeNote());
        }

        crmSummaryServiceClient.updateCrmTeacherFake(id, teacherFake);

        String log = StringUtils.isBlank(reviewNote) ? operation : reviewNote;

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(adminUser.getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
        userServiceRecord.setOperationContent("老师判假审核");
        userServiceRecord.setComments("审核结果:" + operation + ", 备注:" + log);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        // 发送消息到Agent
        Map<String, Object> command = new HashMap<>();
        command.put("command", "crm_review_teacher_fake");
        command.put("teacherId", teacherFake.getTeacherId());
        command.put("teacherName", teacherFake.getTeacherName());
        command.put("reviewStatus", reviewStatus);
        command.put("reviewerName", teacherFake.getReviewerName());
        command.put("reviewNote", StringUtils.isBlank(reviewNote)? "" : reviewNote);
        command.put("receiverId", teacherFake.getFakerId());
        Message message = Message.newMessage();
        message.withPlainTextBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);

        return teacherFake;
    }

    private void fakeTeacher(Long teacherId, String fakeNote) {
        MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, MANUAL_VALIDATION, fakeNote);
        if (message.isSuccess()) {
            // 发送申诉消息
            miscServiceClient.sendFakeAppealMessage(teacherId);
        }
    }

    public CrmTeacherFake defakeTeacher(Long teacherId, AuthCurrentAdminUser adminUser) {
        if (teacherId == null || adminUser == null) {
            return null;
        }
        CrmTeacherFake teacherFake = loadFakedTeacher(teacherId);
        if (teacherFake == null) {
            return null;
        }

        teacherFake.setReviewStatus(ReviewStatus.REJECT);
        teacherFake.setReviewer(adminUser.getAdminUserName());
        teacherFake.setReviewerName(adminUser.getRealName());
        teacherFake.setReviewNote(TEACHER_DEFAKE_NOTE);
        teacherFake.setReviewTime(new Date());

        crmSummaryServiceClient.updateCrmTeacherFake(teacherFake.getId(), teacherFake);

        return teacherFake;
    }
}
