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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.AmbassadorReportType;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.api.constant.MentorType;
import com.voxlearning.utopia.api.constant.TeacherTaskType;
import com.voxlearning.utopia.business.api.BusinessTeacherService;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionLib;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;
import com.voxlearning.utopia.data.CertificationCondition;
import com.voxlearning.utopia.entity.activity.TermBeginStudentAppRecord;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;
import com.voxlearning.utopia.mapper.*;
import com.voxlearning.utopia.service.business.base.AbstractBusinessTeacherService;
import com.voxlearning.utopia.service.business.cache.BusinessCache;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;
import lombok.Getter;

import java.util.*;

public class BusinessTeacherServiceClient extends AbstractBusinessTeacherService {

    @Getter
    @ImportService(interfaceClass = BusinessTeacherService.class)
    private BusinessTeacherService remoteReference;

    public void addPrizeDraw(Teacher teacher) {
        this.remoteReference.addPrizeDraw(teacher);
    }

    @Override
    public List<DisplayBookOfClazzMapper> getSameBookClazz(Long teacherId, Long clazzId) {
        return remoteReference.getSameBookClazz(teacherId, clazzId);
    }

    @Override
    public boolean showedUpgradeClazzBookTip(Long userId) {
        return remoteReference.showedUpgradeClazzBookTip(userId);
    }

    @Override
    public String generateUpgradeBookKey() {
        return remoteReference.generateUpgradeBookKey();
    }

    @Override
    public int studentsFinishedHomeworkCount(Long teacherId, Date start) {
        return remoteReference.studentsFinishedHomeworkCount(teacherId, start);
    }

    @Override
    public void changeUserAuthenticationState(Long userId, AuthenticationState authenticationState, Long operatorId, String operatorName) {
        remoteReference.changeUserAuthenticationState(userId, authenticationState, operatorId, operatorName);
    }

    @Override
    public CertificationCondition getCertificationCondition(Long userId) {
        return remoteReference.getCertificationCondition(userId);
    }

    @Override
    public MapMessage startCertificationApplication(Long userId) {
        return remoteReference.startCertificationApplication(userId);
    }

    @Override
    public List<Long> studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(Long teacherId) {
        return remoteReference.studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(teacherId);
    }

    @Override
    public TeacherNewUserTaskMapper getTeacherNewUserTaskMapper(User teacher) {
        return remoteReference.getTeacherNewUserTaskMapper(teacher);
    }

    public Map loadTeacherIndexData2(Teacher teacher) {
        if (teacher == null) {
            return Collections.emptyMap();
        }
        MapMessage message;
        try {
            message = remoteReference.loadTeacherIndexData(teacher);
        } catch (Exception ex) {
            logger.error("FAILED TO LOAD TEACHER '{}' INDEX DATA", teacher.getId(), ex);
            return Collections.emptyMap();
        }
        if (message.isSuccess()) {
            return (Map) message.get("indexData");
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public String encryptCodeGenerator(Long userId, String email, String subject) {
        return remoteReference.encryptCodeGenerator(userId, email, subject);
    }

    @Override
    public MapMessage nameValidator(String name) {
        return remoteReference.nameValidator(name);
    }

    @Override
    public MapMessage emailValidator(String email) {
        return remoteReference.emailValidator(email);
    }

    @Override
    public MapMessage mobileValidator(String mobile) {
        return remoteReference.mobileValidator(mobile);
    }

    @Override
    public MapMessage rstaffInviteTeacherBySms(User user, String[] mobiles) {
        return remoteReference.rstaffInviteTeacherBySms(user, mobiles);
    }

    @Override
    public MapMessage teacherInviteTeacherBySms(User user, String mobile, String realname, InvitationType type, String subject) {
        return remoteReference.teacherInviteTeacherBySms(user, mobile, realname, type, subject);
    }

    @Override
    public MapMessage wakeUpInvitedTeacherBySms(User inviter, User invitee, InvitationType type) {
        return remoteReference.wakeUpInvitedTeacherBySms(inviter, invitee, type);
    }

    @Override
    public MapMessage sendAuthenticateNotify(User sender, Long receiverId) {
        return remoteReference.sendAuthenticateNotify(sender, receiverId);
    }

    @Override
    public MapMessage teacherRegisterByMobile(String mobile, String realname, String province) {
        return remoteReference.teacherRegisterByMobile(mobile, realname, province);
    }

    @Override
    public MapMessage personalStatisticOfTeacherActivateTeacher(Long teacherId) {
        return remoteReference.personalStatisticOfTeacherActivateTeacher(teacherId);
    }

    @Override
    public List<ActivateInfoMapper> getPotentialTeacher(TeacherDetail teacher) {
        return remoteReference.getPotentialTeacher(teacher);
    }

    @Override
    public int getActivatingCount(Long teacherId) {
        return remoteReference.getActivatingCount(teacherId);
    }

    @Override
    public List<ActivateInfoMapper> getActivatingTeacher(Long teacherId) {
        return remoteReference.getActivatingTeacher(teacherId);
    }

    @Override
    public List<ActivateInfoMapper> getActivatedTeacher(Long teacherId) {
        return remoteReference.getActivatedTeacher(teacherId);
    }

    @Override
    public MapMessage activateTeacher(TeacherDetail inviter, ActivateMapper mapper) {
        return remoteReference.activateTeacher(inviter, mapper);
    }

    @Override
    public MapMessage deleteTeacherActivateTeacherHistory(Long inviterId, String historyId) {
        return remoteReference.deleteTeacherActivateTeacherHistory(inviterId, historyId);
    }

    @Override
    public MapMessage findUnauthenticatedTeacherInTheSameSchoolByIdOrName(Long schoolAmbassadorId, String token) {
        return remoteReference.findUnauthenticatedTeacherInTheSameSchoolByIdOrName(schoolAmbassadorId, token);
    }

    @Override
    public MapMessage recommendTeacherAuthentication(Long schoolAmbassadorId, Long recommendedTeacherId) {
        return remoteReference.recommendTeacherAuthentication(schoolAmbassadorId, recommendedTeacherId);
    }

    @Override
    public Map<String, Object> getActivateIntegralPopupContent(Long teacherId) {
        return remoteReference.getActivateIntegralPopupContent(teacherId);
    }

    @Override
    public void teacherActivateTeacherFinish(User invitee) {
        remoteReference.teacherActivateTeacherFinish(invitee);
    }

    @Override
    public MapMessage rstaffNotifyTeacherBySms(User user, Long teacherId, String flag) {
        return remoteReference.rstaffNotifyTeacherBySms(user, teacherId, flag);
    }

    @Override
    public MapMessage rstaffNotifyAllBySms(Long teacherId) {
        return remoteReference.rstaffNotifyAllBySms(teacherId);
    }

    @Override
    public List<HomeworkMapper> getHomeworkMapperList(Long teacherId, Subject subject) {
        return remoteReference.getHomeworkMapperList(teacherId, subject);
    }

    @Override
    public List<SmartClazzRank> findSmartClazzIntegralHistory(Long groupId, Date createDatetime) {
        return remoteReference.findSmartClazzIntegralHistory(groupId, createDatetime);
    }

    @Override
    public MapMessage resetSmartClazzStudentDisplay(Long groupId, List<Long> userIdList) {
        return remoteReference.resetSmartClazzStudentDisplay(groupId, userIdList);
    }

    @Override
    public Map<String, Object> findSmartClazzRewardHistory(Long clazzId, Long teacherId, Subject subject, Date startDate, Date endDate) {
        return remoteReference.findSmartClazzRewardHistory(clazzId, teacherId, subject, startDate, endDate);
    }

    @Override
    public MapMessage saveSmartClazzQuestion(Teacher teacher, TeacherDetail teacherDetail, Map<String, Object> jsonMap) {
        return remoteReference.saveSmartClazzQuestion(teacher, teacherDetail, jsonMap);
    }

    @Override
    @Deprecated
    public Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long clazzId, Subject subject, Pageable pageable) {
        return remoteReference.findSmartClazzQuestionPage(clazzId, subject, pageable);
    }

    @Override
    public Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long groupId, Pageable pageable) {
        return remoteReference.findSmartClazzQuestionPage(groupId, pageable);
    }

    @Override
    public List<SmartClazzQuestionLib> findSmartClazzQuestionById(Set<String> ids) {
        return remoteReference.findSmartClazzQuestionById(ids);
    }

    @Override
    public List<SmartClazzQuestionRef> findSmartClazzQuestionRefByQId(String questionId) {
        return remoteReference.findSmartClazzQuestionRefByQId(questionId);
    }

    @Override
    public MapMessage addSmartClazzQuestionRef(Teacher teacher, String questionId, List<Long> clazzIds) {
        return remoteReference.addSmartClazzQuestionRef(teacher, questionId, clazzIds);
    }

    @Override
    public MapMessage disabledSmartClazzQuestionRef(Long teacherId, Long clazzId, String questionId) {
        return remoteReference.disabledSmartClazzQuestionRef(teacherId, clazzId, questionId);
    }

    @Override
    public MapMessage generateSmartClazzQuestionReport(Long teacherId, SmartClazzQuestionReport smartClazzQuestionReport) {
        return remoteReference.generateSmartClazzQuestionReport(teacherId, smartClazzQuestionReport);
    }

    @Override
    public SmartClazzQuestionReport findQuestionReportById(String id) {
        return remoteReference.findQuestionReportById(id);
    }

    @Override
    @Deprecated
    public Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long clazzId, Subject subject, Date startDate, Date endDate, Pageable pageable) {
        return remoteReference.findSmartClazzQuestionReport(clazzId, subject, startDate, endDate, pageable);
    }

    @Override
    public Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long groupId, Date startDate, Date endDate, Pageable pageable) {
        return remoteReference.findSmartClazzQuestionReport(groupId, startDate, endDate, pageable);
    }

    @Override
    public Map<Long, CertificationCondition> batchGetCertificationCondition(List<Long> teacherIds) {
        return remoteReference.batchGetCertificationCondition(teacherIds);
    }

    @Override
    public MapMessage findMyMentorOrCandidates(Long menteeId, MentorCategory mentorCategory) {
        return remoteReference.findMyMentorOrCandidates(menteeId, mentorCategory);
    }

    @Override
    public MapMessage setUpMMRelationship(Long mentorId, Long menteeId, MentorCategory mentorCategory, MentorType initiativeType) {
        return remoteReference.setUpMMRelationship(mentorId, menteeId, mentorCategory, initiativeType);
    }

    @Override
    public List<Map<String, Object>> getMentoringTeacherList(Long mentorId) {
        return remoteReference.getMentoringTeacherList(mentorId);
    }

    @Override
    public void changeActivationType(Long teacherId, boolean flag) {
        remoteReference.changeActivationType(teacherId, flag);
    }

    @Override
    public List<Map<String, Object>> getIncrStudentCountTeacherList(Long schoolId) {
        return remoteReference.getIncrStudentCountTeacherList(schoolId);
    }

    @Override
    public Map<String, Object> getMentorLatestInfo(Long teacherId) {
        return remoteReference.getMentorLatestInfo(teacherId);
    }

    @Override
    public boolean isJoinCompetition(Long teacherId) {
        return remoteReference.isJoinCompetition(teacherId);
    }

    @Override
    public Map<String, Object> loadAmbassadorMyCompetitionInfo(Long teacherId, Long schoolId, Subject subject) {
        return remoteReference.loadAmbassadorMyCompetitionInfo(teacherId, schoolId, subject);
    }

    @Override
    public MapMessage joinAmbassadorCompetition(TeacherDetail teacherDetail) {
        return remoteReference.joinAmbassadorCompetition(teacherDetail);
    }

    @Override
    public Map<String, Object> loadTeacherNewHandTaskInfo(Long teacherId) {
        return remoteReference.loadTeacherNewHandTaskInfo(teacherId);
    }

    @Override
    @Deprecated
    public List<TeacherTaskRewardHistory> loadTeacherTaskRewardHistory(Long teacherId, TeacherTaskType taskType) {
        return remoteReference.loadTeacherTaskRewardHistory(teacherId, taskType);
    }

    @Override
    public MapMessage receiveTeacherTaskReward(Long teacherId, TeacherTaskType type, String rewardName) {
        return remoteReference.receiveTeacherTaskReward(teacherId, type, rewardName);
    }

    @Override
    public List<TermBeginStudentAppRecord> loadTermBeginListByTeacherId(Long teacherId) {
        return remoteReference.loadTermBeginListByTeacherId(teacherId);
    }

    @Override
    public Map<String, Long> getFinishCount(Teacher teacher) {
        return remoteReference.getFinishCount(teacher);
    }

    @Override
    public MapMessage setAmbassador(TeacherDetail teacher) {
        return remoteReference.setAmbassador(teacher);
    }

    @Override
    public MapMessage applySchoolAmbassador(SchoolAmbassador ambassador) {
        return remoteReference.applySchoolAmbassador(ambassador);
    }

    @Override
    public Map<String, Object> loadAmbassadorSHXInfo(TeacherDetail teacher) {
        return remoteReference.loadAmbassadorSHXInfo(teacher);
    }

    @Override
    public Map<String, Object> loadAmbassadorZSInfo(TeacherDetail teacher) {
        return remoteReference.loadAmbassadorZSInfo(teacher);
    }

    @Override
    public MapMessage remindTeacherForEffectHw(TeacherDetail teacherDetail) {
        return remoteReference.remindTeacherForEffectHw(teacherDetail);
    }

    @Override
    public MapMessage praiseTeacherForEffectHw(TeacherDetail teacherDetail) {
        return remoteReference.praiseTeacherForEffectHw(teacherDetail);
    }

    @Override
    public MapMessage resignationAmbassador(TeacherDetail detail) {
        return remoteReference.resignationAmbassador(detail);
    }

    @Override
    public List<Map<String, Object>> loadAmbassadorScoreHistory(Long ambassadorId) {
        return remoteReference.loadAmbassadorScoreHistory(ambassadorId);
    }

    @Override
    public List<Map<String, Object>> getContributionRank(Teacher teacher) {
        return remoteReference.getContributionRank(teacher);
    }

    @Override
    public MapMessage reportTeacher(TeacherDetail teacher, Long teacherId, String teacherName, String reason, AmbassadorReportType type) {
        return remoteReference.reportTeacher(teacher, teacherId, teacherName, reason, type);
    }

    @Override
    public Map<String, Object> calculateHomeworkMaxIntegralCount(TeacherDetail teacher, Collection<Long> clazzIds) {
        return remoteReference.calculateHomeworkMaxIntegralCount(teacher, clazzIds);
    }

    @Override
    public Map<String, Object> loadTeacherLuckyBagInfo(Long groupId, Long teacherId) {
        return remoteReference.loadTeacherLuckyBagInfo(groupId, teacherId);
    }

    @Override
    public MapMessage receiveLuckyBagClazzReward(Long groupId, Long teacherId) {
        return remoteReference.receiveLuckyBagClazzReward(groupId, teacherId);
    }

    @Override
    public PageImpl<Map<String, Object>> getUncertificatedTeacherListPage(Long teacherSchoolId, int pageNum, int pageSize) {
        return remoteReference.getUncertificatedTeacherListPage(teacherSchoolId, pageNum, pageSize);
    }

    @Override
    public MapMessage signIn(Long teacherId) {
        return remoteReference.signIn(teacherId);
    }

    @Override
    public MapMessage shareArticle(Long teacherId) {
        return remoteReference.shareArticle(teacherId);
    }

    public Map<String, List<TermBeginStudentAppRecord>> loadTermBeginListByTeacherId2(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyMap();
        }
        String key = CacheKeyGenerator.generateCacheKey(TermBeginStudentAppRecord.class, "teacherId", teacherId);
        List<TermBeginStudentAppRecord> records = BusinessCache.getBusinessCache().load(key);
        if (records == null) {
            try {
                records = remoteReference.loadTermBeginListByTeacherId(teacherId);
            } catch (Exception ex) {
                logger.error("FAILED TO LOAD TEACHER {} TERM BEGIN APP STUDENT LIST", teacherId, ex);
                return Collections.emptyMap();
            }
        }
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Map<String, List<TermBeginStudentAppRecord>> dataMap = new HashMap<>();
        for (TermBeginStudentAppRecord record : records) {
            if (dataMap.containsKey(record.getClazzName())) {
                List<TermBeginStudentAppRecord> recordList = new ArrayList<>(dataMap.get(record.getClazzName()));
                recordList.add(record);
                dataMap.put(record.getClazzName(), recordList);
            } else {
                List<TermBeginStudentAppRecord> recordList = new ArrayList<>();
                recordList.add(record);
                dataMap.put(record.getClazzName(), recordList);
            }
        }
        return dataMap;
    }

    @Override
    public MapMessage loadTeacherIndexData(Teacher teacher) {
        return remoteReference.loadTeacherIndexData(teacher);
    }

    @Override
    public MapMessage loadTeacherClazzIndexData(Teacher teacher) {
        return remoteReference.loadTeacherClazzIndexData(teacher);
    }

    public List<TeacherCardMapper> loadTeacherCardList(Teacher teacher, String sys, String ver, String imgDomain){
        return remoteReference.loadTeacherCardList(teacher, sys, ver, imgDomain);
    }

}
