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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.business.api.MiscService;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.ActivityStatusType;
import com.voxlearning.utopia.entity.activity.InterestingReport;
import com.voxlearning.utopia.entity.activity.NewYearWish;
import com.voxlearning.utopia.entity.campaign.BiZhong;
import com.voxlearning.utopia.entity.misc.IntegralActivity;
import com.voxlearning.utopia.entity.misc.IntegralActivityRule;
import com.voxlearning.utopia.entity.misc.UgcAnswers;
import com.voxlearning.utopia.entity.questionsurvey.QuestionSurveyResult;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.business.api.entity.ActivityData;
import com.voxlearning.utopia.service.business.impl.dao.BiZhongPersistence;
import com.voxlearning.utopia.service.business.impl.dao.NewYearWishDao;
import com.voxlearning.utopia.service.business.impl.loader.MiscLoaderImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.business.impl.support.ProductCardService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignAward;
import com.voxlearning.utopia.service.campaign.client.CampaignLoaderClient;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.finance.client.IntegralActivityRuleServiceClient;
import com.voxlearning.utopia.service.finance.client.IntegralActivityServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkPartLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.question.client.QuestionSurveyServiceClient;
import com.voxlearning.utopia.service.ugc.client.UgcServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.user.api.constants.InvitationType.*;

@Named
@Service(interfaceClass = MiscService.class)
@ExposeService(interfaceClass = MiscService.class)
public class MiscServiceImpl extends BusinessServiceSpringBean implements MiscService {

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private BiZhongPersistence biZhongPersistence;
    @Inject private CampaignLoaderClient campaignLoaderClient;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private IntegralActivityRuleServiceClient integralActivityRuleServiceClient;
    @Inject private IntegralActivityServiceClient integralActivityServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private MiscLoaderImpl miscLoaderImpl;
    @Inject private NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject private NewYearWishDao newYearWishDao;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeServiceClient privilegeServiceClient;
    @Inject private ProductCardService productCardService;
    @Inject private QuestionSurveyServiceClient questionSurveyServiceClient;
    @Inject private UgcServiceClient ugcServiceClient;

    // 激活口袋学社
    @Override
    public MapMessage activeStudyCraftCard(Long cardKey, Long userId) {
        return productCardService.activateStudyCraftCard(cardKey, userId);
    }

    @Override
    public Boolean termBeginHasAdjustClazz(Long teacherId) {
        return asyncBusinessCacheService.TeacherAdjustClazzRemindCacheManager_done(teacherId)
                .getUninterruptibly();
    }

    @Override
    public void termBeginRecordAdjustClazz(Long teacherId) {
        asyncBusinessCacheService.TeacherAdjustClazzRemindCacheManager_record(teacherId)
                .awaitUninterruptibly();
    }

    @Override
    public void addActivityData(ActivityData activityData) {
        activityDataPersistence.insert(activityData);
    }

    @Override
    public void reCover(Long activityId) {
        if (activityId != null) {
            activityDataPersistence.deleteByActivityId(activityId);
        }
    }

    @Override
    public void bindInvitedTeacherMobile(final Long userId) {
        AlpsThreadPool.getInstance().submit(() -> {
            InviteHistory inviteHistory = asyncInvitationServiceClient.loadByInvitee(userId)
                    .enabled()
                    .findFirst();
            if (inviteHistory == null) return;

            if (inviteHistory.getInvitationType() == TEACHER_INVITE_TEACHER_SMS || inviteHistory.getInvitationType() == TEACHER_INVITE_TEACHER_SMS_BY_WECHAT
                    || inviteHistory.getInvitationType() == TEACHER_INVITE_TEACHER_SMS_BY_APP) {
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(userId);
                if ((ua == null || !ua.isMobileAuthenticated()) && StringUtils.isNotEmpty(inviteHistory.getInviteSensitiveMobile())) {
                    userServiceClient.activateUserMobile(inviteHistory.getInviteeUserId(), inviteHistory.getInviteSensitiveMobile(), true);
                }
            }

            Teacher invitee = teacherLoaderClient.loadTeacher(userId);
            Teacher inviter = teacherLoaderClient.loadTeacher(inviteHistory.getUserId());
            if (invitee == null || inviter == null) return;
            if (!invitee.isPrimarySchool() || !inviter.isPrimarySchool()) return;

            // FIXME 邀请活动没有了。。。
//            if (invitee.getLoginCount() <= 1) {
//                //第一次登陆就发个微信消息给邀请者,应在每年的1、2、7、8月自动下线，停止发送消息
//                List<Integer> months = Arrays.asList(1, 2, 7, 8);
//                if (!months.contains(MonthRange.current().getMonth())) {
//                    Map<String, Object> extension = new HashMap<>();
//                    extension.put("teacherId", inviteHistory.getInviteeUserId());
//                    extension.put("createDate", new Date());
//                    extension.put("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(inviteHistory.getInviteeUserId()));
//                    wechatServiceClient.processWechatNotice(
//                            WechatNoticeProcessorType.TeacherAcceptInviteNotice,
//                            inviteHistory.getUserId(),
//                            extension,
//                            WechatType.TEACHER);
//                }
//            }
        });
    }

    @Override
    public MapMessage saveTeacherSourceCampaignAward(Long userId, String source, int awardStatus) {
        // 判断用户是否已经参加过此次活动
        CampaignType campaignType = CampaignType.TEACHER_SOURCE_COLLECTION;
        List<CampaignAward> userAwards = campaignLoaderClient.getCampaignLoader().findCampaignAwards(campaignType.getId(), userId);
        if (CollectionUtils.isNotEmpty(userAwards)) {
            return MapMessage.errorMessage("你已经参加过此次活动!");
        }
        CampaignAward ca = new CampaignAward();
        ca.setAward("");
        ca.setAwardStatus(awardStatus);
        ca.setUserId(userId);
        ca.setExtInfo(source);
        ca.setCampaignId(campaignType.getId());
        campaignServiceClient.getCampaignService().$insertCampaignAward(ca);
        if (awardStatus == 1) {
            //加金币 10个
            IntegralHistory integralHistory = new IntegralHistory(userId, IntegralType.收集老师来源奖励金币_产品平台, 100);
            integralHistory.setComment("收集老师来源奖励园丁豆");
            userIntegralService.changeIntegral(integralHistory);
        }
        return MapMessage.successMessage("谢谢参与");
    }

    // ========================================================================
    // 积分活动相关的方法 By Wyc 2016-01-18
    // ========================================================================

    @Override
    @Deprecated
    public Long addIntegralActivity(IntegralActivity activity) {
        if (activity != null) {
            activity.setStatus(1);
            activity = integralActivityServiceClient.getIntegralActivityService()
                    .insertIntegralActivity(activity)
                    .getUninterruptibly();
            return activity == null ? 0L : activity.getId();
        }
        return 0L;
    }

    @Override
    @Deprecated
    public void updateIntegralActivityStatus(Long activityId, Integer status) {
        if (activityId == null) {
            logger.error("积分活动ID异常！");
            return;
        }
        if (ActivityStatusType.of(status) == ActivityStatusType.EXCEPTION) {
            logger.error("积分活动状态异常！activityId: {}, status: {}", activityId, status);
            return;
        }
        Date current = new Date();
        IntegralActivity activity = integralActivityServiceClient.getIntegralActivityService()
                .loadIntegralActivityFromDB(activityId)
                .getUninterruptibly();
        if (activity == null) {
            // avoid NPE
            logger.error("积分活动ID不存在！activityId={}", activityId);
            return;
        }
        ActivityStatusType actStatus = ActivityStatusType.of(status);
        // 发布的时候如果是[长期/当前时间过了活动开始时间], 状态改为进行中
        if (actStatus == ActivityStatusType.READY) {
            if (activity.getStartDate() == null || current.after(activity.getStartDate())) {
                status = ActivityStatusType.ONGOING.getType();
                activity.setStartDate(current);
            }
        }
        if (actStatus == ActivityStatusType.ONGOING) {
            activity.setStartDate(current);
        } else if (actStatus == ActivityStatusType.FINISHED) {
            activity.setEndDate(current);
        }
        activity.setStatus(status);
        activity.setId(activityId);
        integralActivityServiceClient.getIntegralActivityService()
                .updateIntegralActivity(activity)
                .awaitUninterruptibly();
    }

    @Override
    @Deprecated
    public void updateIntegralActivity(Long activityId, IntegralActivity activity) {
        if (activity != null && activityId != 0L) {
            activity.setId(activityId);
            integralActivityServiceClient.getIntegralActivityService()
                    .updateIntegralActivity(activity)
                    .awaitUninterruptibly();
        } else {
            logger.error("积分活动更新失败。activityId: {}", activityId);
        }

    }

    @Override
    @Deprecated
    public Long addIntegralActivityRule(IntegralActivityRule rule, int department) {
        if (rule != null) {
            int integralType = getIntegralTypeByDepartment(department);
            rule.setIntegralType(integralType);
            rule = integralActivityRuleServiceClient.getIntegralActivityRuleService()
                    .insertIntegralActivityRule(rule)
                    .getUninterruptibly();
            if (rule != null) {
                // trigger buffer reloaded
                integralActivityRuleServiceClient.getIntegralActivityRuleService().reloadIntegralActivityRuleBuffer();
            }
            return rule == null ? 0L : rule.getId();
        }
        return 0L;
    }

    @Override
    @Deprecated
    public void updateIntegralActivityRule(IntegralActivityRule rule) {
        if (rule == null || rule.getId() == null) {
            return;
        }
        rule = integralActivityRuleServiceClient.getIntegralActivityRuleService()
                .updateIntegralActivityRule(rule)
                .getUninterruptibly();
        if (rule != null) {
            // trigger buffer reloaded
            integralActivityRuleServiceClient.getIntegralActivityRuleService().reloadIntegralActivityRuleBuffer();
        }
    }

    @Override
    @Deprecated
    public void disableIntegralActivityRule(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        IntegralActivityRule rule = new IntegralActivityRule();
        rule.setId(ruleId);
        rule.setDisabled(true);
        rule = integralActivityRuleServiceClient.getIntegralActivityRuleService()
                .updateIntegralActivityRule(rule)
                .getUninterruptibly();
        if (rule != null) {
            // trigger buffer reloaded
            integralActivityRuleServiceClient.getIntegralActivityRuleService().reloadIntegralActivityRuleBuffer();
        }
    }

    // 取出所有使用过的积分，然后根据部门filter， 找出最大一位
    private Integer getIntegralTypeByDepartment(int department) {

        Optional<Integer> maxCreditType = integralActivityRuleServiceClient.getIntegralActivityRuleBuffer()
                .loadAllEnabled()
                .stream()
                .filter(e -> !Objects.equals(e.getId(), 0L))
                .map(IntegralActivityRule::getIntegralType)
                .filter(t -> t / 10000 == department).max(Integer::compareTo);
        if (maxCreditType.isPresent()) {
            return maxCreditType.get() + 1;
        } else {
            return (department * 10000) + 1;
        }
    }

    @Override
    public MapMessage saveUgcAnswer(User user, Long recordId, List<Map<String, Object>> answerMapList, UgcAnswers.Source source) {
        if (user == null || recordId == null || CollectionUtils.isEmpty(answerMapList)) {
            return MapMessage.errorMessage("参数错误");
        }
        // 进行结果保存
        List<UgcAnswers> answers = new ArrayList<>();
        // 获取用户答题结果历史
        List<UgcAnswers> answersList = ugcServiceClient.getUgcService()
                .findUgcAnswersByRecordIdAndUserId(recordId, user.getId())
                .getUninterruptibly();
        List<Long> questionIds = answersList.stream().map(UgcAnswers::getQuestionId).collect(Collectors.toList());
        for (Map<String, Object> map : answerMapList) {
            Long questionId = SafeConverter.toLong(map.get("questionId"));
            String answer = SafeConverter.toString(map.get("answer"));
            if (questionIds.contains(questionId)) {
                continue;
            }
            UgcAnswers as = new UgcAnswers();
            as.setRecordId(recordId);
            as.setQuestionId(questionId);
            as.setUserId(user.getId());
            if (answer.length() > 100) {
                answer = StringUtils.substring(answer, 0, 100);
            }
            as.setAnswer(StringUtils.filterEmojiForMysql(answer));
            as.setSource(source.name());
            answers.add(as);
        }
        if (CollectionUtils.isNotEmpty(answers)) {
            List<AlpsFuture<UgcAnswers>> futureList = new ArrayList<>();
            for (UgcAnswers answer : answers) {
                AlpsFuture<UgcAnswers> future = ugcServiceClient.getUgcService().persistUgcAnswers(answer);
                futureList.add(future);
            }
            futureList.forEach(AlpsFuture::awaitUninterruptibly);
        }
        return MapMessage.successMessage();
    }

    @Override
    @Deprecated
    public String saveQuestionSurveyResult(QuestionSurveyResult questionSurvey) {
        if (questionSurvey == null) {
            return null;
        }
        questionSurvey = questionSurveyServiceClient.getQuestionSurveyService()
                .insertQuestionSurveyResult(questionSurvey)
                .getUninterruptibly();
        return questionSurvey == null ? null : questionSurvey.getId();
    }

    @Override
    @Deprecated
    public List<QuestionSurveyResult> loadQuestionSurveyResult(String activityId) {
        if (activityId == null) {
            return Collections.emptyList();
        }
        return questionSurveyServiceClient.getQuestionSurveyService()
                .findQuestionSurveyResults(activityId)
                .getUninterruptibly();
    }

    @Override
    public MapMessage sendFakeAppealMessage(Long teacherId) {
        try {
            // 发送申诉消息
            String pattern = "系统检测到您的账号使用存在异常，为保护您的权益，已将您的账号暂时冻结，部分功能使用将会受到限制。{0}";
            String comment = MessageFormat.format(pattern, "<a href='javascript:;' class='js-clickWarningCheating' data-type='FAKE' class='w-blue'>【点击申请解冻账号】</a>");
            // 系统消息
            messageCommandServiceClient.getMessageCommandService().sendUserMessage(teacherId, comment);
            // 微信模板消息
            comment = MessageFormat.format(pattern, "点击申请解冻账号");
            Map<Long, List<UserWechatRef>> refMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(teacherId), WechatType.TEACHER);
            if (refMap != null && CollectionUtils.isNotEmpty(refMap.get(teacherId))) {
                for (UserWechatRef ref : refMap.get(teacherId))
                    sendWechatNotice(teacherId, comment, ref.getOpenId(), true);
            }
            // app小铃铛消息
            AppMessage message = new AppMessage();
            message.setUserId(teacherId);
            message.setMessageType(TeacherMessageType.ACTIVIY.getType());
            message.setTitle("系统通知");
            message.setContent(comment);
            message.setImageUrl("");
            message.setLinkUrl("/ucenter/appeal.vpage?type=FAKE"); // 这里写相对地址
            message.setLinkType(1);
            message.setIsTop(false);
            message.setTopEndTime(0L);
            message.setExtInfo(new HashMap<>());
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("send appeal message error! tid is {}", teacherId, ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage sendFakeNoticeMessage(Long teacherId, Collection<Long> receivers) {
        if (teacherId == null) {
            return MapMessage.errorMessage("无效的老师ID");
        }
        if (CollectionUtils.isEmpty(receivers)) {
            return MapMessage.successMessage();
        }

        User fakeTeacher = userLoaderClient.loadUser(teacherId);
        String userName = fakeTeacher == null ? String.valueOf(teacherId) : fakeTeacher.fetchRealnameIfBlankId();

        receivers = CollectionUtils.toLinkedHashSet(receivers);
        String content = StringUtils.formatMessage("{}老师账号使用存在异常，和您相关的换班申请已经被取消", userName);

        AppMessage message = new AppMessage();
        message.setMessageType(TeacherMessageType.APPLICATION.getType());
        message.setTitle("系统通知");
        message.setContent(content);
        message.setImageUrl("");
        message.setLinkType(1);
        message.setIsTop(false);
        message.setTopEndTime(0L);
        message.setExtInfo(new HashMap<>());
        try {
            receivers.forEach(receiver -> {
                // 系统消息
                messageCommandServiceClient.getMessageCommandService().sendUserMessage(receiver, content);

                // 微信模板消息
                Map<Long, List<UserWechatRef>> refMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(receiver), WechatType.TEACHER);
                if (refMap != null && CollectionUtils.isNotEmpty(refMap.get(receiver))) {
                    for (UserWechatRef ref : refMap.get(receiver)) {
                        sendWechatNotice(receiver, content, ref.getOpenId(), false);
                    }
                }

                // APP消息
                message.setUserId(receiver);
                messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            });
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("send fake notice message error! tid is {}", teacherId, ex);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage loadLuckyMan() {
        List<BiZhong> allList = biZhongPersistence.findAll();
        if (CollectionUtils.isEmpty(allList)) {
            return MapMessage.errorMessage("没人儿了");
        }
        // 随机取吧。
        Random random = new Random();
        List<BiZhong> lotteryList = new ArrayList<>();
        while (true) {
            int luckyNo = random.nextInt(2200);
            BiZhong pass = lotteryList.stream().filter(b -> b.getWorkNo().intValue() == luckyNo)
                    .findFirst().orElse(null);
            if (pass != null) {
                continue;
            }
            BiZhong luckyMan = allList.stream().filter(b -> b.getWorkNo().intValue() == luckyNo)
                    .findFirst().orElse(null);
            if (luckyMan != null) {
                lotteryList.add(luckyMan);
            }
            if (lotteryList.size() >= 10) {
                break;
            }
        }
        return MapMessage.successMessage().add("luckyMans", lotteryList);
    }

    @Override
    public MapMessage bingo(Long workNo) {
        int rows = biZhongPersistence.disabledByWorkNo(workNo);
        return rows > 0 ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    @Override
    public MapMessage recordNewYearWish(User user, String wishContent) {
        if (user == null || StringUtils.isBlank(wishContent)) {
            return MapMessage.successMessage();
        }
        NewYearWish wish = new NewYearWish();
        wish.setId(user.getId());
        wish.setWishContent(wishContent);
        wish.setUpdateAt(new Date());
        newYearWishDao.upsert(wish);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage getInterestingReportAward(User user) {
        if (user == null) {
            return MapMessage.errorMessage();
        }
        if (user.fetchUserType() == UserType.TEACHER) {
            Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
            // 发奖
            IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.INTERESTING_REPORT_ACTIVITY, 170);
            if (teacher.isPrimarySchool()) {
                integralHistory.setComment("在2017新年礼包中，获得17园丁豆");
            } else {
                integralHistory.setComment("在2017新年礼包中，获得170学豆");
            }
            if (userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                // 记录领取过
                asyncBusinessCacheService.InterestingReportCacheManager_record(user.getId()).awaitUninterruptibly();
                return MapMessage.successMessage();
            } else {
                logger.error("老师领取年度趣味报告礼包失败！userId：{},integral:{}", teacher.getId(), 170);
                return MapMessage.errorMessage("领取失败！");
            }
        } else if (user.fetchUserType() == UserType.STUDENT) {
            Privilege newYearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(Privilege.SpecialPrivileges.新年专属头饰.getCode());
            // 发送头饰
            privilegeServiceClient.getPrivilegeService().grantPrivilege(user.getId(), newYearPrivilege);
            // 发送勋章
            InterestingReport report = miscLoaderImpl.loadUserInterestingReport(user.getId());
            if (report != null) {
                Privilege rewardPrivilege;
                if (Objects.equals(report.getAchievementName(), Privilege.SpecialPrivileges.学习精英.name())) {
                    rewardPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(Privilege.SpecialPrivileges.学习精英.getCode());
                    privilegeServiceClient.getPrivilegeService().grantPrivilege(user.getId(), rewardPrivilege);
                } else if (Objects.equals(report.getAchievementName(), Privilege.SpecialPrivileges.学习达人.name())) {
                    rewardPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(Privilege.SpecialPrivileges.学习达人.getCode());
                    privilegeServiceClient.getPrivilegeService().grantPrivilege(user.getId(), rewardPrivilege);
                } else if (Objects.equals(report.getAchievementName(), Privilege.SpecialPrivileges.潜力之星.name())) {
                    rewardPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(Privilege.SpecialPrivileges.潜力之星.getCode());
                    privilegeServiceClient.getPrivilegeService().grantPrivilege(user.getId(), rewardPrivilege);
                }
            }
            // 记录领取过
            asyncBusinessCacheService.InterestingReportCacheManager_record(user.getId()).awaitUninterruptibly();
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("不支持的用户类型");
        }
    }

    @Override
    public Boolean showScholarshipEnter(Teacher teacher) {
        if (!NewSchoolYearActivity.isInScholarshipPeriod()) {
            return false;
        }
        if (teacher.getSubject() == null || teacher.getSubject() == Subject.CHINESE) {
            return false;
        }
        // 数学老师直接显示
        if (teacher.getSubject() == Subject.MATH) {
            return true;
        }
        // 获取老师班组的教材
        MapMessage message = newHomeworkPartLoaderClient.getTeacherHomeworkProgress(teacher.getId(), teacher.getSubject());
        if (!message.isSuccess()) {
            return false;
        }
        Map<String, Map<String, Object>> groupBookMap = (Map<String, Map<String, Object>>) message.get("homeworkProgress");
        if (groupBookMap == null) {
            return false;
        }
        // 获取开放的教材版本列表
        String openBookIds = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "SCHOLARSHIP_OPEN_BOOK_ID");
        if (StringUtils.isBlank(openBookIds)) {
            return false;
        }
        // 开放的教材ID
        List<String> openIdList = Arrays.asList(openBookIds.split(","));
        // 获取老师的教材
        Set<String> teacherBookIds = new HashSet<>();
        for (Map<String, Object> m : groupBookMap.values()) {
            String bookId = SafeConverter.toString(m.get("bookId"));
            teacherBookIds.add(bookId);
        }
        Map<String, NewBookProfile> newBookProfileMap = newContentLoaderClient.loadBookProfilesIncludeDisabled(teacherBookIds);
        boolean show = false;
        for (NewBookProfile newBookProfile : newBookProfileMap.values()) {
            if (openIdList.contains(newBookProfile.getSeriesId())) {
                show = true;
                break;
            }
        }
        if (show) {
            return true;
        } else {
            return false;
        }
    }

    private void sendWechatNotice(Long teacherId, String content, String openId, Boolean hasUrl) {
        Date date = new Date();
        WechatNotice notice = new WechatNotice();
        notice.setMessageType(WechatNoticeType.TEACHER_FAKE_NOTICE.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(date);
        notice.setExpireTime(DateUtils.calculateDateDay(date, 1));
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("content", content);
        if (hasUrl) {
            contentMap.put("url", contentMap.put("url", ProductConfig.getMainSiteBaseUrl() + "/ucenter/appeal.vpage?type=FAKE"));
        } else {
            contentMap.put("url", "");
        }
        notice.setMessage(JsonUtils.toJson(contentMap));
        notice.setUserId(teacherId);
        notice.setOpenId(openId);
        wechatServiceClient.persistWechatNotice(notice);
    }
}
