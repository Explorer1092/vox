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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.athena.api.UctUserService;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.data.UserRecordData;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.mapper.CreditChangeResult;
import com.voxlearning.utopia.service.integral.client.CreditServiceClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientCategory;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/crm/user")
public class CrmUserController extends CrmAbstractController {

    private static final String STUDENT_PWD_RESET_SMS = "{0}同学好，客服已帮你重置密码，请用新密码：{1} 登录做作业。" +
            "如不是你要求重置，请拨打400-160-1717";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;
    @Inject private CreditServiceClient creditServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserManagementClient userManagementClient;
    @Inject private EmailServiceClient emailServiceClient;

    @ImportService(interfaceClass = UctUserService.class)
    private UctUserService uctUserService;

    private static final String nameRegex = "^[\u4d00-\u9fa5]+([·•][\u4d00-\u9fa5]+)*$";

    /**
     * ***********************查询中间跳转****************************************************************
     */
    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL}, postRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "userhomepage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String redirectUserHomepage() {
        Long userId;
        try {
            userId = Long.parseLong(getRequestParameter("userId", "").replaceAll("\\s", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID" + getRequestParameter("userId", "") + " 不合规范。");
            return "crm/index";
        }

        User user = userLoaderClient.loadUser(userId);

        if (user == null) {
            getAlertMessageManager().addMessageError("用户（ID:" + userId + "）不存在。");
            return "crm/index";
        }

        switch (user.fetchUserType()) {
            case STUDENT:
                return redirect("/crm/student/studenthomepage.vpage?studentId=" + userId);
            case TEACHER:
                //切换到新版的老师详情页面
                return redirect("/crm/teachernew/teacherdetail.vpage?teacherId=" + userId);
//                return redirect("/crm/teacher/teacherhomepage.vpage?teacherId=" + userId);
            case PARENT:
                return redirect("/crm/parent/parenthomepage.vpage?parentId=" + userId);
            case RESEARCH_STAFF:
                return redirect("/crm/researchstaff/researchstaffhomepage.vpage?researchStaffId=" + userId);
            case TEMPORARY:
                return redirect("/crm/temporary/temporaryhomepage.vpage?temporaryId=" + userId);
            case EMPLOYEE:
                return redirect("/crm/marketer/marketerhomepage.vpage?marketerId=" + userId);
            default:
                getAlertMessageManager().addMessageError("用户（ID:" + userId + "）不存在。");
                return "crm/index";
        }
    }

    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL}, postRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "wechatuserhomepage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String redirectWechatUserHomepage() {

        String openId = StringUtils.trim(getRequestParameter("openId", ""));

        if (StringUtils.isEmpty(openId)) {
            getAlertMessageManager().addMessageError("用户（OpenID:" + openId + "）不符合规范。");
            return "crm/index";
        }
        User user = wechatLoaderClient.loadWechatUser(openId);
        if (user == null) {
            getAlertMessageManager().addMessageError("用户（OpenID:" + openId + "）不存在。");
            return "crm/index";
        }
        switch (user.fetchUserType()) {
            case TEACHER:
                return redirect("/crm/teacher/teacherhomepage.vpage?teacherId=" + user.getId());
            case PARENT:
                return redirect("/crm/parent/parenthomepage.vpage?parentId=" + user.getId());
            default:
                getAlertMessageManager().addMessageError("用户（ID:" + user.getId() + "）不存在。");
                return "crm/index";
        }
    }

    /**
     * ***********************进线日志*****************************************************************
     */
    @RequestMapping(value = "addcustomerrecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCustomerRecord() {
        Long userId;
        String operation;
        String questionDesc;
        try {
            userId = Long.parseLong(getRequestParameter("userId", "0"));
            operation = getRequestParameter("operation", "").replaceAll("\\s", "");
            questionDesc = getRequestParameter("questionDesc", "").replaceAll("\\s", "");
        } catch (Exception ignored) {
            return MapMessage.errorMessage("增加用户备注失败，请正确填写各参数");
        }
        if (StringUtils.isBlank(operation) && StringUtils.isBlank(questionDesc)) {
            return MapMessage.errorMessage("请填写操作或问题描述");
        }
        //如果是副账号,同步备注到主账号
        Long mainteacherId = teacherLoaderClient.loadMainTeacherId(userId);
        if (mainteacherId != null) {//是主账号
            UserServiceRecord serviceRecord = new UserServiceRecord();
            serviceRecord.setOperationType(UserServiceRecordOperationType.客服添加.name());
            serviceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            serviceRecord.setUserId(mainteacherId);
            serviceRecord.setOperationContent(operation);
            serviceRecord.setComments(questionDesc);
            userServiceClient.saveUserServiceRecord(serviceRecord);
        }
        UserServiceRecord serviceRecord = new UserServiceRecord();
        serviceRecord.setOperationType(UserServiceRecordOperationType.客服添加.name());
        serviceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        serviceRecord.setUserId(userId);
        serviceRecord.setOperationContent(operation);
        serviceRecord.setComments(questionDesc);
        userServiceClient.saveUserServiceRecord(serviceRecord);

        MapMessage mapMessage = MapMessage.successMessage("增加用户备注成功");
        mapMessage.put("customerServiceRecord", serviceRecord);
        mapMessage.put("createTime", DateUtils.dateToString(new Date()));
        return mapMessage;
    }

    /**
     * 绑定手机的用户
     */
    @AdminAcceptRoles(getRoles = {AdminPageRole.ALLOW_ALL})
    @RequestMapping(value = "mobileusers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<UserType, User> mobileUsers() {
        Map<UserType, User> mobileUsers = new HashMap<>();
        String mobile = requestString("mobile");
        if (StringUtils.isNotBlank(mobile)) {
            // FIXME: 逻辑先暂时改成这样
            UserAuthentication authentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
            User student = userLoaderClient.loadUser(authentication == null ? null : authentication.getId());
            if (student != null && student.isStudent()) {
                mobileUsers.put(UserType.STUDENT, student);
            }
            authentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
            User parent = userLoaderClient.loadUser(authentication == null ? null : authentication.getId());
            if (parent != null && parent.isParent()) {
                mobileUsers.put(UserType.PARENT, parent);
            }
            authentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
            User teacher = userLoaderClient.loadUser(authentication == null ? null : authentication.getId());
            if (teacher != null && teacher.isTeacher()) {
                mobileUsers.put(UserType.TEACHER, teacher);
            }
        }
        return mobileUsers;
    }

    /**
     * ***********************修改用户信息*****************************************************************
     */
    @RequestMapping(value = "resetpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPassword() {
        long userId = getRequestLong("userId", -1L);
        String password = getRequestParameter("password", "");
        String passwordDesc = getRequestParameter("passwordDesc", "");
        String passwordExtraDesc = getRequestParameter("passwordExtraDesc", "");
        String verifyMobile = getRequestParameter("verifyMobile", "").trim();
        MapMessage message = new MapMessage();
        if (StringUtils.isBlank(passwordDesc) || ("其他".equals(passwordDesc) && StringUtils.isBlank(passwordExtraDesc))) {
            message.setSuccess(false);
            message.setInfo("重置用户密码失败，请正确填写各参数");
            return message;
        }
        String adminUser = getCurrentAdminUser().getAdminUserName();
        try {
            passwordDesc += "（" + passwordExtraDesc + "）";
            User user = crmUserService.resetUserPassword(userId, password, false, verifyMobile, passwordDesc, adminUser, "CrmUserController.resetPassword");
            if (user != null && UserType.STUDENT == user.fetchUserType()) {
                studentPasswordResetAction(verifyMobile, user.fetchRealname(), password, user.getId());
            }
            message.setSuccess(true);
            message.setInfo("重置密码成功");
        } catch (Exception ex) {
            logger.warn("重置密码失败，userId:{},password:{},passwordDesc:{},passwordExtraDesc:{},mobile:{},msg:{}", userId, password, passwordDesc, passwordExtraDesc, verifyMobile, ex.getMessage());
            message.setSuccess(false);
            message.setInfo("重置密码失败，" + ex.getMessage());
        }
        return message;
    }

    private void studentPasswordResetAction(String verifyMobile, String studentName, String password, Long studentId) {
        if (StringUtils.isNotBlank(verifyMobile)) {
            // 已绑定手机，发送密码重置短息
            String content = MessageFormat.format(STUDENT_PWD_RESET_SMS, studentName, password);
            smsServiceClient.createSmsMessage(verifyMobile).content(content).type(SmsType.CRM_RESET_USER_PWD.name()).send();
        }
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(studentId);
        if (userAuthentication == null || !userAuthentication.isMobileAuthenticated()) {
            // 未绑定手机，标记MOBILE_FORCE_BIND类型的UserTag，学生登录后强制提醒绑定手机
            UserTag.Tag tag = new UserTag.Tag(UserTagType.MOBILE_FORCE_BIND, null, new Date());
            userManagementClient.updateTag(studentId, UserType.STUDENT, tag);
        }
    }

    // 重置用户支付密码
    @RequestMapping(value = "resetpaymentpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetShopPassword() {

        long userId = getRequestLong("userId", -1L);
        String paymentPassword = getRequestParameter("paymentPassword", "");
        String passwordDesc = getRequestParameter("passwordDesc", "");
        String passwordExtraDesc = getRequestParameter("passwordExtraDesc", "");

        User user = userLoaderClient.loadUser(userId);
        paymentPassword = paymentPassword.replaceAll("\\s", "");
        passwordDesc = passwordDesc.replaceAll("\\s", "");
        passwordExtraDesc = passwordExtraDesc.replaceAll("\\s", "");

        if ((user == null) || StringUtils.isBlank(paymentPassword) || StringUtils.isBlank(passwordDesc)
                || ("其他".equals(passwordDesc) && StringUtils.isBlank(passwordExtraDesc)))
            return MapMessage.errorMessage("重置用户支付密码失败，请正确填写各参数");

        passwordDesc += "（" + passwordExtraDesc + "）";

        MapMessage msg = userServiceClient.setPaymentPassword(userId, paymentPassword);
        if (!msg.isSuccess()) {
            return MapMessage.errorMessage("重置用户支付密码失败");
        }

        // 记录 UserServiceRecord
        String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "重置用户支付密码。";
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("重置支付密码");
        userServiceRecord.setComments(operation + "问题描述[" + passwordDesc + "]");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        MapMessage mapMessage = MapMessage.successMessage("重置用户支付密码成功");
        mapMessage.put("customerServiceRecord", userServiceRecord);
        mapMessage.put("createTime", DateUtils.dateToString(new Date()));
        return mapMessage;
    }

//    // 重置密保问题
//    @RequestMapping(value = "resetpasswordquestion.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage resetPasswordQuestion() {
//
//        long userId = getRequestLong("userId", -1L);
//        String passwordDesc = getRequestParameter("passwordDesc", "");
//        String passwordExtraDesc = getRequestParameter("passwordExtraDesc", "");
//
//        User user = userLoaderClient.loadUser(userId);
//        passwordDesc = passwordDesc.replaceAll("\\s", "");
//        passwordExtraDesc = passwordExtraDesc.replaceAll("\\s", "");
//
//        if ((user == null) || StringUtils.isBlank(passwordDesc)
//                || ("其他".equals(passwordDesc) && StringUtils.isBlank(passwordExtraDesc)))
//            return MapMessage.errorMessage("重置用户密保问题失败，请正确填写各参数");
//
//        passwordDesc += "（" + passwordExtraDesc + "）";
//
//        MapMessage message = userServiceClient.updateSecutiryQuestionSetted(user.getId(), Boolean.FALSE);
//        if (!message.isSuccess()) {
//            return MapMessage.errorMessage("重置用户密保问题失败");
//        } else {
//            // 记录 UserServiceRecord
//            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "重置用户密保问题。";
//            UserServiceRecord userServiceRecord = new UserServiceRecord();
//            userServiceRecord.setUserId(userId);
//            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
//            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
//            userServiceRecord.setOperationContent("重置密保问题");
//            userServiceRecord.setComments(operation + "问题描述[" + passwordDesc + "]");
//            crmSummaryServiceClient.saveUserServiceRecord(userServiceRecord);
//
//            MapMessage mapMessage = MapMessage.successMessage("重置用户密保问题成功");
//            mapMessage.put("customerServiceRecord", userServiceRecord);
//            mapMessage.put("createTime", DateUtils.dateToString(new Date()));
//            return mapMessage;
//        }
//    }

    @RequestMapping(value = "updateusername.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserName() {

        long userId = getRequestLong("userId", -1L);
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String nameDesc = getRequestParameter("nameDesc", "").replaceAll("\\s", "");
        String gender = getRequestParameter("gender", "").replaceAll("\\s", "");
        String birth = getRequestParameter("birth", "").replaceAll("\\s", "");

        if (StringUtils.isBlank(userName) || !userName.matches(nameRegex) || StringUtils.isBlank(nameDesc))
            return MapMessage.errorMessage("用户新姓名：" + userName + "不合要求");

        User user = userLoaderClient.loadUser(userId);
        if ((user == null) || userName.equals(user.getProfile().getRealname()))
            return MapMessage.errorMessage("用户不存在或用户当前姓名与新姓名相同");

        if (!userServiceClient.changeName(userId, userName).isSuccess()) {
            return MapMessage.errorMessage("修改用户姓名失败");
        }

        if (StringUtils.isNoneBlank(gender) && !userServiceClient.changeGender(userId, gender).isSuccess()) {
            return MapMessage.errorMessage("修改用户性别失败");
        }

        String[] s = birth.split("/");
        if (s != null && s.length == 3) {
            Integer year = SafeConverter.toInt(s[0]);
            Integer month = SafeConverter.toInt(s[1]);
            Integer day = SafeConverter.toInt(s[2]);
            if (!userServiceClient.changeUserBirthday(userId, year, month, day).isSuccess()) {
                return MapMessage.errorMessage("修改用户生日失败");
            }
        }

        LogCollector.info("backend-general", MiscUtils.map("usertoken", user.getId(),
                "usertype", user.getUserType(),
                "platform", "crm",
                "version", "",
                "op", "change user name",
                "mod1", user.fetchRealname(),
                "mod2", userName,
                "mod3", user.getAuthenticationState(),
                "mod4", getCurrentAdminUser().getAdminUserName()));

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("管理员修改用户姓名");
        userServiceRecord.setComments("旧名:" + user.getProfile().getRealname() + "，新名：" + userName);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        MapMessage mapMessage = MapMessage.successMessage("修改用户姓名成功");
        mapMessage.set("customerServiceRecord", userServiceRecord);
        mapMessage.set("createTime", DateUtils.dateToString(new Date()));
        mapMessage.set("userName", userName);
        mapMessage.set("gender", gender);
        mapMessage.set("birth", birth);
        return mapMessage;
    }

//    //market app reset teacher password log
//    @RequestMapping(value = "marketresetpwdlog.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage addMarketResetPwdLog() {
//        String operation = getRequestString("operation");
//        long userId = getRequestLong("userId");
//        String passwordDesc = getRequestString("passwordDesc");
//        String password = getRequestString("password");
//        //log
//        addAdminLog(operation, userId, password, passwordDesc, null);
//        //send message
//        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
//        String payload = teacher.getProfile().getRealname() + "老师您好，您的密码已被重置为" + password + "，登录后可进行修改。如有问题，可拨打400-160-1717";
//        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
//        if (!StringUtils.isBlank(ua.getSensitiveMobile())) {
//            userSmsServiceClient.buildSms().to(teacher)
//                    .content(payload)
//                    .type(SmsType.MARKET_RESET_TEACHER_PWD)
//                    .send();
//        }
//        return MapMessage.successMessage();
//    }
    /************************ 查询相关 ******************************************************************/

    /**
     * 查询区域
     */
    @AdminAcceptRoles(getRoles = AdminPageRole.ALLOW_ALL)
    @RequestMapping(value = "regionlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> regionList(@RequestParam Integer regionCode) {
        Map<String, Object> message = new HashMap<>();
        Region regionAll = new Region();

        //返回时增加“全部”选项，小于0的regionCode值都为无效值
        regionAll.setName("全部");
        regionAll.setCode(-1);

        List<Region> regionList = new ArrayList<>();
        if (regionCode >= 0) {
            regionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(regionCode));
        }
        regionList.add(0, regionAll);

        message.put("regionList", regionList);

        return message;
    }

    /**
     * 查询区域下学校
     *
     * @param regionCode
     * @return
     */
    @AdminAcceptRoles(getRoles = AdminPageRole.ALLOW_ALL)
    @RequestMapping(value = "schoollist.vpage", method = RequestMethod.GET)
    @ResponseBody
    Map<String, Object> schoolList(@RequestParam Integer regionCode) {
        Map<String, Object> message = new HashMap<>();
        School schoolAll = new School();

        //返回时增加“全部”选项，小于0的regionCode值都为无效值schoolAll.name = "全部"
        schoolAll.setCname("全部");
        schoolAll.setId(-1L);

        List<School> schoolList = new ArrayList<>();
        if (regionCode >= 0) {
            List<School> schools = raikouSystem.querySchoolLocations(regionCode)
                    .enabled()
                    .transform()
                    .asList()
                    .stream()
                    .sorted(Comparator.comparing(School::getId))
                    .collect(Collectors.toList());
            schoolList.addAll(schools);
        }
        schoolList.add(0, schoolAll);

        message.put("schoolList", schoolList);

        return message;
    }

    /**
     * 查询用户登录记录
     */
    @RequestMapping(value = "userrecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String userRecord(@RequestParam Long userId, Model model) {

        int intervalDay = 6;
        Date startDate;
        Date endDate;
        boolean queryClassmates = false;

        if (isRequestPost()) {

            String startDateStr = getRequestParameter("startDate", "");
            String endDateStr = getRequestParameter("endDate", "");
            queryClassmates = getRequestBool("classmateQuery");

            FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");

            try {
                startDate = sdf.parse(startDateStr);
            } catch (Exception ignored) {
                startDate = DayRange.newInstance(System.currentTimeMillis()).getStartDate();
            }

            try {
                endDate = sdf.parse(endDateStr);
                endDate = DayRange.newInstance(endDate.getTime()).getEndDate();
            } catch (Exception ignored) {
                endDate = DayRange.newInstance(System.currentTimeMillis()).getEndDate();
            }

            Date latestEndDate = DateUtils.nextDay(startDate, intervalDay);
            if (latestEndDate.before(endDate))
                endDate = latestEndDate;

        } else {
            endDate = DayRange.newInstance(System.currentTimeMillis()).getEndDate();
            startDate = DateUtils.nextDay(new Date(), -intervalDay);
            startDate = DayRange.newInstance(startDate.getTime()).getStartDate();
        }

        Map<String, Object> conditionMap = new HashMap<>();
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
        conditionMap.put("userId", userId);
        conditionMap.put("startDate", startDate);
        conditionMap.put("endDate", endDate);

        List<UserRecordData> userRecords = new LinkedList<>();
        if (queryClassmates && clazz != null) {
            conditionMap.put("clazzId", clazz.getId());
            Set<Long> groupIds = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByStudentIdIncludeDisabled(userId)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .map(GroupStudentTuple::getGroupId)
                    .collect(Collectors.toSet());
            Set<Long> userIds = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByGroupIdsIncludeDisabled(groupIds)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .map(GroupStudentTuple::getStudentId)
                    .collect(Collectors.toSet());
            Date s = startDate;
            Date e = endDate;
            Map<Long, List<String>> resMap = uctUserService.queryUserRecordById(new ArrayList<>(userIds), DateUtils.dateToString(s, DateUtils.FORMAT_SQL_DATE), DateUtils.dateToString(e, DateUtils.FORMAT_SQL_DATE));
            if (resMap != null && !resMap.isEmpty()) {
                List<UserRecordData> recordList = new LinkedList<>();
                for (Map.Entry<Long, List<String>> entry : resMap.entrySet()) {
                    Long uid = entry.getKey();
                    List<String> userValue = entry.getValue();
                    if (CollectionUtils.isEmpty(userValue)) {
                        continue;
                    }
                    recordList.addAll(parseStringToUserRecord(userValue, uid));
                }
                userRecords.addAll(recordList
                        .stream()
                        .sorted(Comparator.comparing(UserRecordData::getCreateTime))
                        .collect(Collectors.toList()));
            }

        } else {
            Map<Long, List<String>> resMap = uctUserService.queryUserRecordById(Arrays.asList(userId), DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE), DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE));
            if (resMap != null && CollectionUtils.isNotEmpty(resMap.get(userId))) {
                List<UserRecordData> recordList = parseStringToUserRecord(resMap.get(userId), userId);
                userRecords.addAll(recordList
                        .stream()
                        .sorted(Comparator.comparing(UserRecordData::getCreateTime))
                        .collect(Collectors.toList()));
            }
        }
        List<Map<String, Object>> userRecordInfoList = userRecords.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time", r.getCreateTime());
                    map.put("ip", r.getIp());
                    map.put("region", r.getAddress());
                    map.put("userId", r.getUserId());
                    return map;
                })
                .collect(Collectors.toList());

        User user = userLoaderClient.loadUser(userId);
        String userName = (user == null) ? null : (user.getProfile() == null) ? null : user.getProfile().getRealname();

        model.addAttribute("conditionMap", conditionMap);
        model.addAttribute("userName", userName);
        model.addAttribute("userRecordInfoList", userRecordInfoList);
        model.addAttribute("intervalDay", intervalDay);
        model.addAttribute("queryClassmateShowFlag", clazz != null);
        model.addAttribute("queryClassmates", queryClassmates);
        return "crm/user/userrecord";
    }

    private List<UserRecordData> parseStringToUserRecord(List<String> datas, Long userId) {
        List<UserRecordData> recordList = new ArrayList<>();
        for (String data : datas) {
            String[] values = data.split("#");
            if (values == null || values.length < 3) {
                logger.warn("user record is not formate. string:" + data);
                continue;
            }
            UserRecordData record = new UserRecordData();
            record.setCreateTime(DateUtils.stringToDate(values[0], DateUtils.FORMAT_SQL_DATETIME));
            record.setIp(values[1]);
            record.setAddress(values[2]);
            record.setUserId(userId);
            recordList.add(record);
        }
        return recordList;
    }

    @RequestMapping(value = "findMobileMessagehomepage.vpage", method = RequestMethod.GET)
    public String findSmsMessageHomepage() {
        return "crm/user/mobilequery";
    }

    /**
     * 根据手机号查询发送的信息
     */
    @RequestMapping(value = "findMobileMessage.vpage", method = RequestMethod.POST)
    public String findSmsMessage(@RequestParam(value = "mobile", required = false) String mobile, Model model) {
        if (StringUtils.isBlank(mobile)) {
            getAlertMessageManager().addMessageError("手机号不能为空");
        }
        mobile = mobile.replaceAll("\\s", "").replaceAll("-", "");

        if (!getAlertMessageManager().hasMessageError()) {
            List<Map<String, Object>> userSmsMessages = smsLoaderClient.getSmsLoader().loadUserSmsMessage(mobile, 10)
                    .stream()
                    .map(sms -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("createTime", sms.getCreateTime());
                        info.put("smsType", SmsType.of(sms.getSmsType()));
                        info.put("smsContent", sms.getSmsContent());
                        info.put("status", sms.getStatus());
                        info.put("errorCode", sms.getErrorCode());
                        info.put("errorDesc", sms.getErrorDesc());
                        SmsClientType channel = null;
                        try {
                            channel = SmsClientType.valueOf(sms.getSmsChannel());
                        } catch (Exception ignored) {
                        }
                        if (channel == null) {
                            info.put("smsChannel", null);
                            info.put("verification", false);
                        } else {
                            info.put("smsChannel", channel.name());
                            info.put("verification", SmsClientCategory.verification_code == channel.getCategory() || SmsClientCategory.voice_verification_code == channel.getCategory());
                        }
                        info.put("consumed", Boolean.TRUE.equals(sms.getConsumed()));
                        info.put("receiveTime", sms.getReceiveTime());
                        info.put("sendTime", sms.getSendTime());
                        return info;
                    }).collect(Collectors.toList());
            if (userSmsMessages == null) {
                userSmsMessages = new ArrayList<>();
            }
            model.addAttribute("smsMessageList", userSmsMessages);
            model.addAttribute("queryMobilMessage_mobile", mobile);
        }

        return "crm/user/mobilequery";
    }

    @RequestMapping(value = "wechatnoticelist.vpage", method = RequestMethod.GET)
    public String getWechatNoticeList(Model model) {
        long userId = getRequestLong("userId");
        if (userId <= 0) {
            getAlertMessageManager().addMessageError("参数 userId 为空");
            return redirect("/crm/student/studentlist.vpage");
        }
        boolean isHistory = getRequestBool("isHistory");
        User user = null;
        List<WechatNoticeSnapshot> noticeSnaps = null;
        try {
            user = userLoaderClient.loadUser(userId);
            noticeSnaps = new ArrayList<>();
            if (user.getUserType().equals(UserType.STUDENT.getType())) {
                List<StudentParent> parents = parentLoaderClient.loadStudentParents(userId);
                for (StudentParent p : parents) {
                    noticeSnaps.addAll(wechatLoaderClient.loadWechatNoticeSnapshotByUserId(p.getParentUser().getId(), isHistory));
                }
            } else if (user.getUserType().equals(UserType.TEACHER.getType()) || user.getUserType().equals(UserType.PARENT.getType())) {
                noticeSnaps.addAll(wechatLoaderClient.loadWechatNoticeSnapshotByUserId(userId, isHistory));
            }
        } catch (Exception ex) {
            logger.warn("getWechatNoticeList - Excp : " + ex.getMessage());
        }
        model.addAttribute("user", user);
        model.addAttribute("notices", noticeSnaps);
        model.addAttribute("history", isHistory);
        return "/crm/user/wechatnoticelist";
    }

    @RequestMapping(value = "sendwechatnotice.vpage")
    @ResponseBody
    public int sendWechatNotice(Long id) {
        try {
            return wechatServiceClient.sendNoticeMessage(id);
        } catch (Exception ex) {
            logger.warn("sendWechatNotice - Excp : {}; id : {}", ex.getMessage(), id);
        }
        return 0;
    }

    @RequestMapping(value = "temppassword.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserPassword() {
        // 先加次数
        AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();

        // FIXME 判断是否超限
        String desc = getRequestString("desc");
        MapMessage checkResult = checkTempPasswordLimit(getCurrentAdminUser(), desc);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        adminCacheSystem.incTempPassword(currentAdminUser.getFakeUserId());

        Long userId = getRequestLong("userId");
        MapMessage mapMessage = new MapMessage();
        String password = "";
        if (userId > 0) {
            password = userLoaderClient.generateUserTempPassword(userId);
        }
        addAdminLog("generateUserTempPassword", userId, password, "crm", "ID:" + userId + ", password:" + password);
        mapMessage.setSuccess(true);
        mapMessage.add("password", password);
        return mapMessage;
    }

    private MapMessage checkTempPasswordLimit(AuthCurrentAdminUser currentAdminUser, String desc) {

        long emailAlertCount = 50L;
        long popupAlertCount = 30L;

        if (RuntimeMode.isDevelopment()) {
            emailAlertCount = 10L;
            popupAlertCount = 5L;
        }

        Long userId = currentAdminUser.getFakeUserId();
        long viewUserPhoneCount = adminCacheSystem.loadTempPasswordCount(userId);

        if (Objects.equals(viewUserPhoneCount, emailAlertCount)) {
            Map<String, Object> content = new HashMap<>();
            content.put("info", currentAdminUser.getAdminUserName() + "@" + currentAdminUser.getDepartmentName() + ",触发数量:" + viewUserPhoneCount);
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("list.rmg@17zuoye.com")
                    .cc("zhilong.hu@17zuoye.com;lining.zhang@17zuoye.com")
                    .subject("【" + RuntimeMode.getCurrentStage() + "】admin临时密码查看-高")
                    .content(content)
                    .send();
        }

        if (Objects.equals(viewUserPhoneCount, popupAlertCount)) {
            // 为空，表示第一次触发，需要操作者填写10字以上描述，
            if (StringUtils.isBlank(desc) || desc.length() < 10) {
                return MapMessage.errorMessage().set("popup", true);
            }

            // 表示已经填写描述，继续发送短信
            long tempPasswordCount = adminCacheSystem.loadTempPasswordCount(currentAdminUser.getFakeUserId());

            Map<String, Object> content = new HashMap<>();
            content.put("info", currentAdminUser.getAdminUserName() + "@" + currentAdminUser.getDepartmentName() + ",触发数量:" + tempPasswordCount + ",描述：" + desc);
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("list.rmg@17zuoye.com")
                    .cc("zhilong.hu@17zuoye.com;lining.zhang@17zuoye.com")
                    .subject("【" + RuntimeMode.getCurrentStage() + "】admin临时密码查看-低")
                    .content(content)
                    .send();
        }

        return MapMessage.successMessage();
    }


    /*
        used by agent
     */

    /**
     * 创建主副账号
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "createmainsubaccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createMainSubAccount(@RequestBody Map<String, Object> request) {
        long teacherId = SafeConverter.toLong(request.get("teacherId"));
        if (teacherId <= 0) {
            return MapMessage.errorMessage("错误的老师id");
        }
        long clazzId = SafeConverter.toLong(request.get("clazzId"));
        if (clazzId <= 0) {
            return MapMessage.errorMessage("错误的班级id");
        }
        String subjectText = SafeConverter.toString(request.get("subject"));
        Subject subject = Subject.ofWithUnknown(subjectText);
        if (subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("学科错误");
        }
        return teacherSystemClazzServiceClient.createSubTeacherForTeacherAndClazz(teacherId, clazzId, subject, OperationSourceType.crm);
    }


    @RequestMapping(value = "modifycredit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyCredit() {
        Long userId = getRequestLong("userId");
        String amountStr = getRequestParameter("amount", "amount");
        String comment = getRequestString("comment");
        if (userId == 0) {
            return MapMessage.errorMessage("用户id有误");
        }
        if (!NumberUtils.isNumber(amountStr)) {
            return MapMessage.errorMessage("输入的学分错误");
        }
        if (StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("请填写备注");
        }
        if (userLoaderClient.loadUser(userId) == null) {
            return MapMessage.errorMessage("用户" + userId + "不存在");
        }
        Integer amount = SafeConverter.toInt(amountStr);
        if (amount == 0) {
            return MapMessage.errorMessage("输入的学分为0,请重新输入");
        }

        CreditHistory creditHistory = new CreditHistory();
        creditHistory.setUserId(userId);
        creditHistory.setAmount(amount);
        creditHistory.setComment(comment);
        CreditType creditType = CreditType.Unknown;
        if (amount > 0) {
            creditType = CreditType.crm_modify_increase;
        } else if (amount < 0) {
            creditType = CreditType.crm_modify_decrease;
        }
        creditHistory.setType(creditType.getType());
        CreditChangeResult creditChangeResult = creditServiceClient.getCreditService().changeCredit(creditHistory);

        if (creditChangeResult == null || BooleanUtils.isFalse(creditChangeResult.getSuccess())) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userDelete() {
        long userId = getRequestLong("userId");
        String desc = getRequestString("desc");
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        String mobile = sensitiveUserDataServiceClient.loadUserMobile(userId);
        String mobileEncrypted = com.voxlearning.alps.extension.sensitive.codec.SensitiveLib.encodeMobile(mobile);
        MapMessage mapMessage = new MapMessage();
        String lock = "CrmUserController_delUser";
        AtomicLockManager.instance().acquireLock(lock);
        try {
            UserType userType = user.fetchUserType();
            switch (userType) {
                case STUDENT:
                    kickoutUser(user.getId(), "17Student", "17JuniorStu");
                    mapMessage = studentServiceClient.deleteStudent(user.getId());
                case PARENT:
                    kickoutUser(user.getId(), "17Parent", "17JuniorPar");
                    mapMessage = parentServiceClient.deleteParent(user.getId());
                    unbindWechat(user.getId(), userType);
                case TEACHER:
                    kickoutUser(userId, "17Teacher", "17JuniorTea");
                    mapMessage = teacherServiceClient.deleteTeacher(userId);
                    unbindWechat(userId, userType);
                    break;
                default:
                    return MapMessage.errorMessage("不支持的用户类型");
            }
            if (mapMessage.isSuccess()) {
                mapMessage = userServiceClient.disableUser(userId);
                if (mapMessage.isSuccess()) {
                    // 记录 UserServiceRecord
                    String operation = "删除/注销用户" + userId + "," + mobileEncrypted;
                    UserServiceRecord userServiceRecord = new UserServiceRecord();
                    userServiceRecord.setUserId(userId);
                    userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                    userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                    userServiceRecord.setOperationContent("删除用户");
                    userServiceRecord.setComments(operation + "；说明[" + desc + "]");
                    userServiceClient.saveUserServiceRecord(userServiceRecord);
                }
            }
            return mapMessage;
        } catch (CannotAcquireLockException e) {
            mapMessage.setSuccess(false);
            mapMessage.setInfo("正在处理，请不要重复提交！");
            return mapMessage;
        } finally {
            AtomicLockManager.getInstance().releaseLock(lock);
        }
    }

    private void kickoutUser(Long userId, String... appKeys) {
        for (String appKey : appKeys) {
            updateUserAppSessionKey(userId, appKey);
        }
    }

    private void unbindWechat(Long userId, UserType userType) {
        int wechatTypeId = WechatType.PARENT.getType();
        if (userType.equals(UserType.TEACHER))
            wechatTypeId = WechatType.TEACHER.getType();
        UserWechatRef userWechatRef = wechatLoaderClient.loadUserWechatRefByUserIdAndWechatType(userId,
                wechatTypeId);
        if (userWechatRef != null) {
            wechatServiceClient.unbindUserAndWechat(userWechatRef.getOpenId());
        }
        //todo unbind wechat mini programme
    }
}
