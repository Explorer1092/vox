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

package com.voxlearning.washington.controller.ambassador;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.DefaultNarrowUser;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorAcademyRecord;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.cache.BusinessCache;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.message.api.constant.UserMessageType;
import com.voxlearning.utopia.service.message.api.entity.UserMessage;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by XiaoPeng.Yang on 15-5-6.
 */
@Controller
@RequestMapping("/ambassador")
public class AmbassadorController extends AbstractController {

    @Inject private AsyncUserServiceClient asyncUserServiceClient;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private FeedbackServiceClient feedbackServiceClient;
    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private WechatCodeServiceClient wechatCodeServiceClient;

    @Inject private BusinessTeacherServiceClient businessTeacherServiceClient;

    // 2015 暑假改版 大使专区首页
    @RequestMapping(value = "center.vpage", method = RequestMethod.GET)
    public String center(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher.getTeacherSchoolId() == null) {
            return "redirect:/teacher/index.vpage";
        }
        if (teacher.isSchoolAmbassador()) {
            AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacher.getId());
            // 未读消息数量
            int unReadCount = messageLoaderClient.getMessageLoader().countUnreadUserMessages(teacher.getId(), UserMessageType.AMBASSADOR_MSG);
            model.addAttribute("unReadCount", unReadCount);
            // 是否绑定了大使微信
            boolean isBinding = wechatLoaderClient.isBinding(teacher.getId(), WechatType.AMBASSADOR.getType());
            model.addAttribute("isBinding", isBinding);
            // 正式大使
            if (levelDetail != null && levelDetail.getLevel() != AmbassadorLevel.SHI_XI) {
                Map<String, Object> dataMap = businessTeacherServiceClient.loadAmbassadorZSInfo(teacher);
                model.addAttribute("dataMap", dataMap);
                model.addAttribute("level", levelDetail);
                // fixme 任务栏暂时下线
                // 判断是否显示任务栏 如果是正式大使 全部显示
                if (levelDetail.getLevel() == AmbassadorLevel.TONG_PAI) {
                    // 铜牌大使  根据成为铜牌大使的时间 判断是否显示任务栏 本月15号之后不显示
                    String middle = DateUtils.dateToString(new Date(), "yyyy-MM") + "-15 00:00:00";
                    if (levelDetail.getBornDate() != null && levelDetail.getBornDate().after(DateUtils.stringToDate(middle))) {
                        model.addAttribute("showMisson", false);
                    } else {
                        model.addAttribute("showMisson", true);
                    }
                } else {
                    model.addAttribute("showMisson", true);
                }
                return "teacherv3/ambassador/zscenter";
            } else {
                // 获取实习大使首页需要展示的信息
                // 实习期
                AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacher.getId())
                        .stream().findFirst().orElse(null);
                String sxStr = DateUtils.dateToString(ref.getCreateDatetime(), "yyyy年M月d日") + "-" +
                        DateUtils.dateToString(DateUtils.calculateDateDay(ref.getCreateDatetime(), 30), "yyyy年M月d日");
                model.addAttribute("sxStr", sxStr);
                // 获取实习期首页展示的信息
                Map<String, Object> dataMap = businessTeacherServiceClient.loadAmbassadorSHXInfo(teacher);
                model.addAttribute("dataMap", dataMap);
                // 实习大使页面
                return "redirect:/teacher/index.vpage";
            }
        } else {
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
            //如果本学校没有大使 直接跳转到申请大使页面
            if (ref == null) {
                return "redirect:/teacher/index.vpage";
            }
            //本人是否预备大使
            boolean isCompetition = businessTeacherServiceClient.isJoinCompetition(teacher.getId());
            if (isCompetition) {
                // 是否预备大使， 跳转到大使
                return "redirect:/teacher/index.vpage";
            }
        }
        return "redirect:/teacher/index.vpage";
    }

    // 竞聘详细
    @RequestMapping(value = "competitiondetail.vpage", method = RequestMethod.GET)
    public String competitionDetail(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
        if (ref == null) {
            return "redirect:/teacher/index.vpage";
        }
        TeacherDetail ambassador = teacherLoaderClient.loadTeacherDetail(ref.getAmbassadorId());
        model.addAttribute("ambassador", ambassador);
        AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(ambassador.getId());
        model.addAttribute("level", levelDetail);

        //我的竞聘信息 每天更新
        Map<String, Object> myRankInfo = BusinessCache.getBusinessCache()
                .wrapCache(businessTeacherServiceClient)
                .expiration(DateUtils.getCurrentToDayEndSecond())
                .keyPrefix("SCHOOL_AMBASSADOR_COMPETITION_MY_RANK")
                .keys(teacher.getId())
                .proxy()
                .loadAmbassadorMyCompetitionInfo(teacher.getId(), teacher.getTeacherSchoolId(), teacher.getSubject());
        model.addAttribute("myRankInfo", myRankInfo);
        return "teacherv3/ambassador/competitiondetail";
    }

    // 申请预备大使页
    @RequestMapping(value = "ambapplication.vpage", method = RequestMethod.GET)
    public String ambapplication(Model model) {
        /*TeacherDetail detail = currentTeacherDetail();
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(detail.getSubject(), detail.getTeacherSchoolId());
        if (ref == null) {
            return "redirect:/teacher/index.vpage";
        }
        TeacherDetail ambassador = teacherLoaderClient.loadTeacherDetail(ref.getAmbassadorId());
        model.addAttribute("ambassador", ambassador);
        AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(ambassador.getId());
        model.addAttribute("level", levelDetail);
        // 是否30天内辞任过大使
        boolean haveBeAmbassador = ambassadorLoaderClient.getAmbassadorLoader().haveBeAmbassador(detail.getId());
        model.addAttribute("haveBeAmbassador", haveBeAmbassador);
        return "teacherv3/ambassador/ambapplication";*/

        return "redirect:/teacher/index.vpage";
    }

    // 管理本校老师 ---- 这个恶心的东西已经被干掉了，谁再来，请去首页.
    @RequestMapping(value = "schoolteachermgn.vpage", method = RequestMethod.GET)
    public String schoolTeacherManagement(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 大使消息
    @RequestMapping(value = "messagelist.vpage", method = RequestMethod.GET)
    public String messageList(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || !teacher.isSchoolAmbassador()) {
            return "redirect:/teacher/index.vpage";
        }
        int unReadCount = messageLoaderClient.getMessageLoader().countUnreadUserMessages(teacher.getId(), UserMessageType.AMBASSADOR_MSG);
        model.addAttribute("unReadCount", unReadCount);//未读数
        AmbassadorSchoolRef schoolRef = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(teacher.getId())
                .stream().findFirst().orElse(null);

        // FIXME: 原代码使用schoolRef的创建时间作为起始时间读消息
        DefaultNarrowUser user = (DefaultNarrowUser) teacher.narrow();
        user.setCreateTime(schoolRef.getCreateDatetime());

        Page<UserMessage> messagePage = messageLoaderClient.getMessageLoader().getMessages(user,
                UserMessageType.AMBASSADOR_MSG,
                0,
                1000);//目前没有分页  显示前1000条先

        model.addAttribute("messageList", messagePage.getContent());
        return "teacherv3/ambassador/messagelist";
    }

    // 申请为预备大使
    @RequestMapping(value = "joincompetition.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinCompetition() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherDetail.getId());
        int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());

        if (teacherDetail.fetchCertificationState() != AuthenticationState.SUCCESS || teacherLevel < 2) {
            return MapMessage.errorMessage("对不起，您还不满足申请条件，请满足后再来申请！");
        }
        try {
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("JOIN_AMBASSADOR_COMPETITION")
                    .keys(currentUserId())
                    .proxy()
                    .joinAmbassadorCompetition(teacherDetail);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 大使学院
    @RequestMapping(value = "academy.vpage", method = RequestMethod.GET)
    public String academy(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || !teacher.isSchoolAmbassador()) {
            return "redirect:/teacher/index.vpage";
        }
        AmbassadorAcademyRecord record = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorAcademyRecord(teacher.getId());
        if (null != record) {
            model.addAttribute("recordType", record.getRecordType());

            AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacher.getId());
            if (levelDetail == null) {
                levelDetail = new AmbassadorLevelDetail();
                levelDetail.setAmbassadorId(currentUserId());
                levelDetail.setLevel(AmbassadorLevel.SHI_XI);
            }
            model.addAttribute("ambassadorLevel", levelDetail);
        }
        return "teacherv3/ambassador/academy";
    }

    // 大使学院
    @RequestMapping(value = "startlearning.vpage", method = RequestMethod.GET)
    public String startLearning() {
        return "teacherv3/ambassador/startlearning";
    }

    // 完成答题记录（阶段）
    @RequestMapping(value = "saverecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecord() {
        Integer type = getRequestInt("recordType");
        AmbassadorRecordType recordType = AmbassadorRecordType.typeOf(type);
        if (recordType == null) {
            return MapMessage.errorMessage("操作失败");
        }
        try {
            return atomicLockManager.wrapAtomic(ambassadorServiceClient.getAmbassadorService())
                    .expirationInSeconds(30)
                    .keyPrefix("FINISH_AMBASSADOR_ACADEMY_RECORD")
                    .keys(currentUserId())
                    .proxy()
                    .saveUpdateAmbassadorAcademyRecord(currentUserId(), recordType);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 记录QQ号
    @RequestMapping(value = "saveqq.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveQQ() {
        String qq = getRequestString("qq");
        if (StringUtils.isBlank(qq)) {
            return MapMessage.errorMessage("操作失败");
        }
        try {
            return atomicLockManager.wrapAtomic(userServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("AMBASSADOR_ACADEMY_RECORD_QQ")
                    .keys(currentUserId())
                    .proxy()
                    .changeQQ(currentUserId(), qq);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    /**
     * 校园大使申请页
     */
    @RequestMapping(value = "schoolambassador.vpage", method = RequestMethod.GET)
    public String schoolambassador(Model model) {
        /*TeacherDetail teacher = currentTeacherDetail();
        //获取同科大使
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
        if (ref != null) {
            model.addAttribute("ambassadorId", ref.getAmbassadorId());
            model.addAttribute("ambassadorName", teacherLoaderClient.loadTeacher(ref.getAmbassadorId()).fetchRealname());
        }
        // 是否30天内辞任过大使
        boolean haveBeAmbassador = ambassadorLoaderClient.getAmbassadorLoader().haveBeAmbassador(teacher.getId());
        model.addAttribute("haveBeAmbassador", haveBeAmbassador);
        // 1 2月不开放
        int month = MonthRange.current().getMonth();
        if (month == 1 || month == 2) {
            model.addAttribute("inWinter", true);
        }*/
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 校园大使申请提交
     */
    @RequestMapping(value = "schoolambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage schoolAmbassador() {
        TeacherDetail teacher = currentTeacherDetail();
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
        int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());
        boolean haveBeAmbassador = ambassadorLoaderClient.getAmbassadorLoader().haveBeAmbassador(teacher.getId());
        if (haveBeAmbassador) {
            return MapMessage.errorMessage("您先前辞任过大使，目前尚无法申请，谢谢。");
        }
        // 提交方式
        String _from = getRequestParameter("_from", "");
        if (StringUtils.isNotBlank(_from) && Objects.equals(_from, "apply")) {
            // 1 2月不开放
            int month = MonthRange.current().getMonth();
            if (month == 1 || month == 2) {
                return MapMessage.errorMessage("寒假期间，大使报名暂时关闭，3月1日正常开启。");
            }
        }
        // 前置条件 认证老师 & >=Lv2
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS || teacherLevel < 2) {
            return MapMessage.errorMessage("对不起，您还不满足申请条件，请满足后再来申请！");
        }
        //手动虚假老师直接返回
        CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacher.getId());
        if (teacherSummary != null && teacherSummary.getFakeTeacher() && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(teacherSummary.getValidationType())) {
            return MapMessage.errorMessage("对不起，您目前无法申请！");
        }
        String name = StringHelper.cleanXSS(getRequest().getParameter("name"));
        String mobile = getRequest().getParameter("mobile");
        String qq = getRequest().getParameter("qq");
        String leader = getRequest().getParameter("leader");
        String gender = getRequest().getParameter("gender");
        String address = getRequest().getParameter("address");
        Integer totalCount = getRequestInt("totalCount");
        Integer usingCount = getRequestInt("usingCount");
        String suggestion = StringHelper.cleanXSS(getRequest().getParameter("suggestion"));
        Integer englishCount = getRequestInt("englishCount");
        Integer mathCount = getRequestInt("mathCount");
        Integer chineseCount = getRequestInt("chineseCount");
        Integer studentCount = getRequestInt("studentCount");
        Integer clazzCount = getRequestInt("clazzCount");
        String eduSystemType = getRequest().getParameter("eduSystemType");
        String source = getRequest().getParameter("source");
        String pname = getRequestString("pname");
        String cname = getRequestString("cname");
        String aname = getRequestString("aname");
        Integer bYear = getRequestInt("bYear");
        Integer tYear = getRequestInt("tYear");
        Integer bMonth = getRequestInt("bMonth");
        Integer bDay = getRequestInt("bDay");
        boolean isFx = getRequestBool("isFx");
        String fxClass = getRequestString("fxClass");
        String schoolName = getRequestString("schoolName");
        String schoolLevel = getRequestString("schoolLevel");
        Integer oneGradeClazzCountBegin = getRequestInt("oneGradeClazzCountBegin");
        Integer oneGradeClazzCountEnd = getRequestInt("oneGradeClazzCountEnd");
        Integer oneClazzStudentCountBegin = getRequestInt("oneClazzStudentCountBegin");
        Integer oneClazzStudentCountEnd = getRequestInt("oneClazzStudentCountEnd");

        try {
            MapMessage message = atomicLockManager.wrapAtomic(this)
                    .expirationInSeconds(30)
                    .keyPrefix("APPLY_AMBASSADOR")
                    .keys(currentUserId())
                    .proxy()
                    .applySchoolAmbassador(teacher, name, mobile, qq, "", leader,
                            totalCount, usingCount, suggestion, gender, address,
                            englishCount, mathCount, chineseCount, studentCount, clazzCount, eduSystemType, source,
                            pname, cname, aname, bYear, tYear, bMonth, bDay, isFx, fxClass, schoolName, schoolLevel,
                            oneClazzStudentCountBegin, oneClazzStudentCountEnd, oneGradeClazzCountBegin, oneGradeClazzCountEnd);
            // 如果是申请正式大使 执行判断
            if (StringUtils.isNotBlank(_from) && Objects.equals(_from, "apply")) {
                if (message.isSuccess()) {
                    // 申请成功 直接设置为大使 判断有没有同学科的大使
                    AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
                    if (ref == null) {
                        //直接为本校设置校园大使
                        message = businessTeacherServiceClient.setAmbassador(teacher);
                    } else {
                        return MapMessage.errorMessage("已经存在同学科校园大使");
                    }
                }
            }
            return message;
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 大使一键提醒未完成有效作业的老师
    @RequestMapping(value = "remindteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage remindTeacher() {
        try {
            TeacherDetail detail = currentTeacherDetail();
            if (!detail.isSchoolAmbassador()) {
                return MapMessage.errorMessage("对不起，你不是正式大使");
            }
            // 每个月10号才可以发起提醒
            if (DayRange.current().getDay() < 11) {
                return MapMessage.errorMessage("离月底还早，10号之后再来提醒老师吧！");
            }
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("AMBASSADOR_REMIND_TEACHER_FOR_EFFECT_HW")
                    .keys(currentUserId())
                    .proxy()
                    .remindTeacherForEffectHw(detail);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 大使一键点赞完成有效作业的老师
    @RequestMapping(value = "praiseteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage praiseTeacher() {
        try {
            TeacherDetail detail = currentTeacherDetail();
            if (!detail.isSchoolAmbassador()) {
                return MapMessage.errorMessage("对不起，你不是正式大使");
            }

            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("AMBASSADOR_PRAISE_TEACHER_FOR_EFFECT_HW")
                    .keys(currentUserId())
                    .proxy()
                    .praiseTeacherForEffectHw(detail);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 辞任大使
    @RequestMapping(value = "resignation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resignation() {
        try {
            TeacherDetail detail = currentTeacherDetail();
            if (!detail.isSchoolAmbassador()) {
                return MapMessage.errorMessage("对不起，你还不是校园大使");
            }
            String content = getRequestParameter("reason", "");
            if (StringUtils.isBlank(content)) {
                return MapMessage.errorMessage("请填写辞任理由");
            }
            UserFeedback feedback = new UserFeedback();
            feedback.setUserId(detail.getId());
            feedback.setUserType(detail.getUserType());
            feedback.setRealName(detail.getProfile().getRealname() == null ? detail.getProfile().getNickName() : detail.getProfile().getRealname());
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(detail.getId());
            feedback.setContactSensitivePhone(authentication != null ? authentication.getSensitiveMobile() : "");
            feedback.setContactSensitiveQq("");
            feedback.setContent(content);
            feedback.setFeedbackType("校园大使辞任理由");
            feedback.setIp(getWebRequestContext().getRealRemoteAddr());
            feedback.setExtStr1("");
            feedback.setExtStr2("");
            feedback.setFeedbackSubType1("");
            feedback.setFeedbackSubType2("");
            feedback.setPracticeType(0);
            feedback.setPracticeName("");
            feedback.setRefUrl("");
            feedbackServiceClient.getFeedbackService().saveFeedback(feedback);
            MapMessage message = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("AMBASSADOR_RESIGNATION")
                    .keys(detail.getId())
                    .proxy()
                    .resignationAmbassador(detail);
            if (message.isSuccess()) {
                String key = CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_WISH_ORDER, (String[]) null, new Object[]{detail.getId()});
                RewardCache.getRewardCache().delete(key);
                asyncUserServiceClient.getAsyncUserService()
                        .evictUserCache(detail.getId())
                        .awaitUninterruptibly();
            }
            return message;
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 大使获取积分明细 本月
    @RequestMapping(value = "ambassadorscorehistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ambassadorScoreHistory() {
        TeacherDetail teacher = currentTeacherDetail();
        if (!teacher.isSchoolAmbassador()) {
            return MapMessage.errorMessage();
        }
        // 正式大使获取积分明细 最近一个月
        List<Map<String, Object>> myScoreHistory = BusinessCache.getBusinessCache()
                .wrapCache(businessTeacherServiceClient)
                .expiration(DateUtils.getCurrentToDayEndSecond())
                .keyPrefix("AMBASSADOR_SCORE_HISTORY_MONTH")
                .keys(teacher.getId())
                .proxy()
                .loadAmbassadorScoreHistory(teacher.getId());
        return MapMessage.successMessage().add("myScoreHistory", myScoreHistory);
    }

    //生成校园大使微信二维码
    @RequestMapping(value = "qrcode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage qrcode() {
        MapMessage message = new MapMessage();
        try {
            String url = wechatCodeServiceClient.getWechatCodeService()
                    .generateQRCode(String.valueOf(currentUserId()), WechatType.AMBASSADOR)
                    .getUninterruptibly();
            message.setSuccess(true);
            message.add("qrcode_url", url);
        } catch (UtopiaRuntimeException ex) {
            logger.warn("生成二维码失败，msg:{}", ex.getMessage());
            message.setSuccess(false);
            message.setInfo("生成二维码失败");
        } catch (Exception ex) {
            logger.error("生成二维码失败,msg:{}", ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("生成二维码失败");
        }
        return message;
    }

    // 大使学院
    @RequestMapping(value = "collegeamb.vpage", method = RequestMethod.GET)
    public String ambCollege() {
        return "teacherv3/ambassador/collegeamb";
    }

    // 分分钟成为大使
    @RequestMapping(value = "collegeminute.vpage", method = RequestMethod.GET)
    public String ambMinuteCollege() {
        return "teacherv3/ambassador/collegeminute";
    }

    public MapMessage applySchoolAmbassador(User teacher,
                                            String name,
                                            String mobile,
                                            String qq,
                                            String email,
                                            String leader,
                                            Integer totalCount,
                                            Integer usingCount,
                                            String suggestion,
                                            String gender,
                                            String address,
                                            Integer englishCount,
                                            Integer mathCount,
                                            Integer chineseCount,
                                            Integer studentCount,
                                            Integer clazzCount,
                                            String eduSystemType,
                                            String source,
                                            String pname,
                                            String cname,
                                            String aname,
                                            Integer bYear,
                                            Integer tYear,
                                            Integer bMonth,
                                            Integer bDay,
                                            Boolean isFx,
                                            String fxClass,
                                            String schoolName,
                                            String schoolLevel,
                                            Integer oneClazzStudentCountBegin,
                                            Integer oneClazzStudentCountEnd,
                                            Integer oneGradeClazzCountBegin,
                                            Integer oneGradeClazzCountEnd) {
        if (!User.isTeacherUser(teacher)) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (!RealnameRule.isValidRealName(name)) {
            return MapMessage.errorMessage("姓名不正确");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        if (StringUtils.isBlank(qq) || !qq.matches("^\\d+$")) {
            return MapMessage.errorMessage("QQ不正确");
        }
        EduSystemType systemType = null;
        if (StringUtils.isNotBlank(eduSystemType)) {
            systemType = EduSystemType.of(eduSystemType);
        }
        SchoolAmbassadorSource ambassadorSource = SchoolAmbassadorSource.of(source);
        totalCount = totalCount == null ? 0 : totalCount;
        usingCount = usingCount == null ? 0 : usingCount;
        totalCount = Math.max(totalCount, 0);
        usingCount = Math.max(usingCount, 0);

        try {

            SchoolAmbassador ambassador = SchoolAmbassador.of(teacher.getId(), name, leader,
                    totalCount, usingCount, suggestion, gender, address, englishCount,
                    mathCount, chineseCount, studentCount, clazzCount, systemType, ambassadorSource,
                    pname, cname, aname, bYear, bMonth, bDay, tYear, isFx, fxClass, schoolName, schoolLevel,
                    oneClazzStudentCountBegin, oneClazzStudentCountEnd, oneGradeClazzCountBegin, oneGradeClazzCountEnd);

            ambassador.setSensitiveEmail(sensitiveUserDataServiceClient.encodeEmail(email));
            ambassador.setSensitiveMobile(sensitiveUserDataServiceClient.encodeMobile(mobile));
            ambassador.setSensitiveQq(sensitiveUserDataServiceClient.encodeQq(qq));

            return businessTeacherServiceClient.getRemoteReference().applySchoolAmbassador(ambassador);
        } catch (Exception ex) {
            logger.error("Failed to apply school ambassador", ex);
            return MapMessage.errorMessage("申请失败");
        }
    }
}
