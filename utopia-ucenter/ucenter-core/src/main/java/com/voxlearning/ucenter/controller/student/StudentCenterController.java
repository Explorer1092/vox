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

package com.voxlearning.ucenter.controller.student;

import com.nature.commons.lang.util.StringUtil;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.random.RandomGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.ucenter.service.user.ClazzWebappService;
import com.voxlearning.ucenter.support.ValidateEmailSender;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.ucenter.support.convert.ErrorMessageConvert;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.email.api.mapper.EmailReceiptor;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.PaymentPasswordMobileValidationWrapper;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.constants.StudentParentStatus;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * student center
 *
 * @author changyuan.liu
 * @since 2015.12.15
 */
@Controller
@RequestMapping("/student/center")
public class StudentCenterController extends AbstractWebController {
    private static final int ELEMENTS_PER_PAGE = 10;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private ValidateEmailSender validateEmailSender;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject protected DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private ClazzWebappService clazzWebappService;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;
    @ImportService(interfaceClass = VerificationService.class) private VerificationService verificationService;

    // 2014暑期改版 -- 基本信息
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 判断用户是否需要修改真实名称
        boolean supplementName = false;
        if (currentStudentDetail().isPrimaryStudent()) {
            supplementName = StringUtil.isEmpty(currentUser().getProfile().getRealname());
        } else {
            supplementName = StringRegexUtils.isNotRealName(currentUser().getProfile().getRealname());
        }
        model.addAttribute("isSupplementName", supplementName);

        model.addAttribute("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(currentUserId()));
        model.addAttribute("email", sensitiveUserDataServiceClient.loadUserEmailObscured(currentUserId()));

        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(currentUserId())
                .getUninterruptibly();
        model.addAttribute("balance", null == finance ? 0 : finance.getBalance());

        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail.isShensz() || studentDetail.isJuniorStudent()) {
            KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(currentUserId());
            if (klxStudent != null) {
                String scanNumber = klxStudent.getScanNumber();
                model.addAttribute("scanNumber", scanNumber);
            }
            //目前能进入中学个人中心的肯定是神算子
            model.addAttribute("isShensz", studentDetail.isShensz());
            return "studentssz/center/index";
        }
        return "studentv3/center/index";
    }

    // 2014暑期改版 -- 我的资料
    @RequestMapping(value = "information.vpage", method = RequestMethod.GET)
    public String infomation(Model model) {
        model.addAttribute("currentUserProfileQq", sensitiveUserDataServiceClient.loadUserQqObscured(currentUserId()));

        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(currentUserId())
                .getUninterruptibly();
        model.addAttribute("balance", null == finance ? 0 : finance.getBalance());
        model.addAttribute("updateType", getRequestString("updateType"));

        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail.isShensz() || studentDetail.isJuniorStudent()) {
            KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(currentUserId());
            if (klxStudent != null) {
                String scanNumber = klxStudent.getScanNumber();
                model.addAttribute("scanNumber", scanNumber);
            }

            return "studentssz/center/information";
        }
        return "studentv3/center/information";
    }

    // 2014暑期改版 -- 保存我的资料
    @RequestMapping(value = "saveprofiledata.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveProfileData() {
        //极算中学和原有小学页面不同, 分开处理
        if (currentStudentDetail().isPrimaryStudent()) {
            return saveProfileByPrimaryStudent();
        } else {
            return saveProfileByJuniorStudent();
        }
    }

    /**
     * 中学生修改学生信息(目前只是修改填涂号) 参考 com.voxlearning.washington.controller.open.v1.student.StudentApiController#modifyKlxScanNumber()
     */
    private MapMessage saveProfileByJuniorStudent() {
        String scanNumber = getRequestStringCleanXss("scanNumber");
        if (StringUtils.isEmpty(scanNumber)) {
            return MapMessage.errorMessage("填涂号不可为空");
        }

        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) {
            return MapMessage.errorMessage("学生未分配班级");
        }

        Long studentId = student.getId();
        Long schoolId = student.getClazz().getSchoolId();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        int digit = 5; // 默认五位填涂号
        if (schoolExtInfo != null && schoolExtInfo.getScanNumberDigit() != null) {
            digit = Integer.max(digit, schoolExtInfo.getScanNumberDigit());
        }
        if (StringUtils.isBlank(scanNumber) || scanNumber.length() > digit) {
            return MapMessage.errorMessage("填涂号位数不正确，请填写" + digit + "位数字");
        }

        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(Collections.singletonList(studentId), false).get(studentId);

        if (CollectionUtils.isEmpty(groups)) {
            return MapMessage.errorMessage("学生班级为空");
        }

        MapMessage message = newKuailexueServiceClient.modifyKlxScanNumber(schoolId, studentId, scanNumber);
        return message;
    }

    private MapMessage saveProfileByPrimaryStudent() {
        User user = currentUser();
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        String strQq = getRequestParameter("qq", "");
        // qq特殊处理，返回资料时是掩码后的qq，所以这里可能也是掩码后的qq
        if (StringUtils.equals(strQq, sensitiveUserDataServiceClient.loadUserQqObscured(currentUserId()))) {
            strQq = ua.getSensitiveQq();
        }
        String strGender = getRequestParameter("gender", "");
        Integer year = getRequestInt("year", 0);
        Integer month = getRequestInt("month", 0);
        Integer day = getRequestInt("day", 0);
        return userServiceClient.updateUserProfile(user, strQq, year, month, day, strGender);
    }

    @RequestMapping(value = "changeclazz.vpage", method = RequestMethod.GET)
    public String changeClazz(Model model) {
        model.addAttribute("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(currentUserId()));
        return "studentssz/changeclazz/index";
    }

    @RequestMapping(value = "changeclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doChangeClazz() {
        String clazzIdString = getRequestString("classId");
        String teacherIdString = getRequestString("teacherId");
        String scanNumber = getRequestString("scanNumber");
        String virificationCode = getRequestString("captchaCode");
        StudentDetail studentDetail = currentStudentDetail();

        boolean scanNumberExists = false;
        if (StringUtils.isNotEmpty(scanNumber)) {
            scanNumberExists = true;
        }

        if (StringUtils.isEmpty(clazzIdString)) {
            return MapMessage.errorMessage("请选择班级");
        }
        if (StringUtils.isEmpty(teacherIdString)) {
            return MapMessage.errorMessage("请选择老师");
        }
        if (StringUtils.isEmpty(virificationCode)) {
            return MapMessage.errorMessage("请输入验证码");
        }

        Long clazzId = null;
        Long teacherId = null;
        try {
            clazzId = Long.parseLong(clazzIdString);
            teacherId = Long.parseLong(teacherIdString);
        } catch (NumberFormatException e) {
            return MapMessage.errorMessage("参数错误");
        }

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);

        if (teacher.isSeniorTeacher() || teacher.isJuniorTeacher()) {
            // 有填涂号的，该填涂号已经有对应的17作业学生ID了，无填涂号的，重名
            MapMessage checkResult = newKuailexueServiceClient.checkStudentJoinTeacherGroup(clazzId, teacherId, studentDetail.getId(), scanNumber);
            if (!checkResult.isSuccess()) {
                return MapMessage.errorMessage(checkResult.getInfo());
            }
        }

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(currentUserId(), virificationCode, SmsType.PC_STUDENT_CHANGE_CLAZZ_VERIFY_MOBILE.name());
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        MapMessage mapMessage = joinClazz(studentDetail, clazzId, teacherId, true);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
            if (scanNumberExists) {
                return newKuailexueServiceClient.linkKlxStudentByScanNumber(clazzId, teacherId, studentDetail.getId(), scanNumber);
            } else {
                return newKuailexueServiceClient.joinKlxClazz(clazzId, teacherId, studentDetail.getId());
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 参见 com.voxlearning.washington.controller.open.v1.student.StudentClazzApiController#joinClazz()
     */
    private MapMessage joinClazz(Student student, Long clazzId, Long teacherId, boolean forceLink) {

        // 2016.1.31 学生滥用换班功能刷学霸学豆的处理
        Boolean isValid = studentSystemClazzServiceClient.isValidClazzJoinRequest(teacherId, student.getId());
        if (!isValid) {
            return MapMessage.errorMessage("你最近换班次数异常，已被系统禁止换班！");
        }

        // 加入班级 由于存在账号再使用的情况，所以这里还要判断一下学生是否已经在老师的组里面了
        MapMessage message = studentSystemClazzServiceClient.studentJoinClazz(student.getId(), teacherId, clazzId, forceLink, OperationSourceType.pc);
        if (!message.isSuccess()) {
            String errInfo = ErrorMessageConvert.joinClazzErrorMsg(message);
            return MapMessage.errorMessage(errInfo);
        }

        return MapMessage.successMessage();
    }

    /**
     * PC 端初中学生个人中心更换系统班级发送验证码
     */
    @RequestMapping(value = "changeclazz/verification.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendVerificationCode() {
        String phone = sensitiveUserDataServiceClient.showUserMobile(currentUserId(), "ucenter:sendVerificationCode", SafeConverter.toString(currentUserId()));
        if (StringUtils.isBlank(phone)) {
            return MapMessage.errorMessage("请先绑定手机");
        }
        if (!MobileRule.isMobile(phone)) {
            return MapMessage.errorMessage("错误的手机号");
        }

        MapMessage mapMessage = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), phone, SmsType.PC_STUDENT_CHANGE_CLAZZ_VERIFY_MOBILE.name());
        return mapMessage;
    }

    @RequestMapping(value = "getTeacherList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherList() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("请输入老师号");
        }

        long teacherId = SafeConverter.toLong(id);
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("老师号错误");
        }

        if (!teacher.isJuniorMathTeacher()) {
            return MapMessage.errorMessage("请输入初中数学老师的号哦");
        }

        MapMessage mapMessage = clazzWebappService.getClazzListByTeacher(teacher);

        if (mapMessage.isSuccess()) {
            String teacherSchoolName = teacher.getTeacherSchoolName();
            String teacherName = teacher.getProfile().getRealname();
            String teacherSubject = teacher.getSubject().getValue();
            Boolean scanMachineFlag = false;

            Long teacherSchoolId = teacher.getTeacherSchoolId();
            if (teacherSchoolId != null) {
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(teacherSchoolId)
                        .getUninterruptibly();
                if (schoolExtInfo != null) {
                    scanMachineFlag = schoolExtInfo.getScanMachineFlag();
                }
            }

            return MapMessage.successMessage().add("clazzList", mapMessage.get("clazzList"))
                    .add("schoolName", teacherSchoolName)
                    .add("teacherName", teacherName)
                    .add("teacherSubject", teacherSubject)
                    .add("scanMachineFlag", scanMachineFlag);
        }
        return mapMessage;
    }


    // 2014暑期改版 -- 账号安全
    @RequestMapping(value = "account.vpage", method = RequestMethod.GET)
    public String account(Model model) {
        User student = currentUser();
        String am = sensitiveUserDataServiceClient.loadUserMobileObscured(student.getId());
        String ae = sensitiveUserDataServiceClient.loadUserEmailObscured(student.getId());

        List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
        // 循环获取用户的mobile
        List<Map<String,String>> parentMoiles = new LinkedList<>();
        for (StudentParent studentParent : parents) {
            // load obscured mobile
            Map<String,String> parentInfo = new LinkedHashMap<>();
            Long parentId = studentParent.getParentUser().getId();
            String obscuredMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
            parentInfo.put("callName",studentParent.getCallName());
            parentInfo.put("mobile",obscuredMobile);
            parentMoiles.add(parentInfo);
            // FIXME 20171001
            // studentParent.getParentUser().getProfile().setSensitiveMobile(obscuredMobile);
        }
        StudentParentStatus sps = StudentParent.getStudentParentStatus(parents);
        UserAuthentication studentUa = userLoaderClient.loadUserAuthentication(student.getId());
        model.addAttribute("parentList", parents);
        model.addAttribute("parentMoiles",parentMoiles);
        model.addAttribute("studentParentStatus", sps);
        model.addAttribute("pwdState", studentUa.fetchPasswordState().getCode());
        model.addAttribute("hasPaymentPassword", StringUtils.isNotBlank(studentUa.getPaymentPassword()));
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(student.getId());
        model.addAttribute("mobileVerified", ua != null && ua.isMobileAuthenticated());
        model.addAttribute("mobile", am);
        model.addAttribute("emailVerified", ua != null && ua.isEmailAuthenticated());
        model.addAttribute("email", ae);
//        model.addAttribute("securityQuestionSetted", ua != null && ua.isSecurityQuestionSetted());
        model.addAttribute("securityQuestionSetted", false);
        model.addAttribute("updateType", getRequestString("updateType"));
        model.addAttribute("qq", thirdPartyLoaderClient.loadLandingSource(student.getId(), SsoConnections.QQ.getSource()));

        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(currentUserId())
                .getUninterruptibly();
        model.addAttribute("balance", null == finance ? 0 : finance.getBalance());

        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail.isShensz() || studentDetail.isJuniorStudent()) {
            return "studentssz/center/account";
        }
        return "studentv3/center/account";
    }

    // 2014暑期改版 -- 绑定手机 -- 发送验证码
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMobileCode() {
        try {
            String mobile = getRequest().getParameter("mobile");
            return smsServiceHelper.sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.STUDENT_VERIFY_MOBILE_CENTER);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 验证手机
    @RequestMapping(value = "validatemobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitRebindMobile() {
        try {
            String code = getRequest().getParameter("latestCode");
            MapMessage message = verificationService.verifyMobile(currentUserId(), code, SmsType.STUDENT_VERIFY_MOBILE_CENTER.name());
            return message;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    /**
     * FIXME:本来可以老师和学生可以共用的, 但是老师发送验证码的枚举不能动, url 路径也带 teacher, 所以发送验证码分开, 验证放一起
     * PC 学生修改密码，发送短信验证码
     */
    @RequestMapping(value = "sendTCPWcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendStudentChangePasswordCode() {
        String phone = sensitiveUserDataServiceClient.showUserMobile(currentUserId(), "ucenter:sendStudentChangePasswordCode", SafeConverter.toString(currentUserId()));

        if (StringUtils.isBlank(phone)) {
            return MapMessage.errorMessage("请先绑定手机");
        }
        if (!MobileRule.isMobile(phone)) {
            return MapMessage.errorMessage("错误的手机号");
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), phone, SmsType.PC_JUNIOR_STUDENT_VERIFY_RESET_PASS.name());
    }

    // 2014暑期改版 -- 发送验证邮件
    @RequestMapping(value = "sendvalidateEmail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendValidateEmail() {
        User student = currentUser();
        if (student == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String email = getRequestParameter("email", "");
        if (userLoaderClient.loadEmailAuthentication(email) != null) {
            return MapMessage.errorMessage("该邮箱已被验证, 请更换邮箱");
        }
        EmailReceiptor receiptor = new EmailReceiptor();
        receiptor.setUserId(student.getId());
        receiptor.setRealname(student.fetchRealname());
        receiptor.setEmail(email);
        String siteUrl = ProductConfig.getUcenterUrl();
        return validateEmailSender.sendValidateEmail(receiptor, siteUrl);
    }

//    // 2014暑期改版 -- 设置密保问题
//    @RequestMapping(value = "securityquestion.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage setSecurityQuestion(@RequestBody SecurityQuestionMapper mapper) {
//        MapMessage message;
//        try {
//            message = userServiceClient.setUserSecurityQuestion(currentUser(), mapper);
//        } catch (Exception ex) {
//            message = MapMessage.errorMessage();
//        }
//        if (message.isSuccess()) {
//            return message.setInfo("密保问题设置成功");
//        } else {
//            return message.setInfo("密保问题设置失败");
//        }
//    }

    // 2014暑期改版 -- 注册家长并验证家长手机 -- 生成验证码
    @RequestMapping(value = "sendregisterandverifyparentphonecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateMobileValidationCodeForCreatingAndBindingParentMobile(@RequestBody Map<String, Object> body) {
        try {
            String mobile = (String) body.get("mobile");
            return smsServiceHelper.sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_STUDENT_REGISTER_AND_VERIFY,
                    UserType.PARENT,
                    getWebRequestContext().getRealRemoteAddress()
            );
        } catch (Exception ex) {
            logger.error("Error occurs when generating authentication code for binding parent mobile", ex);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 学生已经有家长，但家长没有验证手机，学生帮家长验证手机 -- 发送验证码
    // 这个是在学生有家长但不是关键家长的时候才会被调用到
    @RequestMapping(value = "sendverifyparentphonecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateMobileValidationCodeForBindingParentMobile(@RequestBody Map<String, Object> body) {
        try {
            String mobile = (String) body.get("mobile");
            Long parentId = ConversionUtils.toLong(body.get("parentId"));
            // 此处用学生想要绑定的家长的Id生成key
            return smsServiceHelper.sendUnbindMobileVerificationCode(parentId, mobile, SmsType.PARENT_VERIFY_MOBILE_STUDENT_VERIFY);
        } catch (Exception ex) {
            logger.error("Error occurs when generating authentication code for binding parent mobile", ex);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 学生已经有家长，但家长没有验证手机，学生帮家长验证手机
    // 这个是在学生有家长但不是关键家长的时候才会被调用到
    @RequestMapping(value = "verifymobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyValidationCodeForBindingParentMobile(@RequestBody Map<String, Object> body) {
        try {
            String code = (String) body.get("latestCode");
            Long parentId = ConversionUtils.toLong(body.get("parentId"));
            // 验证学生的关键家长的手机
            return verificationService.verifyMobile(parentId, code, SmsType.PARENT_VERIFY_MOBILE_STUDENT_VERIFY.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    // 2014暑期改版 -- 注册家长并验证绑定家长手机号码
    @RequestMapping(value = "registerparentandverifymobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyValidationCodeForRegisteringAndBindingParentMobile(@RequestBody Map<String, Object> body) {
        try {
            User student = currentUser();
            if (null == student) {
                return MapMessage.errorMessage("学生ID不正确");
            }

            // 获取前端参数
            String codeA = (String) body.get("latestCode");
            String targetCallName = (String) body.get("callName");
            String targetParentName = (String) body.get("name");
            String mobile = (String) body.get("mobile");
            if (StringUtils.isBlank(codeA) || StringUtils.isBlank(targetCallName) || StringUtils.isBlank(targetParentName) || StringUtils.isBlank(mobile)) {
                return MapMessage.errorMessage("请重新输入家长信息和验证码");
            }
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("手机号不正确，请重新输入");
            }

            MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, codeA, SmsType.PARENT_VERIFY_MOBILE_STUDENT_REGISTER_AND_VERIFY.name());
            if (!validateResult.isSuccess()) {
                return validateResult;
            }

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_PARENT);
            neonatalUser.setUserType(UserType.PARENT);
            neonatalUser.setRealname(targetParentName);
            neonatalUser.setMobile(mobile);
            neonatalUser.setPassword(RandomGenerator.generatePlainPassword());

            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("注册家长用户失败");
            }
            User parent = (User) message.get("user");
            Long parentId = parent.getId();

            if (userServiceClient.activateUserMobile(parentId, mobile).isSuccess()) {
                String smsPayload = "尊敬的家长，帐号绑定成功！使用此手机号登录一起作业家长端，密码：" + neonatalUser.getPassword() + "，请尽快登录修改密码";
                userSmsServiceClient.buildSms()
                        .to(parentId)
                        .content(smsPayload)
                        .type(SmsType.STUDENT_CENTER_REGISTER_PARENT)
                        .send();
                // 设置关键家长
                if (parentLoaderClient.loadStudentKeyParent(student.getId()) == null) {
                    MapMessage msg = parentServiceClient.bindExistingParent(student.getId(), parentId, true, targetCallName);
                    if (!msg.isSuccess()) {
                        return MapMessage.errorMessage("绑定家长失败");
                    }

                    // 发消息
                    String payload = "绑定验证家长手机号码成功";
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage(student.getId(), payload);
                }
            }

            return MapMessage.successMessage("手机验证成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    // 2014暑期改版 -- 学生重新验证家长手机 -- 发送验证码
    @RequestMapping(value = "sendrebindcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateMobileValidationCodeForRebindingParentMobile() {
        try {
            String mobile = getRequest().getParameter("mobile");
            StudentParent studentParent = parentLoaderClient.loadStudentKeyParent(currentUserId());
            if (studentParent == null) {
                return MapMessage.errorMessage("请重新绑定家长手机");
            }
            // 此处用学生的关键家长的Id生成key
            return smsServiceHelper.sendUnbindMobileVerificationCode(studentParent.getParentUser().getId(), mobile, SmsType.PARENT_VERIFY_MOBILE_STUDENT_MODIFY_AND_VERIFY);
        } catch (Exception ex) {
            logger.error("Error occurs when generating authentication code for rebinding parent mobile", ex);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 学生重新绑定家长手机
    @RequestMapping(value = "rebindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyValidationCodeForBindingParentMobile() {
        try {
            String code = getRequest().getParameter("latestCode");
            StudentParent studentParent = parentLoaderClient.loadStudentKeyParent(currentUserId());
            if (studentParent == null) {
                return MapMessage.errorMessage("请重新绑定家长手机");
            }
            // 验证学生的关键家长的手机
            return verificationService.verifyMobile(studentParent.getParentUser().getId(), code, SmsType.PARENT_VERIFY_MOBILE_STUDENT_MODIFY_AND_VERIFY.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    // 2014暑期改版 -- 设置支付密码
    @RequestMapping(value = "setpp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setPaymentPassword() {
        User user = currentUser();
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        if (StringUtils.isNotBlank(ua.getPaymentPassword())) {
            return MapMessage.errorMessage("您已经设置了支付密码");
        }
        String pwd1 = getRequest().getParameter("pwd1");
        String pwd2 = getRequest().getParameter("pwd2");
        if (!StringUtils.equals(pwd1, pwd2)) {
            return MapMessage.errorMessage("两次密码不一致");
        }
        if (ua.fetchUserPassword().match(pwd1)) {
            return MapMessage.errorMessage("支付密码不能和登录密码相同");
        }
        if (!userServiceClient.setPaymentPassword(currentUserId(), pwd1).isSuccess()) {
            return MapMessage.errorMessage("设置支付密码失败");
        }
        return MapMessage.successMessage();
    }

    // 2014暑期改版 -- 修改支付密码
    @RequestMapping(value = "changepp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changePaymentPassword() {
        String oldPwd = getRequest().getParameter("oldPwd");
        String pwd1 = getRequest().getParameter("pwd1");
        String pwd2 = getRequest().getParameter("pwd2");
        if (!StringUtils.equals(pwd1, pwd2)) {
            return MapMessage.errorMessage("两次密码不一致");
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
        if (ua.fetchUserPassword().match(pwd1)) {
            return MapMessage.errorMessage("支付密码不能和登录密码相同");
        }
        MapMessage message = userServiceClient.changePaymentPassword(currentUserId(), oldPwd, pwd1);
        if (!message.isSuccess() && message.getInfo() == null) {
            message.setInfo("修改支付密码失败");
        }
        return message;
    }

    // 2014暑期改版 -- 忘记支付密码
    @RequestMapping(value = "forgetpp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage forgetPaymentPassword() {
        Long userId = currentUserId();
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(userId);
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(userId);
        StudentParentStatus sps = StudentParent.getStudentParentStatus(parents);
        if (sps == StudentParentStatus.WITH_KEY_PARENT || (ua != null && ua.isMobileAuthenticated())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    // 2014暑期改版 -- 忘记支付密码，给绑定的自己手机或者家长手机发送验证码
    @RequestMapping(value = "sendmppvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendModifyPaymentPasswordVerificationCode() {
        return smsServiceHelper.sendMobileVerificationCodeToMobileForPaymentPassword(currentUserId());
    }

    // 2014暑期改版 -- 忘记支付密码，验证码是否正确
    @RequestMapping(value = "verifymppvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyModifyPaymentPasswordVerificationCode() {
        Long studentId = currentUserId();
        String code = getRequest().getParameter("code");
        if (StringUtils.isEmpty(code)) {
            return MapMessage.errorMessage("请输入验证码");
        }

        PaymentPasswordMobileValidationWrapper context = smsServiceClient.getSmsService()
                .loadPaymentPasswordMobileValidationWrapper(SmsType.STUDENT_MODIFY_PAYMENT_PASSWORD.name(), studentId)
                .getUninterruptibly();
        if (null == context) {
            return MapMessage.errorMessage("当前操作已经失效。请重新进行找回支付密码的操作。");
        }

        if (!Objects.equals(studentId, context.getUserId())) {
            return MapMessage.errorMessage("当前操作已经失效。请重新进行找回支付密码的操作。");
        }
        String mobile = context.getMobile();
        String validationCode = context.getValidationCode();
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(validationCode)) {
            return MapMessage.errorMessage("验证码失效，请重新获取验证码");
        }

        if (StringUtils.equals(code, validationCode)) {
            context.setValidated(true);
            smsServiceClient.getSmsService()
                    .setPaymentPasswordMobileValidationWrapper(SmsType.STUDENT_MODIFY_PAYMENT_PASSWORD.name(), studentId, context)
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("验证码不匹配");
        }
    }

    // 2014暑期改版 -- 忘记支付密码，重置支付密码页面
    @RequestMapping(value = "htmlchip/setpaymentpassword.vpage", method = RequestMethod.GET)
    public String setpaymentpassword() {
        Long studentId = currentUserId();
        PaymentPasswordMobileValidationWrapper context = smsServiceClient.getSmsService()
                .loadPaymentPasswordMobileValidationWrapper(SmsType.STUDENT_MODIFY_PAYMENT_PASSWORD.name(), studentId)
                .getUninterruptibly();
        if (null == context) {
            return "redirect:/student/center/index.vpage";
        }
        if (!Objects.equals(studentId, context.getUserId()) || !context.isValidated()) {
            return "redirect:/student/center/index.vpage";
        }
        return "student/center/htmlchip/setpaymentpassword";
    }

    // 2014暑期改版 -- 忘记支付密码，重置支付密码
    @RequestMapping(value = "resetpp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPaymentPassword() {
        Long studentId = currentUserId();
        PaymentPasswordMobileValidationWrapper context = smsServiceClient.getSmsService()
                .loadPaymentPasswordMobileValidationWrapper(SmsType.STUDENT_MODIFY_PAYMENT_PASSWORD.name(), studentId)
                .getUninterruptibly();
        if (null == context) {
            return MapMessage.errorMessage("当前操作已经失效。请重新进行找回支付密码的操作。");
        }
        if (!Objects.equals(studentId, context.getUserId()) || !context.isValidated()) {
            return MapMessage.errorMessage("当前操作已经失效。请重新进行找回支付密码的操作。");
        }

        String pwd1 = getRequest().getParameter("pwd1");
        String pwd2 = getRequest().getParameter("pwd2");
        if (!StringUtils.equals(pwd1, pwd2)) {
            return MapMessage.errorMessage("两次密码不一致");
        }
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
        if (ua.fetchUserPassword().match(pwd1)) {
            return MapMessage.errorMessage("支付密码不能和登录密码相同");
        }
        if (!userServiceClient.setPaymentPassword(currentUserId(), pwd1).isSuccess()) {
            return MapMessage.errorMessage("设置支付密码失败");
        }
        smsServiceClient.getSmsService()
                .deletePaymentPasswordMobileValidationWrapper(SmsType.STUDENT_MODIFY_PAYMENT_PASSWORD.name(), studentId)
                .awaitUninterruptibly();
        return MapMessage.successMessage();
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

    // 2014暑期改版 -- 我的学豆
    @RequestMapping(value = "integral.vpage", method = RequestMethod.GET)
    public String integral(Model model) {
        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(currentUserId())
                .getUninterruptibly();
        model.addAttribute("balance", null == finance ? 0 : finance.getBalance());

        return "studentv3/center/integral";
    }

    @RequestMapping(value = "integralchip.vpage", method = RequestMethod.GET)
    public String integralChip(Model model) {
        Integer pageNumber = getRequestInt("pageNumber", 1);
        // 获取银币前三个月的历史数据
        UserIntegralHistoryPagination pagination = userLoaderClient
                .loadUserIntegralHistories(currentUser(), 3, pageNumber - 1, ELEMENTS_PER_PAGE);
        model.addAttribute("pagination", pagination);
        model.addAttribute("integral", pagination.getUsableIntegral());
        model.addAttribute("currentPage", pageNumber);
        return "studentv3/center/integralchip";
    }

}
