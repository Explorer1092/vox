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

package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DateRangeUnit;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.AfentiSocialService;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UseAppStatus;
import com.voxlearning.utopia.service.afenti.api.context.LearningRankContext;
import com.voxlearning.utopia.service.afenti.api.context.LoadInvitationMsgContext;
import com.voxlearning.utopia.service.afenti.api.context.PopupTextContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiInvitationRecord;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiInvitationRecordPersistence;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiAchievementService;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiPopupMessageService;
import com.voxlearning.utopia.service.afenti.impl.service.processor.invitation.LoadInvitationMsgProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.learningRank.AfentiLearningRankProcessor;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * @author peng.zhang.a
 * @since 16-7-19
 */

@Named
@ExposeService(interfaceClass = AfentiSocialService.class)
public class AfentiSocialServiceImpl extends UtopiaAfentiSpringBean implements AfentiSocialService {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject private AfentiInvitationRecordPersistence afentiInvitationRecordPersistence;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private LoadInvitationMsgProcessor loadInvitationMsgProcessor;
    @Inject private AfentiLearningRankProcessor afentiLearningRankProcessor;
    @Inject private AfentiAchievementService afentiAchievementService;
    @Inject private AfentiPopupMessageService afentiPopupMessageService;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;


    @Override
    @Deprecated
    public MapMessage inviteNewUser(StudentDetail student, Long classmateId, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (classmateId == null || !AfentiUtils.isSubjectAvailable(subject) || Objects.equals(student.getId(), classmateId)) {
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        // 当天增加邀请记录
        if (asyncAfentiCacheService.AfentiInviteUserRecordCacheManager_loadRecord(student.getId(), subject)
                .take()
                .contains(classmateId)) {
            return MapMessage.errorMessage(DEFAULT.getInfo())
                    .setErrorCode(DEFAULT.getCode())
                    .set("message", "当天已经邀请");
        }
        asyncAfentiCacheService.AfentiInviteUserRecordCacheManager_setRecord(student.getId(), classmateId, subject)
                .awaitUninterruptibly();

        // 重复邀请直接返回
        List<AfentiInvitationRecord> records = afentiInvitationRecordPersistence.findByUserIdAndSubject(student.getId(), subject);
        AfentiInvitationRecord afentiInvitationRecord = records
                .stream()
                .filter(p -> !SafeConverter.toBoolean(p.getAccepted(), false))
                .filter(p -> p.getInvitedUserId().longValue() == classmateId.longValue())
                .findFirst()
                .orElse(null);
        if (afentiInvitationRecord != null) {
            return MapMessage.successMessage().set("nextMedalTitle", PopupTextContext.INVITE_SUCCESS_MSG.desc);
        }

        // 只能邀请没有购买的用户，并插入邀请记录
        boolean available = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(student.getClazzId())
                .contains(classmateId);

        if (!available) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        OrderProductServiceType productType = AfentiUtils.getOrderProductServiceType(subject);

        AppPayMapper mapper = userOrderLoaderClient.getUserAppPaidStatus(productType.name(), classmateId);
        if (mapper == null || !mapper.isActive()) {
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }

        AfentiInvitationRecord record = AfentiInvitationRecord.newInstence(student.getId(), classmateId, subject);
        afentiInvitationRecordPersistence.persist(record);

        return MapMessage.successMessage().set("nextMedalTitle", PopupTextContext.INVITE_SUCCESS_MSG.desc);
    }

    @Override
    public MapMessage receiveAchievement(StudentDetail student, Subject subject, AchievementType achievementType, Integer level) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (null == subject || null == achievementType || level == 0) {
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
        return afentiAchievementService.receiveAchievement(student, subject, achievementType, level);
    }

    @Override
    public MapMessage refreshAchievement(Long refreshUserId, Subject subject) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(refreshUserId);
        if (!asyncAfentiCacheService.getAfentiAchievementMaxLevelCacheManager().isRecord(studentDetail, subject)) {
            if (studentDetail != null && subject != null) {
                afentiAchievementService.initUserMaxLevel(studentDetail, subject);
                return MapMessage.successMessage();
            }
        }
        return MapMessage.errorMessage("参数为空");
    }

    @Override
    @Deprecated
    public MapMessage clickLiked(StudentDetail studentDetail, Long likedUserId, Subject subject, AfentiRankType afentiRankType) {
        StudentDetail likedUserDetail = studentLoaderClient.loadStudentDetail(likedUserId);
        if (studentDetail == null || subject == null || likedUserDetail == null) {
            return MapMessage.errorMessage();
        }
        if (studentDetail.getId().longValue() == likedUserId.longValue()) {
            return MapMessage.errorMessage("不能给自己点赞");
        }
        Set<Long> clickUserSet = asyncAfentiCacheService.AfentiClickLikedCacheManager_loadTodayClickLikedSet(studentDetail, subject, afentiRankType)
                .take();
        if (clickUserSet != null && clickUserSet.contains(likedUserId)) {
            return MapMessage.errorMessage("当天已经点赞过");
        }
        asyncAfentiCacheService.AfentiClickLikedCacheManager_clickLiked(studentDetail, likedUserDetail, subject, afentiRankType)
                .awaitUninterruptibly();
        asyncAfentiCacheService.AfentiRankLikedSummaryCacheManager_addLiked(afentiRankType, likedUserDetail)
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage fetchPopupMessage(StudentDetail studentDetail, Subject subject) {

        if (studentDetail == null || subject == null) {
            return MapMessage.errorMessage();
        }
        return afentiPopupMessageService.fetchPopupMessage(studentDetail, subject);
    }


    @Override
    public MapMessage loadUserInvitationMsg(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        // 重置小红点
        asyncAfentiCacheService.AfentiPromptCacheManager_reset(student.getId(), subject, AfentiPromptType.invitation)
                .awaitUninterruptibly();
        LoadInvitationMsgContext context;
        try {
            context = loadInvitationMsgProcessor.process(new LoadInvitationMsgContext(student, subject));
            if (context.isSuccessful()) {
                MapMessage mesg = MapMessage.successMessage();
                mesg.putAll(context.getResult());
                return mesg;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }

    @Override
    public MapMessage loadUserAchievements(StudentDetail student, Subject subject) {
        if (null == student)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        // 重置小红点
        asyncAfentiCacheService.AfentiPromptCacheManager_reset(student.getId(), subject, AfentiPromptType.achievement)
                .awaitUninterruptibly();
        return afentiAchievementService.loadUserAchievements(student, subject);
    }

    @Override
    public MapMessage loadLearningRank(User user, Subject subject) {
        //重置小红点
        asyncAfentiCacheService.AfentiPromptCacheManager_reset(user.getId(), subject, AfentiPromptType.rank)
                .awaitUninterruptibly();
        MapMessage mapMessage = MapMessage.successMessage();

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        Date calculateDate = DateRange.newInstance(System.currentTimeMillis(), DateRangeUnit.DAY).getStartDate();
        Date likedSummaryDate = DateRange.newInstance(System.currentTimeMillis(), DateRangeUnit.WEEK).getStartDate();

        //周一排行榜汇总，本周排行榜为空
        if (calculateDate.getTime() != likedSummaryDate.getTime()) {
            LearningRankContext learningRankContext = afentiLearningRankProcessor.process(new LearningRankContext(studentDetail, subject, calculateDate, likedSummaryDate));
            mapMessage.set("nationalList", learningRankContext.getNationalList())
                    .set("schoolList", learningRankContext.getSchoolList())
                    .set("selfSchoolRank", learningRankContext.getSelfSchoolRank())
                    .set("selfNationalRank", learningRankContext.getSelfNationalRank());
        }

        Date lastCalculateDate = DateRange.newInstance(System.currentTimeMillis(), DateRangeUnit.WEEK).getStartDate();
        Date lastLikedSummaryDate = DateRange.newInstance(System.currentTimeMillis(), DateRangeUnit.WEEK).previous().getStartDate();
        LearningRankContext lastWeekLearningRankContext = afentiLearningRankProcessor.process(new LearningRankContext(studentDetail, subject, lastCalculateDate, lastLikedSummaryDate));
        mapMessage.set("lastWeekNationalList", lastWeekLearningRankContext.getNationalList())
                .set("lastWeekSchoolList", lastWeekLearningRankContext.getSchoolList())
                .set("selfLastWeekSchoolRank", lastWeekLearningRankContext.getSelfSchoolRank())
                .set("selfLastWeekNationalRank", lastWeekLearningRankContext.getSelfNationalRank());

        //判断当前用户的购买状态
        OrderProductServiceType orderProductServiceType = AfentiUtils.getOrderProductServiceType(subject);
        AppPayMapper mapper = userOrderLoaderClient.getUserAppPaidStatus(orderProductServiceType.name(), user.getId());

        UseAppStatus useAppStatus;
        if (mapper == null || mapper.unpaid()) {
            useAppStatus = UseAppStatus.NotBuy;
        } else if (mapper.isActive()) {
            useAppStatus = UseAppStatus.Using;
        } else {
            useAppStatus = UseAppStatus.Expired;
        }
        mapMessage.set("useAppStatus", useAppStatus);
        return mapMessage;
    }

    @Override
    public MapMessage fetchMaxLevelClassmates(StudentDetail student, Subject subject, AchievementType achievementType, Integer level) {
        if (student == null || subject == null || achievementType == null || level == null) {
            return MapMessage.errorMessage("参数缺失").setErrorCode(DEFAULT.getCode());
        }
        return afentiAchievementService.fetchMaxLevelClassmates(student, subject, achievementType, level);
    }
}
