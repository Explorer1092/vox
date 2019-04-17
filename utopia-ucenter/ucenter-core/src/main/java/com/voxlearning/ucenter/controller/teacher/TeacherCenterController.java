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

package com.voxlearning.ucenter.controller.teacher;

import com.nature.commons.lang.util.StringUtil;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.ValidateEmailSender;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.TeacherLevelName;
import com.voxlearning.utopia.core.ArgMapper;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.api.mapper.EmailReceiptor;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.LogisticType;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.UserShippingAddressMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.UserEmailServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.DEDUCT_TEACHER_EXPIRED_INTEGRAL;

/**
 * teacher center
 *
 * @author changyuan.liu
 * @since 2015/12/10.
 */
@Controller
@RequestMapping("/teacher/center")
public class TeacherCenterController extends AbstractWebController {

    private static final int TEACHER_INTEGRAL_PAGE_SIZE = 5;

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private ValidateEmailSender validateEmailSender;

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private UserEmailServiceClient userEmailServiceClient;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;
    @ImportService(interfaceClass = VerificationService.class) private VerificationService verificationService;

    // 2014暑期改版 -- 教师个人中心 -- 基本信息
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        TeacherDetail teacher = currentTeacherDetail();

        //是否是神算子
        model.addAttribute("isShensz", teacher.isShensz());

        // 判断用户是否需要修改真实名称
        boolean supplementName = false;
        if (teacher.isPrimarySchool()) {
            supplementName = StringUtil.isEmpty(teacher.getProfile().getRealname());
        } else {
            supplementName = StringRegexUtils.isNotRealName(teacher.getProfile().getRealname());
        }
        model.addAttribute("isSupplementName", supplementName);

        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/center/kuailexue/index";
        } else {
            if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
                return getPlatformWebRedirectStr(teacher.getKtwelve(), "/teacher/index.vpage", (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()));
            }
            model.addAttribute("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(teacher.getId()));
            model.addAttribute("email", sensitiveUserDataServiceClient.loadUserEmailObscured(teacher.getId()));

            MapMessage message = userServiceClient.generateUserShippingAddress(teacher.getId());
            model.addAttribute("userShippingAddressMapper", message.get("address"));

            // 隐私设置 - 自己的作业动态不显示在同校老师的时间轴
            UserExtensionAttribute uea = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                    .type(UserExtensionAttributeKeyType.CLOSE_HOMEWORK_NEWS)
                    .findFirst();
            if (uea != null && SafeConverter.toBoolean(uea.getExtensionAttributeValue())) {
                model.addAttribute("closeHomeworkNews", true);
            } else {
                model.addAttribute("closeHomeworkNews", false);
            }
            return "teacherv3/center/index";
        }
    }

    @RequestMapping(value = "basicinfo.vpage", method = RequestMethod.GET)
    public String personalInfo(Model model) {
        TeacherDetail teacher = currentTeacherDetail();

        //是否是神算子
        model.addAttribute("isShensz", teacher.isShensz());

        if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
            return getPlatformWebRedirectStr(teacher.getKtwelve(), "/teacher/index.vpage", (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()));
        }
        model.addAttribute("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(teacher.getId()));
        model.addAttribute("email", sensitiveUserDataServiceClient.loadUserEmailObscured(teacher.getId()));

        MapMessage message = userServiceClient.generateUserShippingAddress(teacher.getId());
        model.addAttribute("userShippingAddressMapper", message.get("address"));

        // 隐私设置 - 自己的作业动态不显示在同校老师的时间轴
        UserExtensionAttribute uea = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                .type(UserExtensionAttributeKeyType.CLOSE_HOMEWORK_NEWS)
                .findFirst();
        if (uea != null && SafeConverter.toBoolean(uea.getExtensionAttributeValue())) {
            model.addAttribute("closeHomeworkNews", true);
        } else {
            model.addAttribute("closeHomeworkNews", false);
        }
        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/center/kuailexue/basicinfo";
        } else {
            return "teacherv3/center/basicinfo";
        }
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的资料
    @RequestMapping(value = "myprofile.vpage", method = RequestMethod.GET)
    public String myProfile(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        try {
            if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
                return getPlatformWebRedirectStr(teacher.getKtwelve(), "/teacher/index.vpage", (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()));
            }

            String am = sensitiveUserDataServiceClient.loadUserMobileObscured(teacher.getId());
            String ae = sensitiveUserDataServiceClient.loadUserEmailObscured(teacher.getId());
            model.addAttribute("mobile", am);
            model.addAttribute("email", ae);

            List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
            List<ArgMapper> provinces = new ArrayList<>(regionList.size());
            for (Region region : regionList) {
                provinces.add(new ArgMapper(region.getCode(), region.getName()));
            }
            model.addAttribute("provinces", provinces);
            MapMessage message = userServiceClient.generateUserShippingAddress(teacher.getId());
            model.addAttribute("userShippingAddressMapper", message.get("address"));
            model.addAttribute("backUrl", getRequest().getParameter("backurl"));
            model.addAttribute("receiverPhone", message.get("receiverPhone"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/center/kuailexue/myprofile";
        } else {
            return "teacherv3/center/myprofile";
        }
    }

    // 2014暑期改版 -- 教师个人中心 -- 提交修改我的资料
    @RequestMapping(value = "modifyprofile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyProfile(@RequestBody final UserShippingAddressMapper command) {
        Teacher user = currentTeacher();

        // 参数校验
        MapMessage validateMessage = this.validate(command, user);
        if (!validateMessage.isSuccess()) {
            return validateMessage;
        }

        if (badWordCheckerClient.containsUserNameBadWord(command.getUserName())) {
            return MapMessage.errorMessage("姓名请不要使用敏感词汇!");
        }

        // 更新名字
        if (!StringUtils.equals(command.getUserName(), user.fetchRealname())) {
            // Feature #54929
//            if (ForbidModifyNameAndPortrait.check()) {
//                return ForbidModifyNameAndPortrait.errorMessage;
//            }
            if (!userServiceClient.changeName(user.getId(), command.getUserName()).isSuccess()) {
                return MapMessage.errorMessage("个人信息更新失败！");
            }

            LogCollector.info("backend-general", MiscUtils.map("usertoken", user.getId(),
                    "usertype", user.getUserType(),
                    "platform", "pc",
                    "version", "",
                    "op", "change user name",
                    "mod1", user.fetchRealname(),
                    "mod2", command.getUserName(),
                    "mod3", user.getAuthenticationState()));
        }

        if (!user.isKLXTeacher() || user.isOldJuniorTeacher()) {
            // 更新收货地址
            UserShippingAddress usa = new UserShippingAddress();
            usa.setId(command.getId());
            usa.setDisabled(false);
            usa.setUserId(user.getId());
            usa.setProvinceCode(command.getProvinceCode());
            usa.setProvinceName(command.getProvinceName());
            usa.setCityCode(command.getCityCode());
            usa.setCityName(command.getCityName());
            usa.setCountyCode(command.getCountyCode());
            usa.setCountyName(command.getCountyName());
            usa.setDetailAddress(StringUtils.cleanXSS(command.getDetailAddress()));
            usa.setPostCode(command.getPostCode());
            usa.setSchoolName(command.getSchoolName());
            usa.setSchoolId(command.getSchoolId());
            usa.setLogisticType(ObjectUtils.get(() -> LogisticType.valueOf(command.getLogisticType()), LogisticType.notavailable));
            usa.setReceiver(StringUtils.filterEmojiForMysql(command.getReceiver()));
            usa.setReceiverPhone(command.getReceiverPhone());
            if (!userServiceClient.updateUserShippingAddress(usa).isSuccess()) {
                return MapMessage.errorMessage("个人信息更新失败！");
            }
        }

        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(user.getId());
        if (userAuthentication != null && userAuthentication.isEmailAuthenticated()) {
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("name", StringUtils.defaultString(command.getUserName()));
            String area = StringUtils.defaultString(command.getProvinceName()) + StringUtils.defaultString(command.getCityName()) + StringUtils.defaultString(command.getCountyName());
            content.put("area", area);
            content.put("school", StringUtils.defaultString(command.getSchoolName()));
            content.put("address", StringUtils.defaultString(command.getDetailAddress()));
            content.put("zipcode", StringUtils.defaultString(command.getPostCode()));
            content.put("hotline", Constants.HOTLINE_SPACED);
            content.put("date", DateUtils.dateToString(new Date(), FORMAT_SQL_DATE));
            content.put("time", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 HH点mm分"));

            userEmailServiceClient.buildEmail(EmailTemplate.teachermodifypersonalinformation)
                    .to(userAuthentication)
                    .subject("您已更改在一起作业的个人资料")
                    .content(content)
                    .send();
        }

        return MapMessage.successMessage("更新个人信息成功！");
    }

    // 2014暑期改版 -- 教师个人中心 -- 账号安全
    @RequestMapping(value = "securitycenter.vpage", method = RequestMethod.GET)
    public String securityInformation(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
            return "redirect:/teacher/index.vpage";
        }

        model.addAttribute("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(teacher.getId()));
        model.addAttribute("email", sensitiveUserDataServiceClient.loadUserEmailObscured(teacher.getId()));
        model.addAttribute("qq", thirdPartyLoaderClient.loadLandingSource(teacher.getId(), SsoConnections.QQ.getSource()));
        model.addAttribute("pandaria", needForceModifyPassword(teacher.getTeacherSchoolId(), teacher));

        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/center/kuailexue/securitycenter";
        } else {
            return "teacherv3/center/securitycenter";
        }
    }

    /**
     * 老师修改密码，发送短信验证码
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "sendTCPWcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTeacherChangePasswordCode() {
        String phone = sensitiveUserDataServiceClient.loadUserMobile(currentUserId());
        if (phone == null) {
            return MapMessage.errorMessage("请先绑定手机");
        }
        if (!MobileRule.isMobile(phone)) {
            return MapMessage.errorMessage("错误的手机号");
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), phone, SmsType.TEACHER_CHANGE_PASSWORD.name());
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的等级
    @RequestMapping(value = "mylevel.vpage", method = RequestMethod.GET)
    public String myLevel(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
            return getPlatformWebRedirectStr(teacher.getKtwelve(), "/teacher/index.vpage", (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()));
        }

        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
        int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());
        int teacherLevelValue = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevelValue());
        TeacherLevelName teacherLevelName = extAttribute == null ? TeacherLevelName.ANONYMOUS : extAttribute.fetchTeacherLevel();
        model.addAttribute("teacherLevel", teacherLevel);
        model.addAttribute("teacherLevelValue", teacherLevelValue);
        model.addAttribute("teacherLevelName", teacherLevelName);

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId())
                .getUninterruptibly();
        if (school == null) {
            return "teacherv3/center/mylevel";
        }

        //直接从缓存获取学校老师排行榜数据
        List<Map<String, Object>> teacherLevelRank = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .SchoolTeacherLevelRankManager_load(school.getId())
                .getUninterruptibly();

        if (CollectionUtils.isEmpty(teacherLevelRank)) {
            teacherLevelRank = getSchoolTeacherLevelRank(school);
            asyncUserCacheServiceClient.getAsyncUserCacheService().SchoolTeacherLevelRankManager_set(school.getId(), teacherLevelRank)
                    .awaitUninterruptibly();
        }

        model.addAttribute("rankList", teacherLevelRank);
        //取老师排名
        for (int i = 0; i < teacherLevelRank.size(); i++) {
            if (teacherLevelRank.get(i).get("ID").toString().equals(teacher.getId().toString())) {
                model.addAttribute("rank", i + 1);
                break;
            }
        }
        //是否完成布置作业增加活跃值  今天
        long c1 = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .getAddLevelFromAssignHomeworkCount(teacher.getId())
                .getUninterruptibly();
        model.addAttribute("arrangeHomeWorkFlag", c1 >= 1);
        //是否完成智慧课堂奖励学生增加活跃值  今天
        long c2 = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .getAddLevelFromRewardStudentCount(teacher.getId())
                .getUninterruptibly();
        model.addAttribute("rewardStudentFlag", c2 >= 1);
        //距离升级还需多少活跃值
        int nextLevelValue = (teacherLevel + 1) * (teacherLevel + 1) * 10 + 40 * (teacherLevel + 1);
        model.addAttribute("upValue", nextLevelValue - teacherLevelValue);
        return "teacherv3/center/mylevel";
    }

    // 2014暑期改版 -- 教师个人中心 -- 发送绑定手机验证码
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMobileCode() {
        try {
            String mobile = getRequest().getParameter("mobile");
            return smsServiceHelper.sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.TEACHER_VERIFY_MOBILE_CENTER);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 教师个人中心 -- 验证手机
    @RequestMapping(value = "validatemobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitRebindMobile() {
        try {
            Long teacherId = currentUserId();
            String code = getRequest().getParameter("latestCode");
            MapMessage message = verificationService.verifyMobile(teacherId, code, SmsType.TEACHER_VERIFY_MOBILE_CENTER.name());
            return message;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    // 2014暑期改版 -- 教师个人中心 -- 发送验证邮件
    @RequestMapping(value = "sendvalidateEmail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendValidateEmail() {
        User teacher = currentUser();
        if (teacher == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String email = getRequestParameter("email", "");
        if (userLoaderClient.loadEmailAuthentication(email) != null) {
            return MapMessage.errorMessage("该邮箱已被验证, 请更换邮箱");
        }
        EmailReceiptor receiptor = new EmailReceiptor();
        receiptor.setUserId(teacher.getId());
        receiptor.setRealname(teacher.fetchRealname());
        receiptor.setEmail(email);

        String ucenterUrl = ProductConfig.getUcenterUrl();
        return validateEmailSender.sendValidateEmail(receiptor, ucenterUrl);
    }

    // 2014暑期改版 -- 解绑
    @RequestMapping(value = "unbindsso.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unbindSso() {
        Long id = getRequestLong("id");
        try {
            if (thirdPartyService.unbindLandingSource(id, currentUserId())) {
                return MapMessage.successMessage();
            }
        } catch (Exception e) {
            logger.error("UNBIND LANDING SOURCE BY ID FAILED: ID [{}]", id, e);
        }
        return MapMessage.errorMessage("解绑失败");
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的金币
    @RequestMapping(value = "mygold.vpage", method = RequestMethod.GET)
    public String integral(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
            return "redirect:/teacher/index.vpage";
        }

        boolean show = !Arrays.asList("1", "2", "7", "8").contains(DateUtils.dateToString(new Date(), "M"));
        model.addAttribute("show", show);

        if (show) {
            // 过期积分 = 当前剩余积分 - 最近12个月或的积分（当月算一个月，奖品中心退款不算）
            long expired = 0;
            UserIntegral ui = teacher.getUserIntegral();
            if (ui != null && ui.getUsable() > 0) {
                long sum = integralHistoryLoaderClient.getIntegralHistoryLoader()
                        .sumLatestTwelveMonthsIntegralHistoriesExcludeNegativeAnd45(teacher.getId());
                if (teacher.isPrimarySchool() || teacher.isInfantTeacher()) {
                    expired = Math.max(0, ui.getUsable() - sum / 10);
                } else {
                    expired = Math.max(0, ui.getUsable() - sum);
                }
            }
            model.addAttribute("expired", expired);
            model.addAttribute("date", DateUtils.dateToString(MonthRange.current().getEndDate(), FORMAT_SQL_DATE));

            MonthRange mr = MonthRange.current();
            String current = DateUtils.dateToString(mr.getStartDate(), "M");
            String month = DateUtils.dateToString(mr.getStartDate(), DateUtils.FORMAT_SQL_DATE);

            List<Long> allTeacherIds = new ArrayList<>();
            allTeacherIds.add(teacher.getId());

            // 包班制的老师，连副账号的一起算上
            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacher.getId());
            allTeacherIds.addAll(subTeacherIds);

            // 本月是否免除
            model.addAttribute("count", asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherExpiredIntegralFreeCacheManager_fetchCount(allTeacherIds, month)
                    .getUninterruptibly()
                    .values()
                    .stream()
                    .mapToInt(t -> t)
                    .sum());

            // 上月是否扣除，如果是3月份，查询123月的积分历史，如果是9月份，查询789月的历史，其余只需要查当月的
            Date date = mr.getStartDate();
            if (Arrays.asList("3", "9").contains(current)) date = mr.previous().previous().getStartDate();
            final long timestamp = date.getTime();

            // 查询积分历史
            IntegralHistory history = integralHistoryLoaderClient.getIntegralHistoryLoader()
                    .loadUserIntegralHistories(teacher.getId())
                    .stream()
                    .filter(h -> h.getCreatetime().getTime() >= timestamp)
                    .filter(h -> h.getIntegralType() == DEDUCT_TEACHER_EXPIRED_INTEGRAL.getType())
                    .findFirst()
                    .orElse(null);
            model.addAttribute("last", history != null);
        }

        return "teacherv3/center/mygold";
    }

    @RequestMapping(value = "mygoldchip.vpage", method = RequestMethod.GET)
    public String integralChip(Model model) {
        int pageNumber = getRequestInt("pageNumber", 1);
        int pageSize = getRequestInt("pageSize", TEACHER_INTEGRAL_PAGE_SIZE);
        boolean ge0 = getRequestBool("ge0", true);
        // 获取金币前三个月的历史数据
        UserIntegralHistoryPagination pagination = userLoaderClient
                .loadUserIntegralHistories(currentUser(), 3, pageNumber - 1, pageSize, ge0);
        model.addAttribute("pagination", pagination);
        model.addAttribute("integral", pagination.getUsableIntegral());
        model.addAttribute("currentPage", pageNumber);
        return "teacherv3/center/mygoldchip";
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的认证
    @RequestMapping(value = "myauthenticate.vpage", method = RequestMethod.GET)
    public String myAuthenticate(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        try {
            if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
                return "redirect:/teacher/index.vpage";
            }
            AuthenticationState as = AuthenticationState.safeParse(teacher.getAuthenticationState());
            switch (as) {
                case AGAIN:
                case WAITING:
                    model.addAttribute("state", "WAITING");
                    break;
                case FAILURE:
                    model.addAttribute("state", "FAILURE");
                    break;
                case SUCCESS:
                    model.addAttribute("state", "SUCCESS");
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/center/kuailexue/myauthenticate";
        } else {
            return "teacherv3/center/myauthenticate";
        }
    }

    @RequestMapping(value = "sendApplication2Admin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendApplication2Admin() {
        User user = currentUser();

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(user.getId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage();
        }

        List<Teacher> teachers = teacherLoaderClient.loadSchoolQuizBankAdmin(school.getId());
        if (CollectionUtils.isEmpty(teachers)) {
            return MapMessage.errorMessage();
        }

        String teacherNames = teachers.stream().map(User::fetchRealname).collect(Collectors.joining(", "));

        // 检查是否已经发送过申请
        Date applyTime = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .KLXTeacherApplyAuthCacheManager_getApplyTime(user.getId())
                .getUninterruptibly();
        if (applyTime != null) {
            return MapMessage.successMessage("已发送过申请给管理员(" + teacherNames + "),请耐心等候");
        }

        String payload = user.fetchRealname() + "老师申请获得认证，前去处理  <a href=\"" + ProductConfig.getKuailexueUrl() + "/math/school_bank/items\">[点这里]</a>\n" +
                "<br>温馨提示：获得认证后的老师具有使用校本题库的权限，请谨慎审核";
        teachers.forEach(t -> teacherLoaderClient.sendTeacherMessage(t.getId(), payload));

        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .KLXTeacherApplyAuthCacheManager_applyAuth(user.getId())
                .awaitUninterruptibly();

        return MapMessage.successMessage("您的认证申请已成功发送给管理员" + teacherNames + "，请耐心等候");
    }

    @RequestMapping(value = "updatehwnews.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage updateHomeworkNews() {
        boolean homeworkNews = getRequestBool("homeworkNews");
        Long userId = currentUserId();
        try {
            UserExtensionAttribute uea = userAttributeLoaderClient.loadUserExtensionAttributes(userId)
                    .type(UserExtensionAttributeKeyType.CLOSE_HOMEWORK_NEWS)
                    .findFirst();
            if (uea == null) {
                if (homeworkNews) {
                    uea = new UserExtensionAttribute();
                    uea.setUserId(userId);
                    uea.setExtensionAttributeKey(UserExtensionAttributeKeyType.CLOSE_HOMEWORK_NEWS.name());
                    uea.setExtensionAttributeValue(String.valueOf(Boolean.TRUE));
                    userAttributeServiceClient.setExtensionAttribute(userId,
                            UserExtensionAttributeKeyType.CLOSE_HOMEWORK_NEWS.name(), "true");
                }
            } else {
                userAttributeServiceClient.setExtensionAttribute(uea.getUserId(), uea.getKey(), String.valueOf(homeworkNews));
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("Update close homework news failed: USER ID [{}]", userId, e);
        }

        return MapMessage.errorMessage("更新作业动态不显示在同校老师的时间轴失败！");
    }

    /**
     * ************************************************* 私有方法 ****************************************************
     */

    // 私有方法--校验对象
    private MapMessage validate(UserShippingAddressMapper command, Teacher teacher) {
        MapMessage mesg = new MapMessage();

        String teacherName = command.getUserName();
        Long provinceCode = command.getProvinceCode();
        String provinceName = command.getProvinceName();
        Long cityCode = command.getCityCode();
        String cityName = command.getCityName();
        Long countyCode = command.getCountyCode();
        String countyName = command.getCountyName();
        String detailAddress = command.getDetailAddress();
        String schoolName = command.getSchoolName();

        if (StringUtils.isBlank(teacherName)) {
            mesg.setInfo("用户不存在, 更新个人信息失败");
            mesg.setSuccess(false);
            return mesg;
        }
        if (!RealnameRule.isValidRealName(teacherName)) {
            mesg.setInfo("请输入您的真实姓名,须为中文");
            mesg.setSuccess(false);
            return mesg;
        }
        if (!teacher.isKLXTeacher() && !teacher.isJuniorMathTeacher()) {
            if (StringUtils.isBlank(provinceName) || null == provinceCode) {
                mesg.setSuccess(false);
                mesg.setInfo("请选择省份！");
                return mesg;
            }
            if (StringUtils.isBlank(cityName) || null == cityCode) {
                mesg.setSuccess(false);
                mesg.setInfo("请选择城市！");
                return mesg;
            }
            if (StringUtils.isBlank(countyName) || null == countyCode) {
                mesg.setSuccess(false);
                mesg.setInfo("请选择区县！");
                return mesg;
            }
            if (StringUtils.isBlank(detailAddress)) {
                mesg.setSuccess(false);
                mesg.setInfo("请填写详细地址！");
                return mesg;
            }
            if (StringUtils.isBlank(schoolName)) {
                mesg.setSuccess(false);
                mesg.setInfo("请选择学校！");
                return mesg;
            }
        }
        mesg.setSuccess(true);
        mesg.setInfo("校验通过");
        return mesg;
    }

    private List<Map<String, Object>> getSchoolTeacherLevelRank(School school) {
        List<Teacher> schoolTeachers = teacherLoaderClient.loadSchoolTeachers(school.getId());
        schoolTeachers = schoolTeachers.stream()
                .filter(p -> p.fetchCertificationState() == AuthenticationState.SUCCESS)
                .filter(p -> teacherLoaderClient.loadMainTeacherId(p.getId()) != null)
                .collect(Collectors.toList());
        Set<Long> teacherIds = schoolTeachers.stream().map(Teacher::getId).collect(Collectors.toSet());
        Map<Long, TeacherExtAttribute> teaMap = teacherLoaderClient.loadTeacherExtAttributes(teacherIds);

        Collections.sort(schoolTeachers, (o1, o2) -> {
            int level1 = teaMap.containsKey(o1.getId()) ? SafeConverter.toInt(teaMap.get(o1.getId()).getLevel()) : 0;
            int level2 = teaMap.containsKey(o2.getId()) ? SafeConverter.toInt(teaMap.get(o2.getId()).getLevel()) : 0;
            if (level1 != level2) {
                return Integer.compare(level2, level1);
            }

            int levelScore1 = teaMap.containsKey(o1.getId()) ? SafeConverter.toInt(teaMap.get(o1.getId()).getLevelValue()) : 0;
            int levelScore2 = teaMap.containsKey(o2.getId()) ? SafeConverter.toInt(teaMap.get(o2.getId()).getLevelValue()) : 0;

            return Integer.compare(levelScore2, levelScore1);
        });

        List<Map<String, Object>> teacherLevelRank = new ArrayList<>();
        for (Teacher teacherInfo : schoolTeachers) {
            Map<String, Object> item = new HashMap<>();
            item.put("ID", teacherInfo.getId());
            item.put("REALNAME", teacherInfo.fetchRealname());
            item.put("LEVEL", teaMap.containsKey(teacherInfo.getId()) ? SafeConverter.toInt(teaMap.get(teacherInfo.getId()).getLevel()) : 0);
            item.put("LEVEL_VALUE", teaMap.containsKey(teacherInfo.getId()) ? SafeConverter.toInt(teaMap.get(teacherInfo.getId()).getLevelValue()) : 0);
            item.put("IMG_URL", teacherInfo.fetchImageUrl());
            teacherLevelRank.add(item);
        }

        return teacherLevelRank;
    }
}
