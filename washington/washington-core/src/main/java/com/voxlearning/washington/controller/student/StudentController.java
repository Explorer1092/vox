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
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.monitor.ControllerMetric;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.UserPassword;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.service.campaign.client.WechatUserCampaignServiceClient;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.ExHomeworkComment;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.washington.controller.open.v1.teacher.TeacherConfigApiController;
import com.voxlearning.washington.service.homework.LoadHomeworkHelper;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@ControllerMetric
@Controller
@RequestMapping("/student")
public class StudentController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private LoadHomeworkHelper loadHomeworkHelper;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;
    @Inject private WechatCodeServiceClient wechatCodeServiceClient;
    @Inject private WechatUserCampaignServiceClient wechatUserCampaignServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;

    @ImportService(interfaceClass = VerificationService.class) private VerificationService verificationService;


    // 2014暑期改版 -- 学生端首页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent()) {
            return "redirect:" + ProductConfig.getUcenterUrl();
        }

        if (!getWebRequestContext().isHttpsRequest()) {
            // Chrome浏览器自动跳https
            String ua = getWebRequestContext().getRequest().getHeader("User-Agent");
            if (ua != null && ua.contains("Chrome") && !ProductDevelopment.isDevEnv()) {
                return "redirect:https://" + ProductConfig.getMainSiteBaseUrl().replace("http://", "") + "/student/index.vpage";
            }
            // redmine 36344
            // 湖北地区学生用户访问PC端自动切至HTTPS
            // 这里没有用灰度控制是因为灰度需要load StudentDetail，这个对象太大了，大量学生访问时会影响性能，所以直接在代码里hardcode
            if (studentDetail.getStudentSchoolRegionCode() != null && studentDetail.getStudentSchoolRegionCode().toString().startsWith("42")) {
                return "redirect:https://" + ProductConfig.getMainSiteBaseUrl().replace("http://", "") + "/student/index.vpage";
            }
        }
        Map indexData = businessStudentServiceClient.loadStudentIndexData2(studentDetail);

        Map map = new LinkedHashMap<>();
        map.putAll(indexData);

        //如果学生账号冻结,强制绑定手机号或再次验证手机号
        if (studentLoaderClient.isStudentFreezing(studentDetail.getId())) {
            String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(studentDetail.getId());
            map.put("mobile", mobile);
            map.put("vmpopup", true);
            map.put("force", true);
        } else if (studentLoaderClient.isStudentForbidden(studentDetail.getId())) {
            model.addAttribute("stuforbidden", true);
        }
        model.addAttribute("data", map);
        return "studentv3/index";
    }

    // 2014暑期改版 -- 学生端首页 -- 查看奖励卡
    @RequestMapping(value = "checkrewardcard.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage checkRewardCard() {
        Integer commentCount = getRequestInt("commentCount");
        StudentDetail studentDetail = currentStudentDetail();
        MapMessage mapMessage = checkStudentRewardsCard(studentDetail, commentCount > 0);
        //学生所在班级是否至少有一个认证老师
        boolean teacherCertification = false;
        List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(studentDetail.getClazzId());
        for (ClazzTeacher clazzTeacher : clazzTeachers) {
            Teacher teacher = clazzTeacher.getTeacher();
            if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
                teacherCertification = true;
                break;
            }
        }
        mapMessage.add("teacherCertification", teacherCertification);
        return mapMessage;
    }

    // 2014暑期改版 -- 强化学生记住密码 -- 数据提交
    @RequestMapping(value = "verifystudentpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyStudentPassword() {
        String token = getRequestParameter("token", "");
        String password = getRequestParameter("password", "");
        User student = currentUser();

        List<User> users = userLoaderClient.loadUsers(token, UserType.STUDENT);
        User user = MiscUtils.firstElement(users);
        if (user == null || !user.getId().equals(student.getId())) {
            return MapMessage.errorMessage().add("errorType", "WRONG_ACCOUNT");
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(student.getId());
        if (!new UserPassword(null, ua.getPassword(), ua.getSalt()).match(password)) {
            return MapMessage.errorMessage().add("errorType", "WRONG_PWD");
        }

        // 更新7天倒计时cookie
        updateLupld();
        return MapMessage.successMessage();
    }

    // 2014暑期改版 -- 左边栏推荐应用
    @RequestMapping(value = "appsList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recommendApp() {
        // FIXME: 先写成这个样子，应用管理稍后开发 @晓海
        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage();
        }

        // 白名单地区默认显示 阿分题、趣数、沃克、宠物
        // 付费黑名单 显示通天塔与pk
        List<Map<String, Object>> apps = new ArrayList<>();
        List<VendorApps> validApps = businessVendorServiceClient.getStudentPcAvailableApps(student.getId());

        final List<String> defaultAppNames = Arrays.asList(
                OrderProductServiceType.AfentiExam.name(),
                OrderProductServiceType.Stem101.name(),
                OrderProductServiceType.Walker.name(),
                OrderProductServiceType.PetsWar.name()
        );

        if (!student.isInPaymentBlackListRegion()) {  // 白名单用户
            validApps = validApps.stream().filter(p -> defaultAppNames.contains(p.getAppKey())).collect(Collectors.toList());
        }

        for (VendorApps app : validApps) {
            apps.add(MiscUtils.m("appName", app.getCname(), "appLink", "/student/apps/index.vpage?app_key=" + app.getAppKey(), "appImg", app.getAppKey()));
        }

        if (student.isInPaymentBlackListRegion()) {
            apps.add(MiscUtils.m("appName", "PK", "appLink", "/student/pk/index.vpage", "appImg", "pk"));
            apps.add(MiscUtils.m("appName", "通天塔", "appLink", "/student/babel/api/index.vpage", "appImg", "balel"));
        }

        return MapMessage.successMessage().add("apps", apps);
    }

    // 2014暑期改版 -- 判断学生是否有未完成的作业
    @RequestMapping(value = "homeworkState.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hasUndoHomework() {
        if (loadHomeworkHelper.hasUndoneHomework(currentStudentDetail())) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

    // 2014暑期改版 -- 新手任务卡 -- 送手机验证码
    // 仍有调用
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMobileCode() {
        try {
            String mobile = getRequest().getParameter("mobile");
            List<ClazzTeacher> teachers = userAggregationLoaderClient.loadStudentTeachers(currentUserId());

            if (CollectionUtils.isNotEmpty(teachers)) {
                Map<Long, UserAuthentication> userAuthentications = userLoaderClient.loadUserAuthentications(teachers
                        .stream()
                        .map(teacher -> teacher.getTeacher().getId())
                        .collect(Collectors.toSet())
                );

                boolean isMyTeacherMobile = false;
                for (UserAuthentication ua : userAuthentications.values()) {
                    if (ua.isMobileAuthenticated()) {
                        if (sensitiveUserDataServiceClient.mobileEquals(ua.getSensitiveMobile(), mobile))
                            isMyTeacherMobile = true;
                    }
                }
                if (isMyTeacherMobile) {
                    return MapMessage.errorMessage("不能绑定老师手机号！");
                }
            }
            return getSmsServiceHelper().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.STUDNET_VERIFY_MOBILE_NONAME_POPUP);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 新手任务卡 -- 验证手机
    @RequestMapping(value = "nonameverifymobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage noNameVeriyMobile() {
        String code = getRequestParameter("code", "");
        if (StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("信息不全");
        }

        MapMessage mapMessage = verificationService.verifyMobile(currentUserId(), code, SmsType.STUDNET_VERIFY_MOBILE_NONAME_POPUP.name());
        if (mapMessage.isSuccess() && currentUser().fetchUserType() == UserType.STUDENT && studentLoaderClient.isStudentFreezing(currentUser().getId())) {//冻结学生解冻时恢复冻结前班组
            studentServiceClient.freezeStudent(currentUserId(), false);
        }
        return mapMessage;
    }

    // 2014暑期改版 -- 学生端首页 -- 气泡信息
    @RequestMapping(value = "bubbles.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexBubble() {
        User student = currentUser();
        // UNREAD SYSTEM MESSAGE
        int unreadMessageCount = messageServiceClient.getMessageService().getUnreadMessageCount(student.narrow());
        //int unreadLetterCount = conversationLoaderClient.getConversationLoader().getUnreadLetterCount(student.getId());
        return MapMessage.successMessage().add("unreadTotalCount", unreadMessageCount);
    }

    // 2014暑期改版 -- 点击加入班级
    @RequestMapping(value = "clickjoinclazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clickJoinClazz() {
        return MapMessage.successMessage().add("captchaToken", RandomUtils.randomString(24));
    }

    // 2014暑期改版 -- 加入班级 -- 查看班级信息
    @RequestMapping(value = "checkclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findClazzInfo() {
        String id = getRequestString("id");
        String captchaToken = getRequestParameter("captchaToken", "");
        String captchaCode = getRequestParameter("captchaCode", "");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("请输入老师给你的号。");
        }
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码输入错误，请重新输入。");
        }
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        Teacher teacher = null;
        if (MobileRule.isMobile(id)) {// 输入的是手机号
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(id, UserType.TEACHER);
            if (ua != null) {
                teacher = teacherLoaderClient.loadTeacher(ua.getId());
            }
        } else {// 输入为老师id
            teacher = teacherLoaderClient.loadTeacher(SafeConverter.toLong(id));
        }

        if ((teacher == null || teacher.isDisabledTrue())) {
            return MapMessage.errorMessage("老师号错误！");
        }

        //1.15~APP上线前：需要禁止学生换班至O2O业务的老师名下
        //(老师号是初中数理化生老师or高中老师)且(老师注册时间大于2017年1月15 or 是快乐学导入老师) 且 (该老师与学生现在学校不一致 or 该学生目前无阅卷机号)
        boolean isSeniorTeacher = teacher.getKtwelve() == Ktwelve.SENIOR_SCHOOL;
        boolean isJuniorTeacher = teacher.getKtwelve() == Ktwelve.JUNIOR_SCHOOL && (teacher.getSubject() == Subject.PHYSICS || teacher.getSubject() == Subject.CHEMISTRY || teacher.getSubject() == Subject.BIOLOGY);
        if (isJuniorTeacher || isSeniorTeacher) {
            KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(studentDetail.getId());
            School teacherSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacher.getId())
                    .getUninterruptibly();
            boolean isSameSchool = studentDetail.getClazz() != null && teacherSchool != null && (Objects.equals(studentDetail.getClazz().getSchoolId(), teacherSchool.getId()));
            boolean hasScanNumber = klxStudent != null && StringUtils.isNotBlank(klxStudent.getScanNumber());
            if (!isSameSchool || !hasScanNumber) {
                return MapMessage.errorMessage("暂不支持换班至组卷业务老师名下哦");
            }
        }

        Set<Ktwelve> allowedKtwelves = null;
        if (studentDetail.getClazz() == null) {
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.PRIMARY_SCHOOL, Ktwelve.JUNIOR_SCHOOL, Ktwelve.SENIOR_SCHOOL, Ktwelve.INFANT));
        } else if (studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent()) {//小学毕业学生
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.PRIMARY_SCHOOL, Ktwelve.JUNIOR_SCHOOL));
        } else if (studentDetail.isPrimaryStudent() && studentDetail.getClazzLevel() == ClazzLevel.SIXTH_GRADE && isPreTerminalPeriod()) {//小学毕业学生)
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.PRIMARY_SCHOOL, Ktwelve.JUNIOR_SCHOOL));
        } else if (studentDetail.getClazz().isTerminalClazz() && studentDetail.isJuniorStudent()) {//中学毕业学生
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.JUNIOR_SCHOOL, Ktwelve.SENIOR_SCHOOL));
        } else if (studentDetail.getClazz().isTerminalClazz() && studentDetail.isInfantStudent()) {//学前毕业学生
            allowedKtwelves = new HashSet<>(Arrays.asList(Ktwelve.INFANT, Ktwelve.PRIMARY_SCHOOL));
        } else {
            allowedKtwelves = Collections.singleton(studentDetail.getClazz().getEduSystem().getKtwelve());
        }

        MapMessage result = studentSystemClazzServiceClient
                .joinClazz_findClazzInfo(id, currentUserId(), allowedKtwelves);

        result.remove("teacher");
        return result;
    }

    // flash调用此方法
    @RequestMapping(value = "userinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map userinfo() {
        StudentDetail student = currentStudentDetail();
        ExRegion region = raikouSystem.loadRegion(student.getStudentSchoolRegionCode());
        boolean inAfentiTrialRegion = region != null && region.containsTag(RegionConstants.TAG_AFENTI_TRIAL_REGIONS);

        AppPayMapper mapper = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.AfentiExam.name(), student.getId());
        boolean hasPaidAfentiOrder = mapper != null && mapper.hasPaid();
        HashMap<String, Object> beta = new HashMap<>();
        beta.put("inAfentiTrialRegion", inAfentiTrialRegion);
        beta.put("classLevel", student.getClazzLevelAsInteger());
        beta.put("hasPaidAfentiOrder", hasPaidAfentiOrder);
        beta.put("hasPaidAdventureIslandOrder", false);

        Clazz clazz = student.getClazz();
        String clazzName = clazz == null ? "" : clazz.formalizeClazzName();
        return MiscUtils.map()
                .add("id", student.getId())
                .add("name", student.getProfile().getRealname())
                .add("clazzName", clazzName)
                .add("avatar", getUserAvatarImgUrl(student))
                .add("schoolName", StringUtils.defaultString(student.getStudentSchoolName()))
                .add("beta", beta)
                .add("success", true);
    }

    @RequestMapping(value = "qrcode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage arcode() {
        MapMessage message = new MapMessage();
        try {
            Integer campaignId = getRequestInt("campaignId", 0);
            wechatUserCampaignServiceClient.getWechatUserCampaignService().setUserCampaign(currentUserId(), campaignId).get();

            Integer wechatType = getRequestInt("t", 0);//默认0，家长端微信
            String url = wechatCodeServiceClient.getWechatCodeService()
                    .generateQRCode(currentUserId().toString(), WechatType.of(wechatType))
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

    @RequestMapping(value = "collectstudentschoolinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage collectStudentSchoolInfo(@RequestBody Map<String, Object> info) {

        int clazzNum = SafeConverter.toInt(info.get("clazzNum"));
        if (clazzNum == 0) {
            return MapMessage.errorMessage("年级数错误");
        }

        int studentNum = SafeConverter.toInt(info.get("studentNum"));
        if (studentNum == 0) {
            return MapMessage.errorMessage("学生数错误");
        }

        int englishClazzNum = SafeConverter.toInt(info.get("englishClazzNum"));
        if (englishClazzNum == 0) {
            return MapMessage.errorMessage("英语老师带班数错误");
        }

        int mathClazzNum = SafeConverter.toInt(info.get("mathClazzNum"));
        if (mathClazzNum == 0) {
            return MapMessage.errorMessage("数学老师带班数错误");
        }

        StudentDetail student = currentStudentDetail();

        try {
            StudentSchoolInfoCollection studentSchoolInfoCollection = new StudentSchoolInfoCollection();

            // 年级
            List<String> grades = (List<String>) info.get("grades");
            String gradeStr = String.join(",", grades);
            studentSchoolInfoCollection.setSchoolGrades(gradeStr);

            studentSchoolInfoCollection.setClazzId(student.getClazzId());
            studentSchoolInfoCollection.setClazzNum(clazzNum);
            studentSchoolInfoCollection.setStudentId(student.getId());
            studentSchoolInfoCollection.setStudentNum(studentNum);
            studentSchoolInfoCollection.setStudentGrade(student.getClazz().getClazzLevel().getLevel());
            studentSchoolInfoCollection.setSchoolId(student.getClazz().getSchoolId());
            studentSchoolInfoCollection.setEnglishTeacherClazzNum(englishClazzNum);
            studentSchoolInfoCollection.setMathTeacherClazzNum(mathClazzNum);

            studentServiceClient.saveStudentCollection(studentSchoolInfoCollection);
        } catch (Exception e) {
            logger.error("collect student school info failed: {}", e);
            return MapMessage.errorMessage("保存信息错误");
        }
        return MapMessage.successMessage();
    }

    private MapMessage checkStudentRewardsCard(StudentDetail student, boolean hasComment) {
        if (student == null) {
            return MapMessage.errorMessage("信息获取失败");
        }
        Long studentId = student.getId();
        List<IntegralHistory> histories = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .StudentRewardCardIntegralHistoryCache_load(studentId, true)
                .getUninterruptibly();
        List<ExHomeworkComment> comments = new ArrayList<>();
        if (hasComment) { // 如果有未读评语，获取
            comments.addAll(homeworkCommentLoaderClient.studentUnreadHomeworkComments(student));
        }
        return MapMessage.successMessage().add("histories", histories).add("comments", comments);
    }

    // 收集学生关于老师举报的反馈
    @RequestMapping(value = "collectreportinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage collectReportInfo(@RequestBody Map<String, Object> info) {
        Long englishId = SafeConverter.toLong(info.get("englishId"));
        Long mathId = SafeConverter.toLong(info.get("mathId"));
        boolean englishFlag = SafeConverter.toBoolean(info.get("englishFlag"));
        boolean mathFlag = SafeConverter.toBoolean(info.get("mathFlag"));
        if (englishId == 0 && mathId == 0) {
            return MapMessage.errorMessage("请选择答案");
        }
        try {
            return atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .keys(currentUserId())
                    .proxy()
                    .collectAmbassadorReportFeedback(englishId, mathId, englishFlag, mathFlag, currentUserId());
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN COLLECT AMBASSADOR REPORT FEEDBACK {}, THE ERROR MESSAGE IS: {}", currentUserId(), ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重新操作");
        }
    }

    // 获取学生是否有家长号
    @RequestMapping(value = "hasparent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage hasParent() {
        Long userId = currentUserId();
        boolean hasParent = CollectionUtils.isNotEmpty(studentLoaderClient.loadStudentParentRefs(userId));
        return MapMessage.successMessage().add("hasParent", hasParent);
    }

    // 强绑家长通 发送下载方式
    @RequestMapping(value = "sendbindjzt.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sendBindJzt() {
        try {
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(currentUserId());
            if (authentication == null || !authentication.isMobileAuthenticated()) {
                return MapMessage.errorMessage("请先绑定手机");
            }
            String cacheKey = "D_L_J_N_C:" + currentUserId();
            String dayCount = CacheSystem.CBS.getCache("unflushable").load(cacheKey);
            if (StringUtils.isNotBlank(dayCount) && SafeConverter.toInt(dayCount) >= 3) {
                return MapMessage.errorMessage("超过发送次数");
            }
            String content = "欢迎使用一起作业网，请家长下载并登录家长通，孩子才可以使用，点击 " +
                    TeacherConfigApiController.i7TinyUrl("http://wx.17zuoye.com/download/17parentapp?cid=202013") + " 立即下载";
            userSmsServiceClient.buildSms().to(authentication).content(content).type(SmsType.DOWN_JZT_NOTICE).send();
            CacheSystem.CBS.getCache("unflushable").incr(cacheKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("发送失败");
        }
    }

    // 学生首页 强制验证手机获取验证码
    @RequestMapping(value = "sendvmmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendValidateMobileCode() {
        try {
            Long userId = currentUserId();
            String mobile = getRequest().getParameter("mobile");
            // 获取用户绑定的手机号码
            String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(userId);
            if (authenticatedMobile != null && !StringUtils.equals(authenticatedMobile, mobile)) {
                return MapMessage.errorMessage("请输入正确的手机号码");
            }
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.STUDENT_VALIDATE_MOBILE_POPUP.name(), false);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 学生首页 强制验证手机 验证
    @RequestMapping(value = "validatemobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateMobile() {
        String mobile = getRequestParameter("mobile", "");
        String code = getRequestParameter("code", "");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("信息不全");
        }
        MapMessage mapMessage = verificationService.verifyMobile(currentUserId(), mobile, code, SmsType.STUDENT_VALIDATE_MOBILE_POPUP.name());
        if (mapMessage.isSuccess() && currentUser().fetchUserType() == UserType.STUDENT && studentLoaderClient.isStudentFreezing(currentUser().getId())) {//冻结学生解冻时恢复冻结前班组
            studentServiceClient.freezeStudent(currentUserId(), false);
        }
        return mapMessage;
    }
}
