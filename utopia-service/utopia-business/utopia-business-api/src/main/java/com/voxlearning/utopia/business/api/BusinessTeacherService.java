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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.AmbassadorReportType;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.api.constant.MentorType;
import com.voxlearning.utopia.api.constant.TeacherTaskType;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionLib;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;
import com.voxlearning.utopia.data.CertificationCondition;
import com.voxlearning.utopia.entity.activity.TermBeginStudentAppRecord;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;
import com.voxlearning.utopia.mapper.*;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181011")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BusinessTeacherService extends IPingable {

    MapMessage loadTeacherIndexData(Teacher teacher);

    MapMessage loadTeacherClazzIndexData(Teacher teacher);

    List<TeacherCardMapper> loadTeacherCardList(Teacher teacher, String sys, String ver, String imgDomain);

    void addPrizeDraw(Teacher teacher);
    // ========================================
    // TeacherBookService
    // ========================================

    List<DisplayBookOfClazzMapper> getSameBookClazz(Long teacherId, Long clazzId);


    /**
     * 是否展示升级课本的提示
     */
    boolean showedUpgradeClazzBookTip(Long userId);

    /**
     * 生成在UserExtensionAttribute中保存课本升级状态的key
     */
    String generateUpgradeBookKey();

    // ========================================
    // TeacherCertificationService
    // ========================================

    int studentsFinishedHomeworkCount(Long teacherId, Date start);

    void changeUserAuthenticationState(Long userId, AuthenticationState authenticationState, Long operatorId, String operatorName);

    /**
     * Gain the specified user's certification condition. Only teacher supported.
     *
     * @param userId user id
     * @return certification condition
     */
    CertificationCondition getCertificationCondition(Long userId);

    /**
     * Start certification application for given user. Only teacher supported.
     * Application allowed in following 2 cases:
     * 1. first time certification application
     * 2. current certification application is {@link AuthenticationState#AGAIN}
     *
     * @param userId user id
     */
    MapMessage startCertificationApplication(Long userId);

    List<Long> studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(Long teacherId);

    TeacherNewUserTaskMapper getTeacherNewUserTaskMapper(User teacher);
    // ========================================
    // TeacherInvitationService
    // ========================================

    String encryptCodeGenerator(Long userId, String email, String subject);

    MapMessage nameValidator(String name);

    MapMessage emailValidator(String email);

    MapMessage mobileValidator(String mobile);

    MapMessage rstaffInviteTeacherBySms(User user, String[] mobiles);

    // subject用来区分不同学科老师的短信文案，如果不传就用统一的
    MapMessage teacherInviteTeacherBySms(User user, String mobile, String realname, InvitationType type, String subject);

    MapMessage wakeUpInvitedTeacherBySms(User inviter, User invitee, InvitationType type);

    MapMessage sendAuthenticateNotify(User sender, Long receiverId);

    MapMessage teacherRegisterByMobile(String mobile, String realname, String province);

    // ========================================================================
    // TeacherActivateTeacherService
    // ========================================================================

    /**
     * 查询教师激活教师的统计信息
     *
     * @param teacherId 教师Id
     * @return "pcount" -- 成功唤醒人数，"icount" -- 累计获得金币
     */
    MapMessage personalStatisticOfTeacherActivateTeacher(Long teacherId);

    /**
     * 获取可以激活的教师列表
     *
     * @param teacher 教师
     * @return 可以激活的教师列表
     */
    List<ActivateInfoMapper> getPotentialTeacher(TeacherDetail teacher);

    /**
     * 正在唤醒的老师数量
     */
    int getActivatingCount(Long teacherId);

    /**
     * 获取被唤醒未成功的老师列表
     */
    List<ActivateInfoMapper> getActivatingTeacher(Long teacherId);

    /**
     * 获取已被唤醒成功的老师列表
     */
    List<ActivateInfoMapper> getActivatedTeacher(Long teacherId);

    /**
     * 激活教师
     *
     * @param inviter 教师
     * @param mapper  激活对象
     */
    MapMessage activateTeacher(TeacherDetail inviter, ActivateMapper mapper);

    /**
     * 取消激活
     */
    MapMessage deleteTeacherActivateTeacherHistory(Long inviterId, String historyId);

    /**
     * 通过名字或者id查询同校未认证教师，用于校园大使推荐认证
     *
     * @param schoolAmbassadorId 校园大使Id
     * @param token              名字或者Id
     * @return 返回教师信息
     */
    MapMessage findUnauthenticatedTeacherInTheSameSchoolByIdOrName(Long schoolAmbassadorId, String token);

    /**
     * 推荐认证
     */
    MapMessage recommendTeacherAuthentication(Long schoolAmbassadorId, Long recommendedTeacherId);

    /**
     * 查校园大使激活奖励，给前端弹窗用
     */
    Map<String, Object> getActivateIntegralPopupContent(Long teacherId);

    void teacherActivateTeacherFinish(User invitee);

    /**
     * 教研员邀请提醒老师 短信
     */
    MapMessage rstaffNotifyTeacherBySms(User user, Long teacherId, String flag);

    /**
     * 教研员邀请提醒所有老师 短信
     */
    MapMessage rstaffNotifyAllBySms(Long teacherId);

    List<HomeworkMapper> getHomeworkMapperList(Long teacherId, Subject subject);

    // ========================================
    // TeacherSmartClazzService
    // ========================================

    // 新体系已支持
    List<SmartClazzRank> findSmartClazzIntegralHistory(Long groupId, Date createDatetime);

    // 新体系已支持
    @Deprecated
    MapMessage resetSmartClazzStudentDisplay(Long groupId, List<Long> userIdList);

    // 新体系已支持
    Map<String, Object> findSmartClazzRewardHistory(Long clazzId, Long teacherId, Subject subject, Date startDate, Date endDate);

    // 新体系已支持
    MapMessage saveSmartClazzQuestion(Teacher teacher, TeacherDetail teacherDetail, Map<String, Object> jsonMap);

    @Deprecated
    Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long clazzId, Subject subject, Pageable pageable);

    // 新体系已支持
    Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long groupId, Pageable pageable);

    // 新体系已支持
    List<SmartClazzQuestionLib> findSmartClazzQuestionById(Set<String> ids);

    // 新体系已支持
    List<SmartClazzQuestionRef> findSmartClazzQuestionRefByQId(String questionId);

    // 新体系已支持
    MapMessage addSmartClazzQuestionRef(Teacher teacher, String questionId, List<Long> clazzIds);

    // 新体系已支持
    MapMessage disabledSmartClazzQuestionRef(Long teacherId, Long clazzId, String questionId);

    // 新体系已支持
    MapMessage generateSmartClazzQuestionReport(Long teacherId, SmartClazzQuestionReport smartClazzQuestionReport);

    // 新体系已支持
    SmartClazzQuestionReport findQuestionReportById(String id);

    @Deprecated
    Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long clazzId, Subject subject, Date startDate, Date endDate, Pageable pageable);

    // 新体系已支持
    Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long groupId, Date startDate, Date endDate, Pageable pageable);

    Map<Long, CertificationCondition> batchGetCertificationCondition(List<Long> teacherIds);

    // ========================================
    // Mentor-Mentee
    // ========================================

    MapMessage findMyMentorOrCandidates(Long menteeId, MentorCategory mentorCategory);

    MapMessage setUpMMRelationship(Long mentorId, Long menteeId, MentorCategory mentorCategory, MentorType initiativeType);

    List<Map<String, Object>> getMentoringTeacherList(Long mentorId);

    //将普通老师的激活请求改为校园大使的激活请求，或者反向操作
    void changeActivationType(Long teacherId, boolean flag);

//    MapMessage addInClazzForWechat(Long teacherId, Long clazzId);

    List<Map<String, Object>> getIncrStudentCountTeacherList(Long schoolId);

    Map<String, Object> getMentorLatestInfo(Long teacherId);

    boolean isJoinCompetition(Long teacherId);

    Map<String, Object> loadAmbassadorMyCompetitionInfo(Long teacherId, Long schoolId, Subject subject);

    MapMessage joinAmbassadorCompetition(TeacherDetail teacherDetail);

    Map<String, Object> loadTeacherNewHandTaskInfo(Long teacherId);

    @Deprecated
    List<TeacherTaskRewardHistory> loadTeacherTaskRewardHistory(Long teacherId, TeacherTaskType taskType);

    MapMessage receiveTeacherTaskReward(Long teacherId, TeacherTaskType type, String rewardName);

    // ========================================
    // Tiny Group Relevant
    // ========================================

    List<TermBeginStudentAppRecord> loadTermBeginListByTeacherId(Long teacherId);

    Map<String, Long> getFinishCount(Teacher teacher);

    MapMessage setAmbassador(TeacherDetail teacher);

    MapMessage applySchoolAmbassador(SchoolAmbassador ambassador);

    // 获取实习大使页面展示信息
    Map<String, Object> loadAmbassadorSHXInfo(TeacherDetail teacher);

    Map<String, Object> loadAmbassadorZSInfo(TeacherDetail teacher);

    MapMessage remindTeacherForEffectHw(TeacherDetail teacherDetail);

    MapMessage praiseTeacherForEffectHw(TeacherDetail teacherDetail);

    MapMessage resignationAmbassador(TeacherDetail detail);

    List<Map<String, Object>> loadAmbassadorScoreHistory(Long ambassadorId);

    List<Map<String, Object>> getContributionRank(Teacher teacher);

    MapMessage reportTeacher(TeacherDetail teacher, Long teacherId, String teacherName, String reason, AmbassadorReportType type);

    Map<String, Object> calculateHomeworkMaxIntegralCount(TeacherDetail teacher, Collection<Long> clazzIds);

    Map<String, Object> loadTeacherLuckyBagInfo(Long groupId, Long teacherId);

    MapMessage receiveLuckyBagClazzReward(Long groupId, Long teacherId);

    PageImpl<Map<String, Object>> getUncertificatedTeacherListPage(Long teacherSchoolId, int pageNum, int pageSize);

    /**
     * 老师签到任务
     * @param teacherId
     * @return
     * @author zhouwei
     */
    MapMessage signIn(Long teacherId);

    /**
     * 老师分享文章
     * @param teacherId
     * @return
     */
    MapMessage shareArticle(Long teacherId);
}