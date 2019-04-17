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

package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserActivity;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/12/2
 */
@Named
public class TeacherFakeService extends AbstractAgentService {

    private static final String SYSTEM_REVIEWER = "SYSTEM";
    private static final String SYSTEM_REVIEWER_NAME = "系统";
    private static final String SYSTEM_REVIEW_NOTE = "【系统自动审核】";

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private MiscServiceClient miscServiceClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;

    public boolean isActiveTeacher(Long teacherId) {
        long timeMills = DateUtils.addDays(new Date(), -30).getTime();
        if (teacherId == null) {
            return false;
        }
        List<GroupMapper> groups = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);
        Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> groupStudents = studentLoaderClient.loadGroupStudentIds(groupIds);
        if (MapUtils.isNotEmpty(groupStudents)) {
            Collection<List<Long>> students = groupStudents.values();
            int activeStudents = 0;
            for (List<Long> studentIds : students) {
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    for (Long studentId : studentIds) {
                        if (isActiveStudent(studentId, timeMills)) {
                            activeStudents++;
                            if (activeStudents >= 3) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isActiveStudent(Long studentId, long timeMills) {
        List<UserActivity> activities = userActivityServiceClient.getUserActivityService()
                .findUserActivities(studentId)
                .getUninterruptibly();
        if (CollectionUtils.isNotEmpty(activities)) {
            for (UserActivity activity : activities) {
                if (UserActivityType.LAST_HOMEWORK_TIME == activity.getActivityType()) {
                    Date activityTime = activity.getActivityTime();
                    return activityTime != null && activityTime.getTime() >= timeMills;
                }
            }
        }
        return false;
    }

    public boolean isFakedTeacher(Long teacherId) {
        if (teacherId == null) {
            return false;
        }
        List<CrmTeacherFake> fakedTeachers = crmSummaryServiceClient.findFakedTeacher(teacherId);
        return CollectionUtils.isNotEmpty(fakedTeachers);
    }

    public MapMessage fakeActiveTeacher(Long teacherId, String fakeNote, AuthCurrentUser currentUser) {
        if (teacherId == null || StringUtils.isBlank(fakeNote) || currentUser == null) {
            return MapMessage.errorMessage("参数异常!");
        }
        if (isFakedTeacher(teacherId)) {
            return MapMessage.successMessage();
        }
        Long fakerId = currentUser.getUserId();
        String fakerName = currentUser.getRealName();
        String fakerPhone = currentUser.getUserPhone();
        CrmTeacherFake teacherFake = saveFakeTeacher(teacherId, fakerId, fakerName, fakerPhone, fakeNote, null, null, null, null, ReviewStatus.WAIT);
        MapMessage msg = new MapMessage();
        msg.setSuccess(teacherFake != null);
        msg.setInfo(teacherFake == null ? "判定失败" : null);
        return msg;
    }

    public MapMessage fakeInactiveTeacher(Long teacherId, String fakeNote, AuthCurrentUser currentUser) {
        if (teacherId == null || StringUtils.isBlank(fakeNote) || currentUser == null) {
            return MapMessage.errorMessage("参数异常!");
        }
        if (isFakedTeacher(teacherId)) {
            return MapMessage.successMessage();
        }
        Long fakerId = currentUser.getUserId();
        String fakerName = currentUser.getRealName();
        String fakerPhone = currentUser.getUserPhone();
        String reviewer = SYSTEM_REVIEWER;
        String reviewerName = SYSTEM_REVIEWER_NAME;
        Date reviewTime = new Date();
        String reviewNote = SYSTEM_REVIEW_NOTE;
        ReviewStatus reviewStatus = ReviewStatus.PASS;
        CrmTeacherFake teacherFake = saveFakeTeacher(teacherId, fakerId, fakerName, fakerPhone, fakeNote, reviewer, reviewerName, reviewTime, reviewNote, reviewStatus);
        if (teacherFake == null) {
            return MapMessage.errorMessage("判定失败");
        }
        MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, CrmTeacherFakeValidationType.MANUAL_VALIDATION, fakeNote);
        if (message.isSuccess()) {
            // 发送申诉消息
            miscServiceClient.sendFakeAppealMessage(teacherId);

            // 记录USER_RECORD
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(String.valueOf(fakerId));
            userServiceRecord.setOperatorName(fakerName);
            userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
            userServiceRecord.setOperationContent("市场对老师判假");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        return message;
    }

    private CrmTeacherFake saveFakeTeacher(Long teacherId, Long fakerId, String fakerName, String fakerPhone, String fakeNote, String reviewer,
                                           String reviewerName, Date reviewTime, String reviewNote, ReviewStatus reviewStatus) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//        CrmTeacherSummary teacher = crmTeacherSummaryDao.findByTeacherId(teacherId);
        if (teacher == null) {
            return null;
        }
        CrmTeacherFake teacherFake = new CrmTeacherFake();
        teacherFake.setTeacherId(teacherId);
        teacherFake.setTeacherName(teacher.fetchRealname());
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        teacherFake.setSchoolId(school == null ? 0L : school.getId());
        teacherFake.setSchoolName(school == null ? "" : school.getCname());
        teacherFake.setFakerId(fakerId);
        teacherFake.setFakerName(fakerName);
        teacherFake.setFakerPhone(fakerPhone);
        teacherFake.setFakeNote(fakeNote);
        teacherFake.setReviewer(reviewer);
        teacherFake.setReviewerName(reviewerName);
        teacherFake.setReviewNote(reviewNote);
        teacherFake.setReviewStatus(reviewStatus);
        teacherFake.setReviewTime(reviewTime);

        crmSummaryServiceClient.saveCrmTeacherFake(teacherFake);

        return teacherFake;
    }

    public Map<String, List<CrmTeacherFake>> fakeTeachers(Long fakerId) {
        Map<String, List<CrmTeacherFake>> fakeTeachers = new HashMap<>();
        for (ReviewStatus status : ReviewStatus.values()) {
            fakeTeachers.put(status.name(), new ArrayList<>());
        }
        if (fakerId == null) {
            return fakeTeachers;
        }
        List<CrmTeacherFake> teacherFakes = crmSummaryServiceClient.findFakerIdIs(fakerId);
        if (CollectionUtils.isNotEmpty(teacherFakes)) {
            for (CrmTeacherFake teacherFake : teacherFakes) {
                ReviewStatus reviewStatus = teacherFake.getReviewStatus();
                if (reviewStatus != null) {
                    fakeTeachers.get(reviewStatus.name()).add(teacherFake);
                }
            }
        }
        return fakeTeachers;
    }

    public boolean haveWaitingReviewFakeRecord(Long teacherId) {
        if (teacherId == null) {
            return false;
        }
        List<CrmTeacherFake> fakedTeachers = crmSummaryServiceClient.findTeacherIdIs(teacherId);
        if (CollectionUtils.isNotEmpty(fakedTeachers)){
            return fakedTeachers.stream().anyMatch(item->ReviewStatus.WAIT.equals(item.getReviewStatus()));
        }
        return false;
    }

    public List<CrmTeacherFake> findFakedTeacher(Long teacherId){
        return crmSummaryServiceClient.findFakedTeacher(teacherId);
    }

    public void update(String id, CrmTeacherFake teacherFake) {
        crmSummaryServiceClient.updateCrmTeacherFake(id, teacherFake);
    }
}
