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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.business.api.BusinessTeacherService;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionLib;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;
import com.voxlearning.utopia.business.api.mapper.SmartClazzStudentResult;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.CertificationCondition;
import com.voxlearning.utopia.entity.activity.StudentLuckyBagRecord;
import com.voxlearning.utopia.entity.activity.TermBeginStudentAppRecord;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;
import com.voxlearning.utopia.mapper.*;
import com.voxlearning.utopia.service.business.base.AbstractBusinessTeacherService;
import com.voxlearning.utopia.service.business.impl.dao.StudentLuckyBagRecordPersistence;
import com.voxlearning.utopia.service.business.impl.dao.TermBeginStudentAppRecordPersistence;
import com.voxlearning.utopia.service.business.impl.service.teacher.*;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.card.TeacherCardDataContext;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.card.TeacherCardDataLoader;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.clazzindex.TeacherClazzIndexDataContext;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.index.TeacherIndexDataContext;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.index.TeacherIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.user.UserTaskService;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkCacheServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.client.TeacherTaskRewardHistoryServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.temp.LuckyBagActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.每个学生完成作业老师获得积分;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.每个学生完成测验老师获得积分;


@Named
@Service(interfaceClass = BusinessTeacherService.class)
@ExposeService(interfaceClass = BusinessTeacherService.class)
public class BusinessTeacherServiceImpl extends AbstractBusinessTeacherService {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private BusinessCacheSystem businessCacheSystem;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private DeprecatedAmbassadorService deprecatedAmbassadorService;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private DeprecatedMentorService deprecatedMentorService;
    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private NewHomeworkCacheServiceClient newHomeworkCacheServiceClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private StudentLuckyBagRecordPersistence studentLuckyBagRecordPersistence;
    @Inject private TeacherActivateTeacherServiceImpl teacherActivateTeacherService;
    @Inject private TeacherBookService teacherBookService;
    @Inject private TeacherCardDataLoader teacherCardDataLoader;
    @Inject private TeacherCertificationServiceImpl teacherCertificationService;
    @Inject private TeacherHomeworkServiceImpl teacherHomeworkService;
    @Inject private TeacherIndexDataLoader teacherIndexDataLoader;
    @Inject private TeacherInvitationServiceImpl teacherInvitationService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherSmartClazzServiceImpl teacherSmartClazzServiceImpl;
    @Inject private TeacherTaskRewardHistoryServiceClient teacherTaskRewardHistoryServiceClient;
    @Inject private TermBeginStudentAppRecordPersistence termBeginStudentAppRecordPersistence;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserTaskService userTaskService;
    @Inject private WechatLoaderClient wechatLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    @AlpsPubsubPublisher(topic = "utopia.teacher.task.signin.topic")
    private MessagePublisher messagePublisherSignIn;

    @AlpsPubsubPublisher(topic = "utopia.teacher.share.article.queue")
    private MessagePublisher messagePublisherShareArticle;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @Override
    public MapMessage loadTeacherIndexData(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        TeacherIndexDataContext context = new TeacherIndexDataContext();
        context.setTeacher(teacher);
        context.setClazzList(deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList()));
        context.setSchool(asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId())
                .getUninterruptibly());
        // FIXME 首页老师引导学生使用时需要明文mobile
        String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "be:loadTeacherIndexData", SafeConverter.toString(teacher.getId()));
        context.setMobile(StringUtils.defaultString(phone));
        Map<String, Object> indexData = teacherIndexDataLoader.process(context);
        return MapMessage.successMessage().add("indexData", indexData);
    }

    @Override
    public MapMessage loadTeacherClazzIndexData(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        TeacherClazzIndexDataContext context = new TeacherClazzIndexDataContext();
        context.setTeacher(teacher);
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());

        // FIXME 强制返回系统班级主页
        Map<String, Object> indexData = new LinkedHashMap<>();
        indexData.put("systemClazz", "");
        return MapMessage.successMessage().add("indexData", indexData);

//        context.setClazzs(clazzs);
//        Map<String, Object> indexData = teacherClazzIndexDataLoader.process(context);
//        return MapMessage.successMessage().add("indexData", indexData);
    }

    @Override
    public List<TeacherCardMapper> loadTeacherCardList(Teacher teacher, String sys, String ver, String imgDomain) {
        if (teacher == null) {
            return Collections.emptyList();
        }

        TeacherCardDataContext context = new TeacherCardDataContext(teacher, sys, ver, imgDomain);
        return teacherCardDataLoader.process(context);
    }

    @Override
    public void addPrizeDraw(Teacher teacher) {
        if (Objects.isNull(teacher) || Objects.isNull(teacher.getId())) {
            return;
        }
        int freeChance = 5;
        String key = CacheKeyGenerator.generateCacheKey("teacher_write_comments_or_scholar_beans_for_week", null, new Object[]{teacher.getId()});
        Long l = businessCacheSystem.CBS.unflushable.incr(key, 1, 1, DateUtils.getCurrentToWeekEndSecond());
        if (SafeConverter.toLong(l) == 1) {
            MapMessage message = campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.TEACHER_LOTTERY, teacher.getId(), freeChance);
            if (!message.isSuccess()) {
                logger.error("add lottery chance error, no wrapper find {}, teacher {}, count {}", CampaignType.TEACHER_LOTTERY.name(), teacher.getId(), freeChance);
                businessCacheSystem.CBS.unflushable.delete(key);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getIncrStudentCountTeacherList(Long schoolId) {
        return deprecatedMentorService.getIncrStudentCountTeacherList(schoolId);
    }

    @Override
    public Map<String, Object> getMentorLatestInfo(Long teacherId) {
        return deprecatedMentorService.getMentorLatestInfo(teacherId);
    }

    @Override
    public List<DisplayBookOfClazzMapper> getSameBookClazz(Long teacherId, Long clazzId) {
        return teacherBookService.getSameBookClazz(teacherId, clazzId);
    }

    @Override
    public boolean showedUpgradeClazzBookTip(Long userId) {
        return teacherBookService.showedUpgradeClazzBookTip(userId);
    }

    @Override
    public String generateUpgradeBookKey() {
        return teacherBookService.generateUpgradeBookKey();
    }

    /**
     * 某个老师在某个时间段之间带来的认证学生数（做作业大于3次），
     *
     * @param teacherId
     * @param start     可为null，表示不受start限制
     * @return
     */
    @Override
    public int studentsFinishedHomeworkCount(Long teacherId, Date start) {
        if (teacherId == null) {
            return 0;
        }
        return teacherCertificationService.studentsFinishedHomeworkCount(teacherId, start);
    }

    @Override
    public void changeUserAuthenticationState(Long userId, AuthenticationState authenticationState, Long operatorId, String operatorName) {
        teacherCertificationService.changeUserAuthenticationState(userId, authenticationState, operatorId, operatorName);
    }

    @Override
    public CertificationCondition getCertificationCondition(Long userId) {
        return teacherCertificationService.getCertificationCondition(userId);
    }

    @Override
    public Map<Long, CertificationCondition> batchGetCertificationCondition(List<Long> teacherIds) {
        return teacherCertificationService.batchGetCertificationCondition(teacherIds);
    }

    @Override
    public MapMessage startCertificationApplication(Long userId) {
        return teacherCertificationService.startCertificationApplication(userId);
    }

    @Override
    public List<Long> studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(Long teacherId) {
        return teacherCertificationService.studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(teacherId, null);
    }

    @Override
    public TeacherNewUserTaskMapper getTeacherNewUserTaskMapper(User teacher) {
        return userTaskService.getTeacherNewUserTaskMapper(teacher);
    }

    @Override
    public String encryptCodeGenerator(Long userId, String email, String subject) {
        return teacherInvitationService.encryptCodeGenerator(userId, email, subject);
    }

    @Override
    public MapMessage nameValidator(String name) {
        return teacherInvitationService.nameValidator(name);
    }

    @Override
    public MapMessage emailValidator(String email) {
        return teacherInvitationService.emailValidator(email);
    }

    @Override
    public MapMessage mobileValidator(String mobile) {
        return teacherInvitationService.mobileValidator(mobile);
    }

    @Override
    public MapMessage rstaffInviteTeacherBySms(User user, String[] mobiles) {
        return teacherInvitationService.rstaffInviteTeacherBySms(user, mobiles);
    }

    @Override
    public MapMessage teacherInviteTeacherBySms(User user, String mobile, String realname, InvitationType type, String subject) {
        return teacherInvitationService.teacherInviteTeacherBySms(user, mobile, realname, type, subject);
    }

    public MapMessage wakeUpInvitedTeacherBySms(User inviter, User invitee, InvitationType type) {
        return teacherInvitationService.wakeUpInvitedTeacherBySms(inviter, invitee, type);
    }

    @Override
    public MapMessage sendAuthenticateNotify(User sender, Long receiverId) {
        return teacherInvitationService.sendAuthenticateNotify(sender, receiverId);
    }

    @Override
    public MapMessage personalStatisticOfTeacherActivateTeacher(Long teacherId) {
        return teacherActivateTeacherService.personalStatisticOfTeacherActivateTeacher(teacherId);
    }

    @Override
    public List<ActivateInfoMapper> getPotentialTeacher(TeacherDetail teacher) {
        return teacherActivateTeacherService.getPotentialTeacher(teacher);
    }

    @Override
    public void changeActivationType(Long teacherId, boolean flag) {
        deprecatedAmbassadorService.changeActivationType(teacherId, flag);
    }

    @Override
    public int getActivatingCount(Long teacherId) {
        return teacherActivateTeacherService.getActivatingCount(teacherId);
    }

    @Override
    public MapMessage activateTeacher(TeacherDetail inviter, ActivateMapper mapper) {
        return teacherActivateTeacherService.activateTeacher(inviter, mapper);
    }

    @Override
    public List<ActivateInfoMapper> getActivatingTeacher(Long teacherId) {
        return teacherActivateTeacherService.getActivatingTeacher(teacherId);
    }

    @Override
    public List<ActivateInfoMapper> getActivatedTeacher(Long teacherId) {
        return teacherActivateTeacherService.getActivatedTeacher(teacherId);
    }

    @Override
    public MapMessage deleteTeacherActivateTeacherHistory(Long inviterId, String historyId) {
        return teacherActivateTeacherService.deleteTeacherActivateTeacherHistory(inviterId, historyId);
    }

    @Override
    public MapMessage findUnauthenticatedTeacherInTheSameSchoolByIdOrName(Long schoolAmbassadorId, String token) {
        return teacherActivateTeacherService.findUnauthenticatedTeacherInTheSameSchoolByIdOrName(schoolAmbassadorId, token);
    }

    @Override
    public MapMessage recommendTeacherAuthentication(Long schoolAmbassadorId, Long recommendedTeacherId) {
        return teacherActivateTeacherService.recommendTeacherAuthentication(schoolAmbassadorId, recommendedTeacherId);
    }

    @Override
    public Map<String, Object> getActivateIntegralPopupContent(Long teacherId) {
        return teacherActivateTeacherService.getActivateIntegralPopupContent(teacherId);
    }

    @Override
    public void teacherActivateTeacherFinish(User invitee) {
        teacherActivateTeacherService.teacherActivateTeacherFinish(invitee);
    }


    @Override
    public MapMessage rstaffNotifyTeacherBySms(User user, Long teacherId, String flag) {
        return teacherInvitationService.rstaffNotifyTeacherBySms(user, teacherId, flag);
    }

    @Override
    public MapMessage rstaffNotifyAllBySms(Long userId) {
        return teacherInvitationService.rstaffNotifyAllBySms(userId);
    }

    @Override
    public MapMessage teacherRegisterByMobile(String mobile, String realname, String province) {
        return teacherInvitationService.teacherRegisterByMobile(mobile, realname, province);
    }

    @Override
    public List<HomeworkMapper> getHomeworkMapperList(Long teacherId, Subject subject) {
//        return teacherHomeworkService.getHomeworkMapperList(teacherId, subject);
        return teacherHomeworkService.getClazzGroupHomeworkMappers(teacherId, subject);
    }


    @Override
    public List<SmartClazzRank> findSmartClazzIntegralHistory(Long groupId, Date createDatetime) {
        return teacherSmartClazzServiceImpl.findSmartClazzIntegralHistory(groupId, createDatetime);
    }

    @Override
    public MapMessage resetSmartClazzStudentDisplay(Long groupId, List<Long> userIdList) {
        return clazzIntegralServiceClient.getClazzIntegralService()
                .resetSmartClazzStudentDisplay(groupId, userIdList)
                .getUninterruptibly();
    }


    @Override
    public Map<String, Object> findSmartClazzRewardHistory(Long clazzId, Long teacherId, Subject subject, Date startDate, Date endDate) {
        return teacherSmartClazzServiceImpl.findSmartClazzRewardHistory(clazzId, teacherId, subject, startDate, endDate);
    }

    @Override
    public MapMessage saveSmartClazzQuestion(Teacher teacher, TeacherDetail teacherDetail, Map<String, Object> jsonMap) {
        return teacherSmartClazzServiceImpl.saveSmartClazzQuestion(teacher, teacherDetail, jsonMap);
    }

    @Override
    public Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long clazzId, Subject subject, Pageable pageable) {
        return teacherSmartClazzServiceImpl.findSmartClazzQuestionPage(clazzId, subject, pageable);
    }

    @Override
    public Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long groupId, Pageable pageable) {
        return teacherSmartClazzServiceImpl.findSmartClazzQuestionPage(groupId, pageable);
    }

    @Override
    public List<SmartClazzQuestionLib> findSmartClazzQuestionById(Set<String> ids) {
        return teacherSmartClazzServiceImpl.findSmartClazzQuestionById(ids);
    }

    @Override
    public List<SmartClazzQuestionRef> findSmartClazzQuestionRefByQId(String questionId) {
        return teacherSmartClazzServiceImpl.findSmartClazzQuestionRefByQId(questionId);
    }

    @Override
    public MapMessage addSmartClazzQuestionRef(Teacher teacher, String questionId, List<Long> clazzIds) {
        return teacherSmartClazzServiceImpl.addSmartClazzQuestionRef(teacher, questionId, clazzIds);
    }

    @Override
    public MapMessage disabledSmartClazzQuestionRef(Long teacherId, Long clazzId, String questionId) {
        return teacherSmartClazzServiceImpl.disabledSmartClazzQuestionRef(teacherId, clazzId, questionId);
    }

    @Override
    public MapMessage generateSmartClazzQuestionReport(Long teacherId, SmartClazzQuestionReport smartClazzQuestionReport) {
        return teacherSmartClazzServiceImpl.generateSmartClazzQuestionReport(teacherId, smartClazzQuestionReport);
    }

    @Override
    public SmartClazzQuestionReport findQuestionReportById(String id) {
        return teacherSmartClazzServiceImpl.findQuestionReportById(id);
    }

    @Override
    public Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long clazzId, Subject subject, Date startDate, Date endDate, Pageable pageable) {
        return teacherSmartClazzServiceImpl.findSmartClazzQuestionReport(clazzId, subject, startDate, endDate, pageable);
    }

    @Override
    public Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long groupId, Date startDate, Date endDate, Pageable pageable) {
        Page<SmartClazzQuestionReport> smartClazzQuestionReport = teacherSmartClazzServiceImpl.findSmartClazzQuestionReport(groupId, startDate, endDate, pageable);
        // 这里根据答题详情重新计算答题数和正确数
        // 这事因为爱提问有时候会给出错误数据
        smartClazzQuestionReport.getContent().forEach(r -> {
            int anwserCount = 0;
            int correctAnwserCount = 0;
            for (SmartClazzStudentResult s : r.getStudents()) {
                if (StringUtils.isNotEmpty(s.getStudentAnswer())) {
                    anwserCount++;
                }
                if (StringUtils.equals(s.getStudentAnswer(), r.getAnswer())) {
                    correctAnwserCount++;
                }
            }
            r.setStudentAnswerCount(anwserCount);
            r.setCorrectAnswerCount(correctAnwserCount);
        });
        return smartClazzQuestionReport;
    }

    // ========================================
    // Mentor-Mentee
    // ========================================

    @Override
    public MapMessage findMyMentorOrCandidates(Long menteeId, MentorCategory mentorCategory) {
        return deprecatedMentorService.findMyMentorOrCandidates(menteeId, mentorCategory);
    }

    @Override
    public MapMessage setUpMMRelationship(Long mentorId, Long menteeId, MentorCategory mentorCategory, MentorType initiativeType) {
        return deprecatedMentorService.setUpMMRelationship(mentorId, menteeId, mentorCategory, initiativeType);
    }

    @Override
    public PageImpl<Map<String, Object>> getUncertificatedTeacherListPage(Long teacherSchoolId, int pageNum, int pageSize) {
        return deprecatedMentorService.getUncertificatedTeacherListPage(teacherSchoolId, pageNum, pageSize);
    }

    @Override
    public List<Map<String, Object>> getMentoringTeacherList(Long mentorId) {
        return deprecatedMentorService.getMentoringTeacherList(mentorId);
    }

    @Override
    public boolean isJoinCompetition(Long teacherId) {
        return deprecatedAmbassadorService.isJoinCompetition(teacherId);
    }

    @Override
    public Map<String, Object> loadAmbassadorMyCompetitionInfo(Long teacherId, Long schoolId, Subject subject) {
        return deprecatedAmbassadorService.loadAmbassadorCompetitiondDetailRank(teacherId, schoolId, subject);
    }

    @Override
    public MapMessage joinAmbassadorCompetition(TeacherDetail teacherDetail) {
        return deprecatedAmbassadorService.joinAmbassadorCompetition(teacherDetail);
    }

    @Override
    public Map<String, Object> loadTeacherNewHandTaskInfo(Long teacherId) {
        Map<String, Object> taskInfo = new HashMap<>();
        //是否填写地址
        boolean addressFlag = false;
        UserShippingAddress shippingAddress = userLoaderClient.loadUserShippingAddress(teacherId);
        if (shippingAddress != null && StringUtils.isNotBlank(shippingAddress.getDetailAddress())) {
            addressFlag = true;
        }
        //是否绑定微信
        boolean wechatFlag = false;
        Map<Long, List<UserWechatRef>> wechatRefMap = wechatLoaderClient.loadUserWechatRefs(Collections.singletonList(teacherId), WechatType.TEACHER);
        if (wechatRefMap != null && CollectionUtils.isNotEmpty(wechatRefMap.get(teacherId))) {
            wechatFlag = true;
        }
        //是否论坛发帖
        boolean bbsFlag = false;
        String url = ProductConfig.getBbsSiteBaseUrl() + "/open.php?mod=postCount";
        String responseStr = HttpRequestExecutor.defaultInstance()
                .post(url)
                .addParameter("userId", SafeConverter.toString(teacherId))
                .execute()
                .getResponseString();
        if (StringUtils.isNotBlank(responseStr)) {
            Map<String, Object> data = JsonUtils.fromJson(responseStr);
            if (data != null && ConversionUtils.toInt(data.get("count")) > 0) {
                bbsFlag = true;
            }
        }
        boolean allFlag = false;
        if (addressFlag && wechatFlag && bbsFlag) {
            allFlag = true;
        }
        taskInfo.put("addressFlag", addressFlag);
        taskInfo.put("wechatFlag", wechatFlag);
        taskInfo.put("bbsFlag", bbsFlag);
        taskInfo.put("allFlag", allFlag);
        return taskInfo;
    }

    @Override
    @Deprecated
    public List<TeacherTaskRewardHistory> loadTeacherTaskRewardHistory(Long teacherId, TeacherTaskType taskType) {
        if (teacherId == null || taskType == null) {
            return Collections.emptyList();
        }
        return teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                .findTeacherTaskRewardHistories(teacherId, taskType.name())
                .getUninterruptibly();
    }

    @Override
    public MapMessage receiveTeacherTaskReward(Long teacherId, TeacherTaskType type, String rewardName) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (type == null || teacher == null) {
            return MapMessage.errorMessage("领取失败");
        }
        if (type == TeacherTaskType.NEW_HAND_TASK) {
            //判断是否满足新手任务
            Map<String, Object> taskInfo = loadTeacherNewHandTaskInfo(teacherId);
            if (DateUtils.calculateDateDay(new Date(), -7).after(teacher.getCreateTime())) {
                return MapMessage.errorMessage("对不起，奖励已过期！");
            }
            if (!ConversionUtils.toBool(taskInfo.get("allFlag"))) {
                return MapMessage.errorMessage("对不起，未完成新手奖励任务(填写资料/绑定微信/论坛报到)，无法领取奖励");
            }
            List<TeacherTaskRewardHistory> histories = teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                    .findTeacherTaskRewardHistories(teacher.getId(), type.name())
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(histories)) {
                return MapMessage.errorMessage("对不起，请不要重复领取！");
            }
            //新手任务奖励 100金币
            IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.老师任务奖励_产品平台, 1000);
            integralHistory.setComment("完成新手任务奖励！");
            if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                return MapMessage.errorMessage("领取失败！");
            }
            //记录历史
            TeacherTaskRewardHistory history = new TeacherTaskRewardHistory();
            history.setTeacherId(teacherId);
            history.setRewardName("100园丁豆");
            history.setTaskType(type.name());
            teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                    .insertTeacherTaskRewardHistory(history)
                    .awaitUninterruptibly();
        } else if (type == TeacherTaskType.WEEK_ASSIGN_TASK) {
            //每周布置作业奖励
            Map<Long, Set<String>> dataMap = newHomeworkCacheServiceClient.getNewHomeworkCacheService().assignHomeworkAndQuizDayCountManager_currentDays(Collections.singletonList(teacherId));
            if (dataMap.get(teacherId) == null || dataMap.get(teacherId).size() < 3) {
                return MapMessage.errorMessage("对不起，本周布置作业(测验)天数没有达到3天，无法领取奖励");
            }
            List<TeacherTaskRewardHistory> histories = teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                    .findTeacherTaskRewardHistories(teacher.getId(), type.name())
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(histories)) {
                histories = histories.stream().filter(h -> h.getCreateDatetime().after(WeekRange.current().getStartDate())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(histories)) {
                    return MapMessage.errorMessage("对不起，请不要重复领取!");
                }
            }
            if (StringUtils.equals("GOLD", rewardName)) {
                //5金币
                IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.老师任务奖励_产品平台, 50);
                integralHistory.setComment("完成每周布置作业任务奖励!");
                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    return MapMessage.errorMessage("领取失败!");
                }
                //记录历史
                TeacherTaskRewardHistory history = new TeacherTaskRewardHistory();
                history.setTeacherId(teacherId);
                history.setRewardName("5园丁豆");
                history.setTaskType(type.name());
                teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                        .insertTeacherTaskRewardHistory(history)
                        .awaitUninterruptibly();
            } else {
                //抽奖次数15次
                MapMessage message = campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.TEACHER_LOTTERY, teacherId, 15);
                if (message.isSuccess()) {
                    //记录历史
                    TeacherTaskRewardHistory history = new TeacherTaskRewardHistory();
                    history.setTeacherId(teacherId);
                    history.setRewardName("微信免费抽奖15次");
                    history.setTaskType(type.name());
                    teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                            .insertTeacherTaskRewardHistory(history)
                            .awaitUninterruptibly();
                } else {
                    logger.error("add lottery chance error, no wrapper find {}, teacher {}, count {}", CampaignType.TEACHER_LOTTERY.name(), teacherId, 15);
                }
            }

        } else if (type == TeacherTaskType.REWARD_COLLECTION) {
            Date now = new Date();
            String monthFormat = "yyyyMM";

            // 小学老师50园丁豆，对应中学老师500学豆
            String unit = "园丁豆";
            if (teacher.isJuniorTeacher())
                unit = "学豆";

            String month;
            RewardLogistics logistics;
            int start = 1;
            int monthRange = 2;// 看两个月的

            for (Date time = now; start <= monthRange; time = DateUtils.addMonths(time, -1), start = start + 1) {
                month = DateUtils.dateToString(time, monthFormat);
                logistics = crmRewardService.$findRewardLogistics(teacherId, RewardLogistics.Type.STUDENT, month);
                if (logistics == null || logistics.getDisabled())
                    continue;

                //计算奖励领取的截止时间
                Date receiveEndTime = DateUtils.addDays(logistics.getCreateDatetime(), 30);
                if (receiveEndTime.before(now))
                    break;

                DateRange validRange = new DateRange(logistics.getCreateDatetime(), receiveEndTime);
                // 不能重复领取
                boolean received = loadTeacherTaskRewardHistory(teacherId, TeacherTaskType.REWARD_COLLECTION)
                        .stream()
                        .anyMatch(r -> validRange.contains(r.getCreateDatetime()));

                if (received)
                    continue;

                IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.REWARD_COLLECTION_REWARD, 500);
                integralHistory.setComment("奖品代收货老师" + unit + "奖励");
                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    return MapMessage.errorMessage("领取失败!");
                }

                TeacherTaskRewardHistory history = new TeacherTaskRewardHistory();
                history.setTeacherId(teacherId);
                history.setRewardName("奖品中心老师代收学生包裹奖励");
                history.setTaskType(type.name());
                teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                        .insertTeacherTaskRewardHistory(history)
                        .awaitUninterruptibly();
            }

        }

        return MapMessage.successMessage("领取成功!");
    }

    // ========================================
    // Tiny Group Relevant
    // ========================================

    @Override
    public List<TermBeginStudentAppRecord> loadTermBeginListByTeacherId(Long teacherId) {
        return termBeginStudentAppRecordPersistence.findByTeacherId(teacherId);
    }

    @Override
    public Map<String, Long> getFinishCount(Teacher teacher) {
        return teacherCertificationService.getFinishCount(teacher);
    }

    @Override
    public MapMessage setAmbassador(TeacherDetail teacher) {
        return deprecatedAmbassadorService.setAmbassador(teacher);
    }

    @Override
    public MapMessage applySchoolAmbassador(SchoolAmbassador schoolAmbassador) {
        return deprecatedAmbassadorService.applySchoolAmbassador(schoolAmbassador);
    }

    @Override
    public Map<String, Object> loadAmbassadorSHXInfo(TeacherDetail teacher) {
        return deprecatedAmbassadorService.loadAmbassadorSHXInfo(teacher);
    }

    @Override
    public Map<String, Object> loadAmbassadorZSInfo(TeacherDetail teacher) {
        return deprecatedAmbassadorService.loadAmbassadorZSInfo(teacher);
    }

    @Override
    public MapMessage remindTeacherForEffectHw(TeacherDetail teacherDetail) {
        return deprecatedAmbassadorService.remindTeacherForEffectHw(teacherDetail);
    }

    @Override
    public MapMessage praiseTeacherForEffectHw(TeacherDetail teacherDetail) {
        return deprecatedAmbassadorService.praiseTeacherForEffectHw(teacherDetail);
    }

    @Override
    public MapMessage resignationAmbassador(TeacherDetail detail) {
        return deprecatedAmbassadorService.resignationAmbassador(detail);
    }

    @Override
    public List<Map<String, Object>> loadAmbassadorScoreHistory(Long ambassadorId) {
        return deprecatedAmbassadorService.loadAmbassadorScoreHistory(ambassadorId);
    }

    @Override
    public List<Map<String, Object>> getContributionRank(Teacher teacher) {
        if (teacher == null || teacher.getSubject() == null) return Collections.emptyList();
        List<Integer> available = Arrays.asList(每个学生完成作业老师获得积分.getType(), 每个学生完成测验老师获得积分.getType());

        List<GroupMapper> groups = groupLoaderClient.loadTeacherGroupsByTeacherId(teacher.getId(), false);
        if (CollectionUtils.isEmpty(groups)) return Collections.emptyList();
        Set<Long> clazzIds = groups.stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        Map<Long, List<GroupStudentTuple>> refs = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(groupIds)
                .stream()
                .collect(Collectors.groupingBy(GroupStudentTuple::getGroupId));
        Map<Long, List<IntegralHistory>> histories = integralHistoryLoaderClient.getIntegralHistoryLoader()
                .loadUserIntegralHistories(teacher.getId())
                .stream()
                .filter(h -> available.contains(h.getIntegralType()) && groupIds.contains(h.getRelationClassId())
                        && WeekRange.current().contains(h.getCreatetime()))
                .collect(Collectors.groupingBy(IntegralHistory::getRelationClassId, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMapper group : groups) {
            Map<String, Object> map = new HashMap<>();
            Clazz clazz = clazzs.get(group.getClazzId());
            map.put("clazzId", group.getClazzId());
            map.put("clazzName", clazz == null ? "" : clazz.formalizeClazzName());
            map.put("groupId", group.getId());
            map.put("hwc", 0);
            map.put("cc", 0);
            if (CollectionUtils.isNotEmpty(refs.get(group.getId()))) {
                Set<Long> sids = refs.getOrDefault(group.getId(), Collections.emptyList())
                        .stream()
                        .map(GroupStudentTuple::getStudentId)
                        .collect(Collectors.toSet());
                String date = DateUtils.dateToString(WeekRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATE);
                long hwc = asyncUserCacheServiceClient.getAsyncUserCacheService()
                        .WeekFinishHAQCountCacheManager_current(sids, teacher.getSubject(), date)
                        .getUninterruptibly()
                        .values()
                        .stream()
                        .filter(CollectionUtils::isNotEmpty)
                        .count();
                map.put("hwc", hwc);
            }
            if (CollectionUtils.isNotEmpty(histories.get(group.getId()))) {
                map.put("cc", histories.get(group.getId()).stream().mapToInt(IntegralHistory::getIntegral).sum() / 10);
            }
            result.add(map);
        }
        Collections.sort(result, ((o1, o2) -> {
            int i1 = SafeConverter.toInt(o1.get("cc"));
            int i2 = SafeConverter.toInt(o2.get("cc"));
            return Integer.compare(i2, i1);
        }));

        return result;
    }

    @Override
    public Map<String, Object> calculateHomeworkMaxIntegralCount(TeacherDetail teacher, Collection<Long> clazzIds) {
        if (teacher == null || CollectionUtils.isEmpty(clazzIds) || teacher.getUserIntegral() == null)
            return Collections.emptyMap();

        Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        if (groups.size() != clazzIds.size()) return Collections.emptyMap();
        Set<Long> groupIds = groups.values().stream().map(GroupMapper::getId).collect(Collectors.toSet());

        int min = Integer.MAX_VALUE;
        Map<Long, SmartClazzIntegralPool> pools = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralPools(groupIds)
                .getUninterruptibly();

        for (Long groupId : groupIds) {
            SmartClazzIntegralPool pool = pools.get(groupId);
            if (pool == null) return Collections.emptyMap(); // 班级学豆池有问题，导致计算最大可用学豆数量不准确，不算了，返回空
            if (pool.fetchTotalIntegral() < min) min = pool.fetchTotalIntegral();
        }
        // 如果min是还是最大值。。。我擦，那一定是搞错了。。。
        if (min == Integer.MAX_VALUE) return Collections.emptyMap();

        // 最大可用学豆数 = 教师可用园丁豆数量 / 传入的班级数量 * 5 + min
        int max = new BigDecimal(teacher.getUserIntegral().getUsable())
                .divide(new BigDecimal(clazzIds.size()), 0, BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal(5)).intValue() + min;

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> overTimeGids = fetchOverTimeGids(groups, groupIds, clazzIds);
        result.put("mc", max);
        result.put("dc", Math.min(max, 20));
        result.put("limitTime", NewHomeworkConstants.LIMIT_HOMEWORK_TIME);
        result.put("overTimeGids", overTimeGids);
        return result;
    }

    //#redmine 64390
    private List<Map<String, Object>> fetchOverTimeGids(Map<Long, GroupMapper> groups, Set<Long> groupIds, Collection<Long> clazzIds) {

        //周五不处理
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            return Collections.emptyList();
        }
        Map<Long, Set<Long>> sharedGroupIds = groupLoaderClient.loadSharedGroupIds(groupIds);
        Set<Long> gids = sharedGroupIds.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Map<Long, String> gidToHidMap = newHomeworkCacheServiceClient.getNewHomeworkCacheService().fetchHomeworkIdByGroupIdFromCache(gids);
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoaderClient.loads(gidToHidMap.values());
        List<Map<String, Object>> overTimeGids = new LinkedList<>();
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Map<Long, GroupMapper> mapperMap = groups.values().stream().collect(Collectors.toMap(GroupMapper::getId, Function.identity()));
        for (Long groupId : groupIds) {
            if (!sharedGroupIds.containsKey(groupId))
                continue;
            Set<Long> _gids = sharedGroupIds.get(groupId);
            if (CollectionUtils.isEmpty(_gids))
                continue;
            long time = 0;//秒
            for (Long gid : _gids) {
                if (!gidToHidMap.containsKey(gid))
                    continue;
                String hid = gidToHidMap.get(gid);
                if (!newHomeworkMap.containsKey(hid))
                    continue;
                NewHomework newHomework = newHomeworkMap.get(hid);
                if (newHomework.isDisabledTrue())
                    continue;
                if (newHomework.isHomeworkTerminated())
                    continue;
                time += SafeConverter.toLong(newHomework.getDuration());
            }

            if (!mapperMap.containsKey(groupId))
                continue;
            Long clazzId = mapperMap.get(groupId).getClazzId();
            if (clazzId == null)
                continue;
            Map<String, Object> m = MapUtils.m("groupId", groupId,
                    "clazzId", clazzId,
                    "duration", time,
                    "clazzName", clazzMap.containsKey(clazzId) ? clazzMap.get(clazzId).formalizeClazzName() : "");
            overTimeGids.add(m);
        }
        return overTimeGids;
    }

    @Override
    public MapMessage reportTeacher(TeacherDetail teacher, Long teacherId, String teacherName, String reason, AmbassadorReportType type) {
        return deprecatedAmbassadorService.reportTeacher(teacher, teacherId, teacherName, reason, type);
    }

    @Override
    public Map<String, Object> loadTeacherLuckyBagInfo(Long groupId, Long teacherId) {
        if (groupId == null || teacherId == null) {
            return Collections.emptyMap();
        }
        // 获取学生
        List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(groupId);
        if (CollectionUtils.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);
        // 获取同学手上持有过福袋情况
        Map<Long, StudentLuckyBagRecord> receiveBags = studentLuckyBagRecordPersistence.loadByReceiverIds(studentIds);
        // 获取同学发出去的情况
        Map<Long, List<StudentLuckyBagRecord>> sendBags = studentLuckyBagRecordPersistence.loadBySenderIds(studentIds);
        // 获取未获得福袋的学生
        List<User> noBagUserList = userMap.values().stream().filter(u -> receiveBags.get(u.getId()) == null).collect(Collectors.toList());
        // 领奖记录
        Map<Long, Long> receiveMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCounts(UserBehaviorType.LUCKY_BAG_RECEIVE_REWARD, studentIds)
                .getUninterruptibly();
        // 已经领奖的人数
        long count = receiveMap.values().stream().filter(l -> l != null && l > 0).count();

        // 已经领奖的学生
        List<Map<String, Object>> successList = new ArrayList<>();
        // 持有福袋没有发送的学生
        List<Map<String, Object>> holdList = new ArrayList<>();
        // 没有获得福袋的学生
        List<Map<String, Object>> waitList = new ArrayList<>();

        // 没有可以传递的同学了
        if (CollectionUtils.isEmpty(noBagUserList)) {
            // 发出去的并且全部被打开了并且没有领取的学生
            Set<Long> s_list = sendBags.entrySet().stream()
                    .filter(e -> CollectionUtils.isNotEmpty(e.getValue()) &&
                            e.getValue().stream().filter(s -> s.getStatus() != LuckyBagStatus.OPEN).count() == 0)
                    .filter(e -> receiveMap.get(e.getKey()) == null || receiveMap.get(e.getKey()) == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            // 收到的并且没人可传了并且已经帮助传给我的人打开了并且没有领取的学生
            Set<Long> s_list_1 = receiveBags.values().stream()
                    .filter(r -> CollectionUtils.isEmpty(sendBags.get(r.getReceiverId())))
                    .filter(r -> r.getStatus() == LuckyBagStatus.OPEN)
                    .filter(r -> receiveMap.get(r.getReceiverId()) == null || receiveMap.get(r.getReceiverId()) == 0)
                    .map(StudentLuckyBagRecord::getReceiverId)
                    .collect(Collectors.toSet());
            for (Long sid : s_list) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", userMap.get(sid).fetchRealname());
                map.put("img", userMap.get(sid).fetchImageUrl());
                successList.add(map);
            }
            for (Long sid : s_list_1) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", userMap.get(sid).fetchRealname());
                map.put("img", userMap.get(sid).fetchImageUrl());
                successList.add(map);
            }
        } else {
            // 还有未被传递的同学
            for (User u : noBagUserList) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", u.fetchRealname());
                map.put("img", u.fetchImageUrl());
                waitList.add(map);
            }

            List<StudentLuckyBagRecord> h_list = receiveBags.values().stream()
                    .filter(r -> CollectionUtils.isEmpty(sendBags.get(r.getReceiverId())))
                    .collect(Collectors.toList());
            for (StudentLuckyBagRecord r : h_list) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", userMap.get(r.getReceiverId()).fetchRealname());
                map.put("img", userMap.get(r.getReceiverId()).fetchImageUrl());
                holdList.add(map);
            }

            List<Long> s_list = sendBags.entrySet().stream()
                    .filter(e -> CollectionUtils.isNotEmpty(e.getValue()) &&
                            e.getValue().stream().filter(s -> s.getStatus() != LuckyBagStatus.OPEN).count() == 0)
                    .filter(e -> receiveMap.get(e.getKey()) == null || receiveMap.get(e.getKey()) == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            for (Long uid : s_list) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", userMap.get(uid).fetchRealname());
                map.put("img", userMap.get(uid).fetchImageUrl());
                successList.add(map);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("successList", successList);
        data.put("holdList", holdList);
        data.put("waitList", waitList);
        // 获取领奖的进度
        int progress = studentIds.size() == 0 ? 0 : new BigDecimal(count).divide(new BigDecimal(studentIds.size()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
        data.put("totalCount", progress);
        // 获取班级是否全部领取了奖励
        if (count == studentIds.size()) {
            data.put("clazzFlag", true);
        }
        return data;
    }

    @Override
    public MapMessage signIn(Long teacherId) {
        Map<String, Object> msgBody = new HashMap<>();
        msgBody.put("messageType", "signin");
        msgBody.put("teacherId", teacherId);
        messagePublisherSignIn.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgBody)));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage shareArticle(Long teacherId) {
        Map<String, Object> msgBody = new HashMap<>();
        msgBody.put("messageType", "teacherShareArticle");
        msgBody.put("teacherId", teacherId);
        messagePublisherShareArticle.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgBody)));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage receiveLuckyBagClazzReward(Long groupId, Long teacherId) {
        if (groupId == null) {
            return MapMessage.errorMessage();
        }
        // 添加班级学豆
        ClazzIntegralHistory history = new ClazzIntegralHistory();
        history.setGroupId(groupId);
        history.setClazzIntegralType(ClazzIntegralType.开学福袋老师领取班级学豆.getType());
        history.setIntegral(100);
        history.setComment(ClazzIntegralType.开学福袋老师领取班级学豆.getDescription());
        history.setAddIntegralUserId(teacherId);
        MapMessage msg = clazzIntegralServiceClient.getClazzIntegralService()
                .changeClazzIntegral(history)
                .getUninterruptibly();
        if (msg.isSuccess()) {
            // 记录
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_incUserBehaviorCount(UserBehaviorType.LUCKY_BAG_CLAZZ_REWARD_COUNT, groupId, 1L, LuckyBagActivity.getActivityEndDate())
                    .awaitUninterruptibly();
            return MapMessage.successMessage("领取成功");
        } else {
            return MapMessage.errorMessage("领取失败");
        }
    }
}
