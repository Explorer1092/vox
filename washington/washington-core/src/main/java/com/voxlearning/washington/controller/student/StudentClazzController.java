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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;
import com.voxlearning.utopia.service.clazz.client.AsyncTinyGroupServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.service.StudentAccomplishmentService;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import com.voxlearning.utopia.service.zone.api.constant.ZoneConstants;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import com.voxlearning.utopia.service.zone.api.mapper.JournalPagination;
import com.voxlearning.utopia.service.zone.api.mapper.PopularityPagination;
import com.voxlearning.utopia.service.zone.client.*;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.InternalGiftLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.StudentType.TINY_GROUP_LEADER;

@Controller
@RequestMapping("/student/clazz")
public class StudentClazzController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private AsyncTinyGroupServiceClient asyncTinyGroupServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;
    @Inject private ZoneBagServiceClient zoneBagServiceClient;
    @Inject private ZoneConfigServiceClient zoneConfigServiceClient;
    @Inject private ZoneLikeServiceClient zoneLikeServiceClient;
    @Inject private ZonePhotoServiceClient zonePhotoServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;
    @Inject private ZoneSignInServiceClient zoneSignInServiceClient;
    @Inject private InternalGiftLoader internalGiftLoader;

    private StudentAccomplishmentService studentAccomplishmentService;

    @ImportService(interfaceClass = StudentAccomplishmentService.class)
    public void setStudentAccomplishmentService(StudentAccomplishmentService studentAccomplishmentService) {
        this.studentAccomplishmentService = studentAccomplishmentService;
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String latestBest(Model model) {
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        if (null != clazz) {
            // 今天是否签过到
            com.voxlearning.utopia.service.zone.data.SignInContext sc = new com.voxlearning.utopia.service.zone.data.SignInContext();
            sc.setStudentId(student.getId());
            sc.setClazzId(student.getClazzId());
            AlpsFuture<Boolean> signInFuture = zoneSignInServiceClient.getZoneSignInService().checkSignIn(sc);
            // 今天是否上传过照片
            AlpsFuture<Boolean> uploadedFuture = zonePhotoServiceClient.getZonePhotoService().alreadyUploaded(student.getId(), student.getClazzId());

            // 个人名片部分
            model.addAttribute("personalCard", loadStudentPersonalCard(student, true));
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
            model.addAttribute("hasPaymentPassword", StringUtils.isNotBlank(ua.getPaymentPassword()));
            // 班级教师信息
            List<Teacher> teachers = ClazzTeacher.toTeacherList(userAggregationLoaderClient.loadStudentTeachersByClazzId(clazz.getId(), student.getId()));
            model.addAttribute("teachers", teachers);
            //在sendgift.ftl中
            List<User> students = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(clazz.getId(), student.getId());
            students = students.stream()
                    .filter(u -> StringUtils.isNotBlank(u.getProfile().getRealname()))
                    .collect(Collectors.toList());
            model.addAttribute("classmates", students);

            model.addAttribute("hasHomework", loadHomeworkHelper.hasUndoneHomework(student));
            // 判断是否需要弹出输入密码的弹窗
            model.addAttribute("passwordPopup", StringUtils.isBlank(getWebRequestContext().getCookieManager().getCookie("lupld", "")));
            //学生所在班级是否至少有一个认证老师
            boolean teacherCertification = false;
            List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(clazz.getId());
            for (ClazzTeacher clazzTeacher : clazzTeachers) {
                Teacher teacher = clazzTeacher.getTeacher();
                if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
                    teacherCertification = true;
                    break;
                }
            }
            model.addAttribute("teacherCertification", teacherCertification);
            model.addAttribute("popularity", zoneLikeServiceClient.getZoneLikeService().show(student.getId()).getUninterruptibly());

            // 有时间了 这里的逻辑可以重构一下， 按任务链的模式 -- by xiaopeng.yang

            model.addAttribute("signIn", signInFuture.getUninterruptibly());
            model.addAttribute("uploaded", uploadedFuture.getUninterruptibly());
        }

        if (studentLoaderClient.isStudentForbidden(student.getId())) {
            model.addAttribute("stuforbidden", true);
        }

        model.addAttribute("vipUser", userOrderLoaderClient.isVipUser(student.getId()));

        return "studentv3/clazz/index";
    }

    /**
     * 学生个人名片
     */
    @RequestMapping(value = "studentcard.vpage", method = RequestMethod.GET)
    public String studentCard(Model model) {
        Long userId = getRequestLong("userId");
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        model.addAttribute("studentCard", loadStudentPersonalCard(student, false));
        return "studentv3/clazz/studentcard";
    }

    /**
     * 土豪榜
     */
    @RequestMapping(value = "silverrank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage silverRank() {
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        List<Map<String, Object>> rankList = new ArrayList<>();
        if (clazz != null) {
            rankList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(zoneLoaderClient.getZoneLoader())
                    .expiration(1800)
                    .keyPrefix("CLAZZ_WEALTHIEST_RANK")
                    .keys(clazz.getId(), student.getId())
                    .proxy()
                    .silverRank(clazz, student.getId());
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 学霸榜
     */
    @RequestMapping(value = "smcountrank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage smCountRank() {
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        List<Map<String, Object>> rankList = new ArrayList<>();
        if (clazz != null) {
            if (!clazz.isSystemClazz()) {
                rankList = washingtonCacheSystem.CBS.flushable
                        .wrapCache(zoneLoaderClient.getZoneLoader())
                        .expiration(1800)
                        .keyPrefix("CLAZZ_SMCOUNT_RANK")
                        .keys(clazz.getId())
                        .proxy()
                        .studyMasterCountRank(clazz, student.getId());
            } else {
                rankList = washingtonCacheSystem.CBS.flushable
                        .wrapCache(zoneLoaderClient.getZoneLoader())
                        .expiration(1800)
                        .keyPrefix("CLAZZ_SMCOUNT_RANK")
                        .keys(clazz.getId(), student.getId())
                        .proxy()
                        .studyMasterCountRank(clazz, student.getId());
            }
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 点赞榜
     */
    @RequestMapping(value = "likecountrank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage likeCountRank() {
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        List<Map<String, Object>> rankList = new ArrayList<>();
        if (clazz != null) {
            if (!clazz.isSystemClazz()) {
                rankList = washingtonCacheSystem.CBS.flushable
                        .wrapCache(zoneLoaderClient.getZoneLoader())
                        .expiration(1800)
                        .keys(clazz.getId())
                        .proxy()
                        .likeCountRank(clazz, student.getId());
            } else {
                rankList = washingtonCacheSystem.CBS.flushable
                        .wrapCache(zoneLoaderClient.getZoneLoader())
                        .expiration(1800)
                        .keys(clazz.getId(), student.getId())
                        .proxy()
                        .likeCountRank(clazz, student.getId());
            }
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 显示气泡面板
     */
    @RequestMapping(value = "showbubbles.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage showBubbles() {
        return personalZoneLoaderClient.getPersonalZoneLoader().showBubbles(currentUserId());
    }

    /**
     * 更换气泡
     */
    @RequestMapping(value = "changebubble.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBubble() {
        StudentDetail student = currentStudentDetail();
        Long bubbleId = getRequestLong("bubbleId");
        try {
            // 这里有关于魔法城堡的判断
            MapMessage message = changeBubbleHandler(student, bubbleId);
            if (message.isSuccess() && student.getClazz() != null) {
                // 如果今天没有更换过气泡，发新鲜事
                String content = student.fetchRealname() + "更换了新鲜事气泡";
                if (!student.getClazz().isSystemClazz()) {// 非系统自建班级
                    zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                            .withUser(student.getId())
                            .withUser(student.fetchUserType())
                            .withClazzJournalType(ClazzJournalType.CHANGE_BUBBLE)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                            .withPolicy(JournalDuplicationPolicy.DAILY)
                            .commit();
                } else {// 系统自建班级
                    List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);
                    //TODO currently send to one group for the student
                    //TODO or one message will be saw multi-times for other students
                    if (groups.size() > 0) {
                        zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                                .withUser(student.getId())
                                .withUser(student.fetchUserType())
                                .withClazzJournalType(ClazzJournalType.CHANGE_BUBBLE)
                                .withClazzJournalCategory(ClazzJournalCategory.MISC)
                                .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                                .withPolicy(JournalDuplicationPolicy.DAILY)
                                .withGroup(groups.get(0).getId())
                                .commit();
                    }
                }
            }
            return message;
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("请不要重复操作").withDuplicatedException();
        }
    }

    private MapMessage changeBubbleHandler(StudentDetail student, Long bubbleId) {
        ClazzZoneProduct product = zoneConfigServiceClient.getClazzZoneProductFromBuffer(bubbleId);
        if (product == null) {
            return MapMessage.errorMessage("气泡不存在");
        }
        StudentMagicLevel level = studentMagicCastleServiceClient.loadStudentMagicLevel(student.getId());
        if (level == null) {
            level = StudentMagicLevel.newLevel();
        }
        switch (product.fetchSubspecies()) {
            case MAGIC_CASTLE_LV1: {
                if (level.getLevel() < 4) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV2: {
                if (level.getLevel() < 7) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV3: {
                if (level.getLevel() < 10) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV4: {
                if (level.getLevel() < 13) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV5: {
                if (level.getLevel() < 16) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV6: {
                if (level.getLevel() < 19) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV7: {
                if (level.getLevel() < 22) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV8: {
                if (level.getLevel() < 25) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV9: {
                if (level.getLevel() < 28) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV10: {
                if (level.getLevel() < 31) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            case MAGIC_CASTLE_LV11: {
                if (level.getLevel() < 34) {
                    return MapMessage.errorMessage("魔法师等级不足");
                }
                break;
            }
            default:
                break;
        }
        return atomicLockManager.wrapAtomic(personalZoneServiceClient.getPersonalZoneService())
                .keys(student)
                .proxy()
                .changeBubble(student, bubbleId);
    }

    /**
     * 点赞
     */
    @RequestMapping(value = "like.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage like() {
        Long journalId = getRequestLong("journalId");
        Long clazzId = getRequestLong("clazzId");
        Long relevantUserId = getRequestLong("relevantUserId");

        User user = currentUser();
        User relevantUser;
        boolean selfHigh = false;
        if (Objects.equals(user.getId(), relevantUserId)) {
            // 自己给自己点赞...魔镜魔镜，谁属至美？显然Charlize的王后最赞
            relevantUser = user;
            selfHigh = true;
        } else {
            relevantUser = raikouSystem.loadUser(relevantUserId);
        }

        MapMessage mesg = clazzJournalServiceClient.like(user, journalId, relevantUser, clazzId);
        if (mesg.isSuccess() && !selfHigh && !zoneLikeServiceClient.getZoneLikeService().sent(relevantUserId).getUninterruptibly()) {
            String message = "班级空间里有你的一条新消息哦！";
            String messagePayload = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【去看看】</a>",
                    "/student/clazz/index.vpage?type=popularity");
            userPopupServiceClient.createPopup(relevantUserId)
                    .content(messagePayload)
                    .type(PopupType.STUDENT_RECEIVE_LIKE_OR_COMMENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            zoneLikeServiceClient.getZoneLikeService().record(relevantUserId).awaitUninterruptibly();
            zoneLikeServiceClient.getZoneLikeService().turnOn(relevantUserId).awaitUninterruptibly();
        }
        return mesg;
    }

    /**
     * 显示可选心情
     */
    @RequestMapping(value = "moods.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage listAllMoods() throws InterruptedException {
        StudentDetail student = currentStudentDetail();
        Collection<UserMood> moods = zoneConfigServiceClient.getUserMoodListFromBuffer();

        com.voxlearning.utopia.service.zone.data.SignInContext sc = new com.voxlearning.utopia.service.zone.data.SignInContext();
        sc.setStudentId(student.getId());
        sc.setClazzId(student.getClazzId());
        boolean signIn = zoneSignInServiceClient.getZoneSignInService()
                .checkSignIn(sc).getUninterruptibly();
        boolean freeSignable;
        try {
            // 如果今天已经签到过了，就没有必要再去判断是否可以免费签到了。
            freeSignable = signIn || isFreeSignable(student);
        } catch (Exception ex) {
            logger.error("Failed to check student {} zone free signable", student.getId(), ex);
            freeSignable = false;
        }
        return MapMessage.successMessage().add("moods", moods).add("needPay", !freeSignable);
    }

    /**
     * 签到
     */
    @RequestMapping(value = "czsignin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzZoneSignIn() {
        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage("签到失败");
        }
        Long moodId = getRequestLong("moodId");
        UserMood mood = zoneConfigServiceClient.getUserMoodFromBuffer(moodId);
        if (mood == null) {
            return MapMessage.errorMessage("签到失败");
        }

        com.voxlearning.utopia.service.zone.data.SignInContext sc = new com.voxlearning.utopia.service.zone.data.SignInContext();
        sc.setStudentId(student.getId());
        sc.setClazzId(student.getClazzId());
        boolean alreadySingedIn = zoneSignInServiceClient.getZoneSignInService()
                .checkSignIn(sc).getUninterruptibly();
        if (alreadySingedIn) {
            return MapMessage.errorMessage("您今天已经签到过了，不能重复签到");
        }

        boolean freeSignable;
        try {
            freeSignable = isFreeSignable(student);
        } catch (InterruptedException ex) {
            return MapMessage.errorMessage("签到失败");
        }
        boolean needPay = !freeSignable;
        AtomicCallbackBuilder<MapMessage> builder = AtomicLockManager.getInstance().callback();
        MapMessage message;
        try {
            message = builder.keyPrefix("StudentClazzController:clazzZoneSignIn")
                    .keys(student.getId())
                    .callback(() -> {
                        if (needPay) {
                            UserIntegral ui = student.getUserIntegral();
                            if (ui == null || ui.getUsable() < ZoneConstants.COST_SIGN_IN) {
                                return MapMessage.errorMessage("余额不足");
                            }
                            int count = -ZoneConstants.COST_SIGN_IN;
                            IntegralHistory integralHistory = new IntegralHistory(student.getId(), IntegralType.学生班级空间签到, count);
                            integralHistory.setComment(DateUtils.dateToString(new Date(), "yyyy年MM月dd日") + "班级空间签到扣除学豆");
                            MapMessage msg = userIntegralService.changeIntegral(student, integralHistory);
                            if (!msg.isSuccess()) {
                                return MapMessage.errorMessage("余额不足");
                            }
                        }
                        Boolean ret = zoneSignInServiceClient.getZoneSignInService()
                                .finishSignIn(sc)
                                .getUninterruptibly();
                        if (SafeConverter.toBoolean(ret)) {
                            return MapMessage.successMessage("签到成功");
                        } else {
                            return MapMessage.errorMessage("签到失败");
                        }
                    })
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("请不要重复签到");
        } catch (Exception ex) {
            logger.error("Student {} failed to sign in zone", student.getId(), ex);
            return MapMessage.errorMessage("签到失败");
        }

        if (message.isSuccess()) {
            String content = mood.getTitle() + ":" + mood.getDescription();
            if (!student.getClazz().isSystemClazz()) {// 非系统自建班级
                zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                        .withUser(student.getId())
                        .withUser(student.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.SIGN_IN)
                        .withClazzJournalCategory(ClazzJournalCategory.MISC)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "img", mood.getImgUrl())))
                        .commit();
            } else {// 系统自建班级
                List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);
                //TODO currently send to one group for the student
                //TODO or one message will be saw multi-times for other students
                if (groups.size() > 0) {
                    zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                            .withUser(student.getId())
                            .withUser(student.fetchUserType())
                            .withClazzJournalType(ClazzJournalType.SIGN_IN)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "img", mood.getImgUrl())))
                            .withGroup(groups.get(0).getId())
                            .commit();
                }
            }
            message.setInfo("签到成功");
        }
        return message;
    }

    /**
     * 上传照片 发送新动态
     */
    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPhoto() {
        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage("上传失败");
        }
        String photoFileNames = getRequestString("photos");
        if (StringUtils.isBlank(photoFileNames)) {
            return MapMessage.errorMessage("上传失败");
        }
        String content = student.fetchRealname() + "上传了照片";
        List<String> photos = Arrays.asList(StringUtils.split(photoFileNames, ","));
        zonePhotoServiceClient.getZonePhotoService().photoUploaded(student.getId(), student.getClazzId()).awaitUninterruptibly();
        if (!student.getClazz().isSystemClazz()) {// 非系统自建班级
            zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                    .withUser(student.getId())
                    .withUser(student.fetchUserType())
                    .withClazzJournalType(ClazzJournalType.STUDENT_UPLOAD_PHOTO)
                    .withClazzJournalCategory(ClazzJournalCategory.MISC)
                    .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "photos", photos)))
                    .withPolicy(JournalDuplicationPolicy.DAILY)
                    .commit();
        } else {// 系统自建班级
            List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);
            //TODO currently send to one group for the student
            //TODO or one message will be saw multi-times for other students
            if (groups.size() > 0) {
                zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                        .withUser(student.getId())
                        .withUser(student.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.STUDENT_UPLOAD_PHOTO)
                        .withClazzJournalCategory(ClazzJournalCategory.MISC)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "photos", photos)))
                        .withPolicy(JournalDuplicationPolicy.DAILY)
                        .withGroup(groups.get(0).getId())
                        .commit();
            }
        }
        return MapMessage.successMessage("上传成功");
    }

    /**
     * 表情评论
     */
    @RequestMapping(value = "czcomment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzZoneComment() {
        Long relevantUserId = getRequestLong("relevantUserId");
        Long journalId = getRequestLong("journalId");
        Long clazzId = getRequestLong("clazzId");
        Long imageId = getRequestLong("imgId");
        User user = currentUser();
        User relevantUser;
        boolean selfHigh = false;
        if (Objects.equals(user.getId(), relevantUserId)) {
            // 自己给自己的新鲜事评论
            relevantUser = user;
            selfHigh = true;
        } else {
            relevantUser = raikouSystem.loadUser(relevantUserId);
        }

        MapMessage mesg = clazzJournalServiceClient.comment(user, relevantUser, journalId, clazzId, imageId);
        if (mesg.isSuccess() && !selfHigh && !zoneLikeServiceClient.getZoneLikeService().sent(relevantUserId).getUninterruptibly()) {
            String message = "班级空间里有你的一条新消息哦！";
            String messagePayload = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【去看看】</a>",
                    "/student/clazz/index.vpage?type=popularity");
            userPopupServiceClient.createPopup(relevantUserId)
                    .content(messagePayload)
                    .type(PopupType.STUDENT_RECEIVE_LIKE_OR_COMMENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            zoneLikeServiceClient.getZoneLikeService().record(relevantUserId).awaitUninterruptibly();
            zoneLikeServiceClient.getZoneLikeService().turnOn(relevantUserId).awaitUninterruptibly();
        }
        return mesg;
    }

    /**
     * 班级动态
     */
    @RequestMapping(value = "clazzlatestnews.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexClazzLatestNews() {
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("班级为空");
        }
        int currentPage = ConversionUtils.toInt(getRequest().getParameter("currentPage"), 1);
        String type = getRequestString("type");
        // 支持移动端，type在Flash里面是关键字不能用，移动端参数名改成Type
        if (StringUtils.isBlank(type)) {
            type = getRequestString("Type");
        }
        int page = currentPage - 1;
        int size = 10;
        switch (type) {
            case "ALL": {
                Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false).stream()
                        .map(GroupMapper::getId)
                        .collect(Collectors.toSet());
                JournalPagination journalPagination = clazzJournalLoaderClient.getClazzJournalLoader().getClazzJournals(
                        student.getId(), clazz.getId(), page, size, null, groupIds);
                return MapMessage.successMessage().add("journalPage", journalPagination);
            }
            case "PERSONAL": {
                JournalPagination journalPagination = clazzJournalLoaderClient.getClazzJournalLoader().getClazzJournals(student.getId(), page, size);
                return MapMessage.successMessage().add("journalPage", journalPagination);
            }
            default: {
                return MapMessage.errorMessage("获取班级动态失败");
            }
        }
    }

    /**
     * 删除新鲜事
     */
    @RequestMapping(value = "delMyJournal.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentDelJournal() {
        long journalId = getRequestLong("journalId", -1);
        if (-1l == journalId) {
            return new MapMessage().setSuccess(false).setInfo("参数错误");
        }
        MapMessage response = clazzJournalServiceClient.delete(journalId, currentUserId());
        if (response.hasDuplicatedException()) {
            return MapMessage.errorMessage("请不要重复点击");
        }
        response.setInfo(response.isSuccess() ? "删除成功" : "删除失败");
        return response;
    }


    /**
     * 班级规则
     */
    @RequestMapping(value = "rank/rankrule.vpage", method = RequestMethod.GET)
    public String rankRule(Model model) {
        return "studentv3/clazz/rank/rankrule";
    }

//    /**
//     * 班级排行榜，领取奖励
//     */
//    @RequestMapping(value = "rank/rewardlist.vpage", method = RequestMethod.GET)
//    public String rewardList(Model model) {
//        StudentDetail student = currentStudentDetail();
//        Clazz clazz = student.getClazz();
//        if (null == clazz) {
//            return "redirect:/student/index.vpage";
//        }
//        List<ClazzRankHistory> histories = deprecatedClazzZoneServiceClient.getRemoteReference().loadClazzRankHistory(clazz, student.getId());
//        model.addAttribute("rewardList", histories);
//        return "studentv3/clazz/rank/rewardlist";
//    }

//    /**
//     * 领取奖励---班级排行榜
//     */
//    @RequestMapping(value = "awardclazzrankreward.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage awardClazzRankReward() {
//        StudentDetail student = currentStudentDetail();
//        Clazz clazz = student.getClazz();
//        if (null == clazz) {
//            return MapMessage.errorMessage("班级为空");
//        }
//        int rank = getRequestInt("rank", -1);
//        String month = getRequestString("month");
//        if (StringUtils.isBlank(month) || rank == -1) {
//            return MapMessage.errorMessage("参数错误");
//        }
//        if (deprecatedClazzZoneServiceClient.getRemoteReference().hasAwardClazzRankReward(student.getId(), rank, month)) {
//            return MapMessage.errorMessage("你已经领取过了。");
//        }
//        MapMessage mapMessage;
//        try {
//            mapMessage = atomicLockManager.wrapAtomic(deprecatedClazzZoneServiceClient.getRemoteReference())
//                    .expirationInSeconds(30)
//                    .keyPrefix("AWARD_CLAZZ_RANK_REWARD")
//                    .keys(student.getId())
//                    .proxy()
//                    .awardClazzRankReward(clazz, student, month, rank);
//        } catch (DuplicatedOperationException ignore) {
//            return MapMessage.errorMessage("你点击太快了，请重试");
//        }
//        return mapMessage;
//    }

    // 我的人气
    @RequestMapping(value = "popularity.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage popularity() {
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("班级为空");
        }
        int currentPage = ConversionUtils.toInt(getRequest().getParameter("currentPage"), 1);
        int page = currentPage - 1;
        int size = 10;
        PopularityPagination pagination = clazzJournalLoaderClient.getClazzJournalLoader().getUserPopularity(student.getId(), page, size);
        zoneLikeServiceClient.getZoneLikeService().turnOff(student.getId()).awaitUninterruptibly();
        return MapMessage.successMessage().add("journalPage", pagination);
    }

    /**
     * In clazz zone, use this method to load specified student' personal card.
     * The personal card is used for poping up.
     */
    private MapMessage loadStudentPersonalCard(final StudentDetail student,
                                               boolean self) {
        if (student == null || student.getId() == null) {
            return MapMessage.successMessage();
        }
        MapMessage message = MapMessage.successMessage();
        message.add("studentId", student.getId());
        message.add("img", student.fetchImageUrl());
        message.add("realname", student.fetchRealname());
        message.add("vip", userOrderLoaderClient.isVipUser(student.getId()));
        message.add("silver", student.getUserIntegral() == null ? 0 : student.getUserIntegral().getUsable());
        // 是否是小组长
        message.add("isLeader", asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                .findTinyGroupStudentRefsByStudentId(student.getId())
                .getUninterruptibly()
                .stream().filter(ref -> ref.getType() == TINY_GROUP_LEADER).count() > 0);

        final StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(student.getId());
        if (studentInfo == null) {
            message.add("smCount", 0);
            message.add("likeCount", 0);
            message.add("bubble", 0);
            message.add("signInCount", 0);
        } else {
            if (self) {
                // 如果当前人是自己的话，看看使用的气泡是否失效（过期了，或者不是付费用户了）
                message.add("bubble", dealWithBubble(studentInfo.getBubbleId(), student));
            } else {
                // 如果是看别人名片，发个异步请求，更新气泡
                AlpsThreadPool.getInstance().submit(() -> dealWithBubble(studentInfo.getBubbleId(), student));
            }
            message.add("smCount", studentInfo.getStudyMasterCount());
            message.add("likeCount", studentInfo.getLikeCount());
            message.add("signInCount", studentInfo.getSignInCount());
        }
        message.add("gifts", internalGiftLoader.loadLatestThreeGifts(student.getId()));
        message.add("noThanksCount", giftLoaderClient.getGiftLoader().loadNoThanksGiftCount(student.getId()));
        if (student.getClazz() != null) {
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(student.getClazz().getSchoolId())
                    .getUninterruptibly();
            message.add("payOpen", school == null ? false : school.getPayOpen());
        } else {
            message.add("payOpen", false);
        }

        return message;
    }

    private Long dealWithBubble(final Long currentBubble, StudentDetail student) {
        ClazzZoneProduct product = zoneConfigServiceClient.getClazzZoneProductFromBuffer(currentBubble);
        if (product == null) {
            // 气泡不存在， 直接更换为默认气泡
            personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
            return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
        }
        StudentMagicLevel level = studentMagicCastleServiceClient.loadStudentMagicLevel(student.getId());
        switch (product.fetchSubspecies()) {
            case AFENTI_EXAM: {
                if (!userOrderLoaderClient.isVipUser(student.getId())) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case AFENTI_BASIC: {
//                if (!student.isAfentiBasicFlag()) {
//                    personalZoneServiceClient.__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
//                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
//                }
                break;
            }
            case TALENT: {
//                if (!student.isAfentiTalentFlag()) {
//                    personalZoneServiceClient.__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
//                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
//                }
                break;
            }
            case PAY: {
                final Date current = new Date();
                List<ClazzZoneBag> list = zoneBagServiceClient.getZoneBagService()
                        .findClazzZoneBagList(student.getId())
                        .getUninterruptibly()
                        .stream()
                        .filter(source -> Objects.equals(source.getProductId(), currentBubble))
                        .filter(source -> source.getExpireDate() != null && source.getExpireDate().after(current))
                        .collect(Collectors.toList());
                if (list.isEmpty()) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case BEST_TINY_GROUP_ENGLISH: {
                if (!tinyGroupLoaderClient.isBestTinyGroupAvailable(student.getId(), Subject.ENGLISH)) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case BEST_TINY_GROUP_MATH: {
                if (!tinyGroupLoaderClient.isBestTinyGroupAvailable(student.getId(), Subject.MATH)) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case BEST_TINY_GROUP_CHINESE: {
                if (!tinyGroupLoaderClient.isBestTinyGroupAvailable(student.getId(), Subject.CHINESE)) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV1: {
                if (level != null && level.getLevel() < 4) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV2: {
                if (level != null && level.getLevel() < 7) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV3: {
                if (level != null && level.getLevel() < 10) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV4: {
                if (level != null && level.getLevel() < 13) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV5: {
                if (level != null && level.getLevel() < 16) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV6: {
                if (level != null && level.getLevel() < 19) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV7: {
                if (level != null && level.getLevel() < 22) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV8: {
                if (level != null && level.getLevel() < 25) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV9: {
                if (level != null && level.getLevel() < 28) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV10: {
                if (level != null && level.getLevel() < 31) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case MAGIC_CASTLE_LV11: {
                if (level != null && level.getLevel() < 34) {
                    personalZoneServiceClient.getPersonalZoneService().__changeBubble(student, ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE);
                    return ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
                }
                break;
            }
            case FREE:
            default:
                break;
        }
        return currentBubble;
    }


    private boolean isStudentHasMustCompletedHomework(Long studentId, Long clazzId, HomeworkType homeworkType) {
        if (studentId == null || clazzId == null || homeworkType == null) {
            return false;
        }
        Date now = new Date();
        // 获取组ID 新作业体系
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadClazzGroups(clazzId);
        GroupMapper groupMapper = groupMappers.stream().filter(g -> g.getSubject() == homeworkType.getSubject()).findFirst().orElse(null);
        if (groupMapper == null) {
            return false;
        }

        List<NewHomework.Location> mathNewHomeworkList = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupMapper.getId(), groupMapper.getSubject());
        NewHomework.Location newLocation = mathNewHomeworkList.stream()
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime())).findFirst().orElse(null);
        if (newLocation != null) {
            NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(newLocation);
            boolean studentFinished = accomplishment != null && accomplishment.contains(studentId);
            if (!newLocation.isChecked() && now.after(new Date(newLocation.getStartTime())) && now.before(new Date(newLocation.getEndTime())) && !studentFinished) {
                return true;
            }
        }

        return false;
    }


    private boolean isFreeSignable(StudentDetail student) throws InterruptedException {
        if (student == null || student.getId() == null) {
            return false;
        }
        // 今天发飙心情要花钱么？？？ vip不用花钱，今天完成过作业或者测验不花钱（补做除外）
        if (userOrderLoaderClient.isVipUser(student.getId())) {
            return true;
        }
        String key = CacheKeyGenerator.generateCacheKey(StudentClazzController.class, "isFreeSignable", student.getId());
        UtopiaCache cache = CacheSystem.CBS.getCache("flushable");
        if (cache.load(key) != null) {
            // 已经完成了检查，并且检查结果为真，无需再检查了
            return true;
        }

        // 今天是否完成过作业，补做不算。
        boolean ret = studentAccomplishmentService.finishedHomeworkWithinToday(student.getId()).get();
        if (ret) {
            cache.set(key, DateUtils.getCurrentToDayEndSecond(), "1");
        }
        return ret;
    }
}
