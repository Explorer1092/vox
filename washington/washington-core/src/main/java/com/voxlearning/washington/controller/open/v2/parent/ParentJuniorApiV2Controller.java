package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 中学家长端app
 *
 * @author chongfeng.qi
 * @date 20181123
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/junior")
public class ParentJuniorApiV2Controller extends AbstractParentApiController {

    private static final String BIND_COUNT = "junior_parent_bind_count_";

    private static int DAY_30_SECOND = 30 * 24 * 60 * 60; // 30天

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private WashingtonCacheSystem washingtonCacheSystem;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private NewKuailexueLoaderClient newKuailexueLoaderClient;

    /**
     * 中学家长端登录
     *
     * @return
     */
    @RequestMapping(value = "login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage login() {
        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String mobile = getRequestString(REQ_USER_CODE).trim();
        String code = getRequestString(REQ_VERIFY_CODE).trim();
        if (!MobileRule.isMobile(mobile)) {
            return failMessage("手机号格式错误");
        }
        SmsType smsType = SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE;
        MapMessage mapMessage;
        if (AppAuditAccounts.isJuniorParentAuditAccount(mobile) && code.equals("1234")) {
            mapMessage = MapMessage.successMessage();
        } else {
            mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
        }
        // 校验验证码
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        User parent = userLoaderClient.loadUserByToken(mobile).stream().filter(p -> p.fetchUserType() == UserType.PARENT).findFirst().orElse(null);
        boolean create = false;
        // 手机号未注册家长
        if (parent == null) {
            create = true;
            NeonatalUser neonatalUser = parentRegisterHelper.initChannelCParent(mobile, RoleType.ROLE_PARENT, "17JuniorPar");
            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                return failMessage(message.getInfo());
            }
            parent = (User) message.get("user");
        }
        mapMessage = successMessage();
        // 获取用户的登录信息
        mapMessage.add(RES_USER_TYPE, RoleType.ROLE_PARENT.getType());
        mapMessage.add(RES_USER_ID, parent.getId());
        mapMessage.add(RES_AVATAR_URL, getUserAvatarImgUrl(parent));
        mapMessage.add(RES_SESSION_KEY, attachUser2RequestApp(parent.getId()));
        mapMessage.add(REQ_USER_CODE, mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));

        // 读取家长的孩子信息
        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        List<Map<String, Object>> studentsInfo = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(students)) {
            wrapperStudentInfo(students.stream().map(User::getId).collect(Collectors.toList()), studentsInfo);
        }
        mapMessage.add(RES_CLAZZ_STUDENTS, studentsInfo);
        // 记录登录信息
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(parent.getId(),
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.wechat,
                false,
                getAppType());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
        String userPass = ua.getPassword();
        getWebRequestContext().saveAuthenticationStates(-1, parent.getId(), userPass, RoleType.ROLE_PARENT);
        String sys = getRequestString(REQ_SYS);
        String model = getRequestString(REQ_MODEL);
        String deviceId = getRequestString(REQ_IMEI);

        // 登录成功，记录设备号
        asyncFootprintServiceClient.getAsyncFootprintService().recordUserDeviceInfo(parent.getId(), deviceId, sys, model);
        return mapMessage.add(REQ_RESULT_PARENT_IS_CREATE, create);
    }

    /**
     * 中学家长个人信息
     *
     * @return
     */
    @RequestMapping(value = "profile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage profile() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage("请重新登录");
        }
        MapMessage mapMessage = successMessage();
        // 获取用户的登录信息
        mapMessage.add(RES_USER_TYPE, RoleType.ROLE_PARENT.getType());
        mapMessage.add(RES_USER_ID, parent.getId());
        mapMessage.add(RES_AVATAR_URL, getUserAvatarImgUrl(parent));
        mapMessage.add(RES_SESSION_KEY, getRequestString(RES_SESSION_KEY));
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parent.getId());
        if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
            mapMessage.add(REQ_USER_CODE, ObjectUtils.get(() -> SensitiveLib.decodeMobile(userAuthentication.getSensitiveMobile()).replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")));
        }
        // 读取家长的孩子信息
        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        List<Map<String, Object>> studentsInfo = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(students)) {
            wrapperStudentInfo(students.stream().map(User::getId).collect(Collectors.toList()), studentsInfo);
        }
        mapMessage.add(RES_CLAZZ_STUDENTS, studentsInfo);
        // jpush_tag
        Set<String> tagSet = getUserMessageTagList(parent.getId());
        mapMessage.add(RES_JPUSH_TAGS, tagSet);
        return mapMessage;
    }

    /**
     * 家长绑定孩子
     *
     * @return
     */
    @RequestMapping(value = "bindstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindStudent() {
        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequired(REQ_PASSWORD, "密码");
            validateRequest(REQ_USER_CODE, REQ_PASSWORD);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User currentParent = getCurrentParent();
        if (currentParent == null) {
            return failMessage("请重新登录");
        }
        // 校验一个月内是否绑定了三次
        int bindCount = SafeConverter.toInt(washingtonCacheSystem.CBS.persistence.get(BIND_COUNT + currentParent.getId()).getValue());
        if (bindCount >= 3) {
            return failMessage("最多可查看3个不同账号的考试信息，如有问题请联系客服").add(RES_OVER_LIMIT, true);
        }
        String userCode = getRequestString(REQ_USER_CODE).trim();
        String userPassword = getRequestString(REQ_PASSWORD).trim();
        UserAuthentication studentAuthentication = null;
        if (MobileRule.isMobile(userCode)) {
            studentAuthentication = userLoaderClient.loadMobileAuthentication(userCode, UserType.STUDENT);
        } else {
            long userId = SafeConverter.toLong(userCode);
            if (userId > 0) {
                studentAuthentication = userLoaderClient.loadUserAuthentication(userId);
            }
        }
        if (studentAuthentication == null || !Objects.equals(studentAuthentication.getUserType(), UserType.STUDENT)) {
            return failMessage("请输入正确的一起中学学生账号");
        }
        if (!studentAuthentication.verifyPassword(userPassword)) {
            return failMessage("学生密码输入错误");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentAuthentication.getId());
        if (studentDetail.getClazz() != null && studentDetail.isPrimaryStudent()) {
            return failMessage("孩子是小学生，请下载家长通");
        }
        Set<Long> studentIds = parentLoaderClient.loadParentStudentRefs(currentParent.getId()).stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
        // 只判断中学生
        List<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values().stream().filter(student -> !student.isPrimaryStudent()).collect(Collectors.toList());
        if (studentDetails.size() >= 3) {
            return failMessage("家长绑定孩子超过上限，如有疑问，请联系客服");
        }
        // 绑定
        MapMessage mapMessage = parentServiceClient.bindExistingParent(studentAuthentication.getId(), currentParent.getId(), true, "");
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        mapMessage = successMessage();
        mapMessage.add(RES_USER_TYPE, RoleType.ROLE_PARENT.getType());
        mapMessage.add(RES_USER_ID, currentParent.getId());
        List<Map<String, Object>> studentsInfo = new ArrayList<>();
        // 封装返回用户信息
        wrapperStudentInfo(Collections.singletonList(studentAuthentication.getId()), studentsInfo);
        mapMessage.add(RES_CLAZZ_STUDENTS, studentsInfo);
        // 记录绑定次数
        washingtonCacheSystem.CBS.persistence.set(BIND_COUNT + currentParent.getId(), DAY_30_SECOND, ++bindCount);
        return mapMessage;
    }

    /**
     * 获取当前家长的孩子的信息
     *
     * @return
     */
    @RequestMapping(value = "parentstudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentStudents() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User currentParent = getCurrentParent();
        if (currentParent == null) {
            return failMessage("请重新登录");
        }
        MapMessage mapMessage = successMessage();
        mapMessage.add(RES_USER_TYPE, RoleType.ROLE_PARENT.getType());
        List<User> students = studentLoaderClient.loadParentStudents(currentParent.getId());
        List<Map<String, Object>> studentsInfo = new ArrayList<>();
        // 封装返回用户信息
        wrapperStudentInfo(students.stream().map(User::getId).collect(Collectors.toList()), studentsInfo);
        mapMessage.add(RES_CLAZZ_STUDENTS, studentsInfo);
        return mapMessage;
    }

    /**
     * 解绑家长和学生的关系
     *
     * @return
     */
    @RequestMapping(value = "unbindstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unBindStudent() {
        try {
            validateRequired(RES_STUDENT_ID, "学生id");
            validateRequest(RES_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User currentParent = getCurrentParent();
        if (currentParent == null) {
            return failMessage("请重新登录");
        }
        MapMessage mapMessage = successMessage();
        long studentId = getRequestLong(RES_STUDENT_ID);
        StudentParentRef studentParentRef = ObjectUtils.get(studentLoaderClient.loadStudentParentRefs(studentId).stream().filter(ref -> currentParent.getId().equals(ref.getParentId())).findFirst()::get);
        if (studentParentRef == null) {
            return failMessage("学生家长未绑定");
        }
        parentServiceClient.disableStudentParentRef(studentParentRef);
        // 返回数据结构
        mapMessage.add(RES_USER_TYPE, RoleType.ROLE_PARENT.getType());
        mapMessage.add(RES_USER_ID, currentParent.getId());
        List<User> students = studentLoaderClient.loadParentStudents(currentParent.getId());
        List<Map<String, Object>> studentsInfo = new ArrayList<>();
        // 封装返回用户信息
        wrapperStudentInfo(students.stream().filter(s -> !s.getId().equals(studentId)).map(User::getId).collect(Collectors.toList()), studentsInfo);
        mapMessage.add(RES_CLAZZ_STUDENTS, studentsInfo);
        return mapMessage;
    }

    /**
     * 清除家长绑定次数
     *
     * @return
     */
    @RequestMapping(value = "clearbindcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearBindCount() {
        long parentId = getRequestLong(RES_PARENT_ID);
        String password = getRequestString(RES_PASSWORD);
        if (!"17zuoye.com".equals(password)) {
            return failMessage("调用被禁止");
        }
        if (parentId == 0) {
            return failMessage("家长id为空");
        }
        Boolean delete = washingtonCacheSystem.CBS.persistence.delete(BIND_COUNT + parentId);
        if (delete != null && delete) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    /**
     * 封装学生信息
     *
     * @param studentIds   学生ids
     * @param studentsInfo
     */
    private void wrapperStudentInfo(List<Long> studentIds, List<Map<String, Object>> studentsInfo) {
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        List<Long> schoolIds = studentDetailMap.values().stream().map(StudentDetail::getClazz).filter(Objects::nonNull).map(Clazz::getSchoolId).collect(Collectors.toList());
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
        studentDetailMap.forEach((k, v) -> {
            // 只要求返回中学的学生
            if (v.isPrimaryStudent() && v.getClazz() != null) {
                return;
            }
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put(RES_STUDENT_ID, k);
            studentInfo.put(RES_STUDENT_NAME, v.fetchRealname());
            studentInfo.put("student_gender", v.fetchGender().getDescription());
            studentInfo.put("student_avatar", getUserAvatarImgUrl(v));
            Clazz clazz = v.getClazz();
            SchoolExtInfo schoolExtInfo = ObjectUtils.get(() -> schoolExtInfoMap.get(clazz.getSchoolId()));
            if (schoolExtInfo != null) {
                studentInfo.put(RES_SCHOOL_ID, clazz.getSchoolId());
                studentInfo.put(RES_IS_SCAN_FLAG, schoolExtInfo.isScanMachineFlag());
                studentInfo.put(RES_CLAZZ_ID, clazz.getId());
                studentInfo.put(RES_CLAZZ_LEVEL, clazz.getClazzLevel().getDescription());
                studentInfo.put(RES_CLAZZ_NAME, clazz.formalizeClazzName());
            }
            School school = ObjectUtils.get(() -> schoolMap.get(clazz.getSchoolId()));
            ;
            ExRegion exRegion;
            if (school != null && (exRegion = raikouSystem.loadRegion(school.getRegionCode())) != null) {
                studentInfo.put(RES_SCHOOL_NAME, school.getCname());
                studentInfo.put(RES_COUNTRY_CODE, exRegion.getCountyCode());
                studentInfo.put(RES_CITY_CODE, exRegion.getCityCode());
                studentInfo.put(RES_PROVINCE_CODE, exRegion.getProvinceCode());
            }
            // 查询klx学生
            KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(k);
            if (klxStudent != null) {
                studentInfo.put(RES_KLX_SCAN_NUMBER, klxStudent.getScanNumber());
                studentInfo.put(RES_STUDENT_NUMBER, klxStudent.getStudentNumber());
                studentInfo.put("klx_id", klxStudent.getId());
            }
            studentsInfo.add(studentInfo);
        });
    }

}
