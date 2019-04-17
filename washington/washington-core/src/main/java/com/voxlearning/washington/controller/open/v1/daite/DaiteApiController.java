package com.voxlearning.washington.controller.open.v1.daite;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserAvatar;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.clazz.client.NewClazzServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.daite.DaiteApiConstants.*;

@Controller
@RequestMapping(value = "/v1/daite")
@Slf4j
public class DaiteApiController extends DaiteBaseApiController {

    public static final String REQ_STUDENT_ID = "student_id";
    public static final String REQ_PASSWORD = "password";
    public static final String REQ_USER_IDS = "user_ids";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private NewClazzServiceClient newClazzServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    /**
     * 检查用户名密码是否正确
     *
     * @return
     */
    @RequestMapping(value = "/checkUserPassword.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkUserPassword() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_IDS, "用户ID");
            validateRequired(REQ_PASSWORD, "用户密码");
            validateRequestNoSessionKey(REQ_USER_IDS, REQ_PASSWORD);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Set<Long> userIds = Arrays.stream(getRequestString(REQ_USER_IDS).split(",")).map(Long::valueOf).collect(Collectors.toSet());
        Map<Long, UserAuthentication> userAuthentications = userLoaderClient.loadUserAuthentications(userIds);
        // 先校验一下合作校
        for (UserAuthentication userAuthentication : userAuthentications.values()) {
            // 如果有非合作校得直接返回
            if (userAuthentication == null) {
                continue;
            }
            User user = raikouSystem.loadUser(userAuthentication.getId());
            if (isNotDaiteUserSchool(userAuthentication.getId(), UserType.of(user.getUserType()))) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
                return resultMap;
            }
        }
        // 校验密码
        String successIds = "";
        for (UserAuthentication userAuthentication : userAuthentications.values()) {
            if (userAuthentication == null) {
                continue;
            }
            String password = getRequestString(REQ_PASSWORD);
            if (!(userAuthentication.fetchUserPassword().match(password) || StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(userAuthentication.getId()), password))) {
                continue;
            }
            successIds = String.format("%s,%s", successIds, userAuthentication.getId());
        }
        if (successIds.equals("")) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOW_USER_ACCOUNT_MSG);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(REQ_USER_IDS, successIds.substring(1));
        return resultMap;
    }

    @RequestMapping(value = "/getSchoolInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getSchoolInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_SCHOOL_ID, "学校ID");
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequestNoSessionKey(REQ_SCHOOL_ID, REQ_TIMESTAMP);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        Map<String, Object> schoolInfo = new HashMap<>();
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_SCHOOL);
            return resultMap;
        }
        // 戴特合作校校验
        if (isNotDaiteSchool(schoolId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_DAITE);
            return resultMap;
        }
        schoolInfo.put("id", schoolId);
        schoolInfo.put("level", school.getLevel());
        schoolInfo.put("name", school.getCname());
        schoolInfo.put("main_name", school.getCmainName());
        schoolInfo.put("district", school.getSchoolDistrict());
        schoolInfo.put("district_code", school.getRegionCode());
        ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
        if (region != null) {
            schoolInfo.put("city_code", region.getCityCode());
            schoolInfo.put("province_code", region.getProvinceCode());
            schoolInfo.put("county_code", region.getCountyCode());
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo != null) {
            schoolInfo.put("address", schoolExtInfo.getAddress());
            schoolInfo.put("edu_system", schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS)
                .add("school_info", schoolInfo);
        return resultMap;
    }

    @RequestMapping(value = "/getClassInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getClassInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_CLASS_ID, "班级ID");
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequestNoSessionKey(REQ_CLASS_ID, REQ_TIMESTAMP);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(getRequestLong(REQ_CLASS_ID));
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        // 戴特合作校校验
        if (isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_DAITE);
            return resultMap;
        }
        Map<String, Object> clazzInfo = new HashMap<>();
        clazzInfo.put("id", clazz.getId());
        clazzInfo.put("name", clazz.getClassName());
        clazzInfo.put("jie", clazz.getJie());
        clazzInfo.put("class_level", this.classLevelToDaite(clazz.getClassLevel()));//转换成对外的年级
        clazzInfo.put("school_id", clazz.getSchoolId());
        clazzInfo.put("class_type", clazz.getClassType());
        List<Group> groups = groupLoaderClient.getGroupLoader().loadGroupsByClazzId(clazz.getId()).getUninterruptibly();
        clazzInfo.put("art_science_type", getScienceType(groups).name());
        clazzInfo.put("edu_system", clazz.getEduSystem().name());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add("class_info", clazzInfo);
        return resultMap;
    }

    @RequestMapping(value = "/upsertClassInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage upsertClassInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_SCHOOL_ID, "班所属学校");
            validateRequiredNumber(REQ_CLASS_TYPE, "班级的类型");
            validateEnum(REQ_CLASS_TYPE, "班级的类型", "1", "3");
            validateRequired(REQ_CLASS_NAME, "班的名称");
            validateRequired(REQ_CLASS_LEVEL, "班级level");
            validateRequiredNumber(REQ_CLASS_LEVEL, "班级level");
            validateRequired(REQ_EDU_SYSTEM, "班级的学制");
            validateEnum(REQ_EDU_SYSTEM, "班级的学制", "P6", "P5", "J3", "J4", "S3", "S4");
            validateRequestNoSessionKey(REQ_CLASS_ID, REQ_TIMESTAMP, REQ_SCHOOL_ID, REQ_CLASS_TYPE, REQ_TEACHER_ID, REQ_CLASS_NAME, REQ_CLASS_LEVEL, REQ_EDU_SYSTEM);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Integer clazzType = getRequestInt(REQ_CLASS_TYPE);
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        String eduSystem = getRequestString(REQ_EDU_SYSTEM);
        String clazzName = getRequestString(REQ_CLASS_NAME);
        String clazzLevelTmp = getRequestString(REQ_CLASS_LEVEL);
        String clazzLevel = this.classLevelTo17(clazzLevelTmp);//转换成17内部年级
        // 学制和level校验
        if (Arrays.stream(EduSystemType.of(eduSystem).getCandidateClazzLevel().split(",")).noneMatch(edu -> edu.equals(clazzLevel))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_EDU_LEVEL_DIFF);
            return resultMap;
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_SCHOOL);
            return resultMap;
        }

        Clazz clazz;
        if (clazzId > 0) {
            clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
                return resultMap;
            }
            // 戴特合作校校验
            if (isNotDaiteSchool(clazz.getSchoolId())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NO_DAITE);
                return resultMap;
            }
            if (!Objects.equals(clazz.getClassName(), clazzName)) {
                newClazzServiceClient.getNewClazzService().updateClazzName(clazzId, clazzName);
            }
        } else {
            if (isNotDaiteSchool(schoolId)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NO_DAITE);
                return resultMap;
            }
            ClassMapper mapper = new ClassMapper();
            mapper.setClazzName(clazzName);
            mapper.setClassLevel(clazzLevel);
            mapper.setSchoolId(schoolId);
            mapper.setEduSystem(eduSystem);
            MapMessage clazzResultMap;
            if (clazzType == 1) {
                clazzResultMap = clazzServiceClient.createSystemClazz(Collections.singletonList(mapper));
            } else {
                Long teacherId = getRequestLong(REQ_TEACHER_ID);
                TeacherDetail teacherDetail;
                if (teacherId == 0L || (teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId)) == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_NO_USER);
                    return resultMap;
                }
                if (!Objects.equals(teacherDetail.getTeacherSchoolId(), schoolId)) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_SCHOOL_USER_DIFF);
                    return resultMap;
                }
                clazzResultMap = clazzServiceClient.createWalkingClazz(teacherId, Collections.singletonList(mapper));
            }
            List<NeonatalClazz> neonatalClazzes;
            if (!clazzResultMap.isSuccess() || (neonatalClazzes = JsonUtils.fromJsonToList(JsonUtils.toJson(clazzResultMap.get("neonatals")), NeonatalClazz.class)) == null || neonatalClazzes.isEmpty()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, clazzResultMap.getInfo());
                return resultMap;
            }
            // 校验 其他情况的失败
            if (StringUtils.isNoneBlank(neonatalClazzes.get(0).getErrorMessage())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, neonatalClazzes.get(0).getErrorMessage());
                return resultMap;
            }
            clazzId = neonatalClazzes.get(0).getClazzId();
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).
                add("class_info", MapUtils.m("id", clazzId));
        return resultMap;
    }

    @RequestMapping(value = "/getUserInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_ID, "用户ID");
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequestNoSessionKey(REQ_USER_ID, REQ_TIMESTAMP);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long userId = getRequestLong(REQ_USER_ID);
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不存在, 请核实");
            return resultMap;
        }
        if (!Objects.equals(user.getWebSource(), userWebSource) && isNotDaiteUserSchool(user.getId(), UserType.of(user.getUserType()))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("realname", user.fetchRealname());
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(userId);
        userInfo.put("mobile", SensitiveLib.decodeMobile(userAuthentication.getSensitiveMobile()));
        if (user.isTeacher()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userId);
            userInfo.put("school_id", teacherDetail.getTeacherSchoolId());
            userInfo.put("subject", teacherDetail.getSubject() != null ? teacherDetail.getSubject().name() : "");
        } else if (user.isStudent()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (null != studentDetail.getClazz()) {//学生可能还没有加班
                userInfo.put("school_id", studentDetail.getClazz().getSchoolId());
            }
        }
        userInfo.put("avatar", getUserAvatarImgUrl(user));
        userInfo.put("gender", user.fetchGender().getCode());
        userInfo.put("user_type", user.getUserType());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS)
                .add("user_info", userInfo);
        return resultMap;
    }

    @RequestMapping(value = "/upsertUserInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage upsertUserInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequired(REQ_REALNAME, "用户姓名");
            validateMobileNumber(REQ_MOBILE);
            validateRequired(REQ_USER_GENDER, "性别");
            validateEnum(REQ_USER_GENDER, "性别", "M", "F", "N");
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_USER_ID, REQ_REALNAME, REQ_MOBILE, REQ_SCHOOL_ID, REQ_AVATAR, REQ_USER_GENDER, REQ_USER_TYPE, REQ_SUBJECT, REQ_PASSWORD);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long userId = getRequestLong(REQ_USER_ID);
        Integer userType = getRequestInt(REQ_USER_TYPE);
        String mobile = getRequestString(REQ_MOBILE);
        String realName = getRequestString(REQ_REALNAME);
        String avatar = getRequestString(REQ_AVATAR);
        String gender = getRequestString(REQ_USER_GENDER);
        String subject = getRequestString(REQ_SUBJECT);
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        String password = getRequestString(REQ_PASSWORD);
//        if (StringRegexUtils.isNotRealName(realName)) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, "姓名必须为全中文，请重新填写!");
//            return resultMap;
//        }
        if (StringUtils.isNoneBlank(avatar)) {
            if (Arrays.stream(UserAvatar.values()).map(UserAvatar::name).noneMatch(u -> Objects.equals(u, avatar))) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "头像不合规范");
                return resultMap;
            }
        }
        List<UserAuthentication> userAuthentications = null;
        // 校验一下手机号是否被暂用
        if (StringUtils.isNoneBlank(mobile)) {
            userAuthentications = userLoaderClient.loadMobileAuthentications(mobile);
            Long finalUserId = userId;
            long count = userAuthentications.stream().filter(u -> (u.getUserType() == UserType.of(userType) && !Objects.equals(u.getId(), finalUserId))).count();
            if (count > 0) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_IS_BIND_ERROR_MSG);
                return resultMap;
            }
        }
        if (schoolId > 0 && isNotDaiteSchool(schoolId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_DAITE);
            return resultMap;
        }
        if (userId > 0) {
            User user = raikouSystem.loadUser(userId);
            if (user == null) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "用户不存在, 请核实");
                return resultMap;
            }
            // 戴特合作校
            if (!Objects.equals(user.getWebSource(), userWebSource) && isNotDaiteUserSchool(user.getId(), UserType.of(user.getUserType()))) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
                return resultMap;
            }
            if (UserType.of(userType) == UserType.TEACHER) {
                TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
                if (teacher == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "老师用户不存在, 请核实");
                    return resultMap;
                }
            }
            if (!Objects.equals(realName, user.fetchRealname())) {
                userServiceClient.changeName(userId, realName);
            }
            if (!Objects.equals(gender, user.fetchGender().getCode())) {
                userServiceClient.changeGender(userId, gender);
            }
            // 当前手机号和用户自己的手机号不同
            if (userAuthentications != null && userAuthentications.isEmpty() && userAuthentications.stream().filter(u -> Objects.equals(u.getId(), user.getId())).count() == 0) {
                userServiceClient.updateEmailMobile(userId, null, mobile);
            }
            if (StringUtils.isNoneBlank(avatar) && !Objects.equals(user.fetchImageUrl(), UserAvatar.valueOf(avatar).getUrl())) {
                userServiceClient.userImageUploaded(userId, UserAvatar.valueOf(avatar).getUrl(), "");
            }
            if (StringUtils.isNoneBlank(password)) {
                UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(user.getId());
                userServiceClient.changePassword(user, null, password);
            }
        } else {
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.of(userType));
            neonatalUser.setUserType(UserType.of(userType));
            neonatalUser.setWebSource(userWebSource);
            if (StringUtils.isNoneBlank(mobile)) {
                neonatalUser.setMobile(mobile);
            }
            neonatalUser.setGender(gender);
            neonatalUser.setRealname(realName);
            if (StringUtils.isBlank(password)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "密码不能为空");
                return resultMap;
            }
            neonatalUser.setPassword(password);
            if (UserType.of(userType) == UserType.TEACHER) {
                // 校验一下
                try {
                    validateRequired(REQ_SUBJECT, "科目");
                    validateEnum(REQ_SUBJECT, "科目", Arrays.stream(Subject.values()).map(Subject::name).toArray());
                    validateRequired(REQ_SCHOOL_ID, "学校");
                } catch (IllegalArgumentException e) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, e.getMessage());
                    return resultMap;
                }
            }
            if (StringUtils.isNotEmpty(mobile)) {
                neonatalUser.setCode("17abzy");//设定超级验证码，底层如果填写了手机号码，需要校验验证码
            }
            MapMessage mapMessage = userServiceClient.registerUser(neonatalUser);
            if (!mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, StringUtils.isBlank(mapMessage.getInfo()) ? mapMessage.get("attributes") : mapMessage.getInfo());
                return resultMap;
            }
            User user = (User) mapMessage.get("user");
            userId = user.getId();

            //新增老师后，需要处理老师所属学校的k_subject信息
            if (neonatalUser.getUserType() == UserType.TEACHER) {//如果是老师，需要设置老师的学科以及学制信息
                School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                teacherServiceClient.setTeacherSubjectSchool(userId, Subject.of(subject), Ktwelve.of(school.getLevel()), schoolId);
                UserCache.evictUserCache(userId);
            }

            // 更新头像
            if (StringUtils.isNoneBlank(avatar)) {
                userServiceClient.userImageUploaded(userId, UserAvatar.valueOf(avatar).getUrl(), "");
            }
        }
        // 记录用户record
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorName("戴特");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setComments("修改用户信息");
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS)
                .add("user_info", MapUtils.m("id", userId));
        return resultMap;
    }

    @RequestMapping(value = "/getTeacherRelationInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getTeacherRelationInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_USER_ID, "用户ID");
            validateRequestNoSessionKey(REQ_USER_ID, REQ_TIMESTAMP);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        User user = raikouSystem.loadUser(getRequestLong(REQ_USER_ID));
        if (user == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不存在, 请核实");
            return resultMap;
        }
        // 戴特合作校
        if (!Objects.equals(user.getWebSource(), userWebSource) && isNotDaiteUserSchool(user.getId(), UserType.of(user.getUserType()))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        try {
            List<Map> relations = new ArrayList<>();
            List<Long> groupIds = new ArrayList<>();
            if (user.isStudent()) {
                groupIds = raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .findByStudentId(user.getId())
                        .stream()
                        .filter(e -> e.getGroupId() != null)
                        .map(GroupStudentTuple::getGroupId)
                        .distinct()
                        .collect(Collectors.toList());
            } else if (user.isTeacher()) {
                List<GroupTeacherTuple> groupRefs = raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .findByTeacherId(user.getId());
                groupIds = groupRefs.stream().map(GroupTeacherTuple::getGroupId).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(groupIds)) {
                Map<Long, Group> groupsMap = groupLoaderClient.getGroupLoader().loadGroups(groupIds).get();
                List<Long> clazzIds = groupsMap.values().stream().map(Group::getClazzId).collect(Collectors.toList());
                Map<Long, Clazz> clazzsMap = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(clazzIds)
                        .stream()
                        .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                ArtScienceType scienceType = getScienceType(groupsMap.values().stream().collect(Collectors.toList()));
                groupsMap.values().forEach(g -> {
                    Clazz clazz = clazzsMap.get(g.getClazzId());
                    if (null != clazz) {
                        Map<String, Object> relationMap = new HashMap<>();
                        relationMap.put("subject", g.getSubject().name());
                        relationMap.put("class_id", clazz.getId());
                        relationMap.put("name", clazz.getClassName());
                        relationMap.put("class_level", SafeConverter.toInt(this.classLevelToDaite(clazz.getClassLevel())));
                        relationMap.put("jie", clazz.getJie());
                        relationMap.put("school_id", clazz.getSchoolId());
                        relationMap.put("class_type", clazz.getClazzType() != null ? clazz.getClazzType().getType() : "");
                        relationMap.put("art_science_type", scienceType.name());
                        relations.add(relationMap);
                    }
                });

            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("relation", relations);
            return resultMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
    }

    @RequestMapping(value = "/getStudentFamilyRelationInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudentFamilyRelationInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_USER_ID, "学生ID");
            validateRequestNoSessionKey(REQ_USER_ID, REQ_TIMESTAMP);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long userId = getRequestLong(REQ_USER_ID);
        User user = raikouSystem.loadUser(userId);
        if (user == null || !user.isStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不存在, 请核实");
            return resultMap;
        }
        // 戴特合作校
        if (!Objects.equals(user.getWebSource(), userWebSource) && isNotDaiteUserSchool(user.getId(), UserType.of(user.getUserType()))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        List<Map> relations = new ArrayList<>();
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(Collections.singleton(userId)).get(userId);
        if (studentParentRefs != null && !studentParentRefs.isEmpty()) {
            studentParentRefs.forEach(s ->
                    relations.add(MapUtils.m("user_id", s.getParentId(), "relation", s.getCallName()))
            );
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("student_family_relation", relations);
        return resultMap;
    }

    @RequestMapping(value = "/getParentFamilyRelationInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getParentFamilyRelationInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_USER_ID, "家长ID");
            validateRequestNoSessionKey(REQ_USER_ID, REQ_TIMESTAMP);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long userId = getRequestLong(REQ_USER_ID);
        User user = raikouSystem.loadUser(userId);
        if (user == null || !user.isParent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不存在, 请核实");
            return resultMap;
        }
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(userId);

        List<Map> relations = new ArrayList<>();
        if (studentParentRefs != null && !studentParentRefs.isEmpty()) {
            for (StudentParentRef ref : studentParentRefs) {
                if (this.isNotDaiteUserSchool(ref.getStudentId(), UserType.STUDENT)) {//如果这个学生不是daite合作校的学生，则过滤掉
                    continue;
                }
                relations.add(MapUtils.m("user_id", ref.getStudentId(), "relation", ref.getCallName()));
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("parant_family_relation", relations);
        return resultMap;
    }

    @RequestMapping(value = "/updateFamilyRelation.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateFamilyRelation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_RELATIONS, "关系");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_STUDENT_ID, REQ_RELATIONS);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        User user = raikouSystem.loadUser(getRequestLong(REQ_STUDENT_ID));
        if (!user.isStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "用户不存在, 请核实");
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteUserSchool(user.getId(), UserType.STUDENT)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        List<Map> relations = JsonUtils.fromJsonToList(getRequestString(REQ_RELATIONS), Map.class);
        if (relations == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "关系内容有误");
            return resultMap;
        }
        // 校验relations
        List<Relation> relationList = new ArrayList<>();
        for (Map relation : relations) {
            Long parentId;
            User parent;
            if (CallName.of(ConversionUtils.toString(relation.get("relation"))) == null
                    || (parentId = ConversionUtils.toLong(relation.get("parent_id"))) < 0
                    || (parent = raikouSystem.loadUser(parentId)) == null
                    || !parent.isParent()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "内容有误，请核对");
                return resultMap;
            }
            relationList.add(new Relation(parentId, CallName.of(ConversionUtils.toString(relation.get("relation")))));
        }
        try {
            Map<Long, Relation> relationListParentIdMap = relationList.stream().collect(Collectors.toMap(r -> r.parentId, Function.identity(), (u, v) -> {
                throw new IllegalArgumentException("家长ID重复，请核对");
            }));
            List<StudentParentRef> studentParents = studentLoaderClient.loadStudentParentRefs(user.getId());
            // 分三部分更新的一部分，新增的一部分，删除一部分
            // parentId 维度转map
            Map<Long, StudentParentRef> parentIdsRefMap = studentParents.stream().collect(Collectors.toMap(s -> s.getParentId(), Function.identity()));
            List<StudentParentRef> deleteRelations = studentParents.stream().filter(s -> !relationListParentIdMap.containsKey(s.getParentId())).collect(Collectors.toList());
            List<Relation> addRelations = new ArrayList<>();
            List<Relation> updateRelations = new ArrayList<>();
            relationList.forEach(s -> {
                if (!parentIdsRefMap.containsKey(s.parentId)) {
                    addRelations.add(s);
                } else {
                    if (!Objects.equals(parentIdsRefMap.get(s.parentId).getCallName(), s.callName.name())) {
                        updateRelations.add(s);
                    }
                }
            });
            if (!updateRelations.isEmpty()) {
                updateRelations.forEach(m -> {
                    MapMessage mapMessage = parentServiceClient.setParentCallName(m.parentId, user.getId(), m.callName);
                    if (!mapMessage.isSuccess()) {
                        throw new IllegalArgumentException(mapMessage.getInfo());
                    }
                });
            }
            if (!addRelations.isEmpty()) {
                addRelations.forEach(m -> {
                    MapMessage mapMessage = parentServiceClient.bindExistingParent(user.getId(), m.parentId, false, m.callName.name());
                    if (!mapMessage.isSuccess()) {
                        throw new IllegalArgumentException(mapMessage.getInfo());
                    }
                });
            }
            if (!deleteRelations.isEmpty()) {
                deleteRelations.forEach(m -> parentServiceClient.disableStudentParentRef(m));
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        // 记录用户record
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(user.getId());
        userServiceRecord.setOperatorName("戴特");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setComments("修改家长关系");
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/getUserImageUrl.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserImageUrl() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_USER_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        int userType = getRequestInt(REQ_USER_TYPE);
        List<Map> userImage = new ArrayList<>();
        Arrays.stream(UserAvatar.values()).filter(u -> u.getUserType() == userType).forEach(u ->
                userImage.add(MapUtils.m("name", u.name(), "id", u.getKey(), "cname", u.getName(), "url", getUserAvatarImgUrl(u.getUrl()), "user_type", u.getUserType()))
        );
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("user_image", userImage);
        return resultMap;
    }

    /**
     * 老师加班
     *
     * @return
     */
    @RequestMapping(value = "/insertTeacherRelation.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage insertTeacherRelation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_CLASS_ID, "班级的ID");
            validateRequiredNumber(REQ_TEACHER_ID, "老师的ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_CLASS_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        if (!clazz.isSystemClazz()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "非行政班");
            return resultMap;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER);
            return resultMap;
        }
        //老师没有学科信息
        if (null == teacherDetail.getSubject()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_TEACHER_SUBJECT);
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteUserSchool(teacherId, UserType.TEACHER) || isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        if (teacherLoaderClient.isTeachingClazz(teacherId, clazzId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_CLAZZ_TEACHER_EXIST);
            return resultMap;
        }
        // 老师所在的学校和要加入的班级的学校时候不一致
        if (!Objects.equals(clazz.getSchoolId(), teacherDetail.getTeacherSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SCHOOL_USER_DIFF);
            return resultMap;
        }
        // 老师不可以跨学段教学
        if (clazz.getEduSystem() != null && teacherDetail.getKtwelve() != null && clazz.getEduSystem().getKtwelve() != teacherDetail.getKtwelve()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_KTWELVE_USER_DIFF);
            return resultMap;
        }
        MapMessage mapMessage = clazzServiceClient.teacherJoinSystemClazz(teacherDetail.getId(), clazz.getId(), OperationSourceType.wap);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
        // 记录用户record
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorName("戴特");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师换班.name());
        userServiceRecord.setComments("老师加班, 班级ID:" + clazzId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 老师退班
     *
     * @return
     */
    @RequestMapping(value = "/deleteTeacherRelation.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteTeacherRelation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_CLASS_ID, "班级的ID");
            validateRequiredNumber(REQ_TEACHER_ID, "老师ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_CLASS_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER);
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteUserSchool(teacherId, UserType.TEACHER) || isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        if (!teacherLoaderClient.isTeachingClazz(teacherId, clazzId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_TEACHER_CLAZZ);
            return resultMap;
        }

        MapMessage mapMessage = clazzServiceClient.teacherExitSystemClazz(teacherId, clazzId, true, OperationSourceType.wap);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorName("戴特");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师换班.name());
        userServiceRecord.setComments("老师退班, 班级ID:" + clazzId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 学生加班
     *
     * @return
     */
    @RequestMapping(value = "/insertStudentRelation.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage insertStudentRelation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_CLASS_ID, "班级的ID");
            validateRequiredNumber(REQ_STUDENT_ID, "学生的ID");
            validateRequiredNumber(REQ_TEACHER_ID, "老师的ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_CLASS_ID, REQ_STUDENT_ID, REQ_TEACHER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_TEACHER);
            return resultMap;
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_STUDENT);
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteUserSchool(teacherId, UserType.TEACHER) || isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        // 当前班级是否属于该老师
        if (!teacherLoaderClient.isTeachingClazz(teacherId, clazzId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_TEACHER_CLAZZ);
            return resultMap;
        }
        // 学生已有班级的情况下
        if (student.getClazz() != null) {
            List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
            if (clazz.isWalkingClazz() && studentGroups.stream().anyMatch(g -> Objects.equals(g.getClazzId(), clazzId))) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_CLAZZ_STUDENT_EXIST);
                return resultMap;
            }
            // 一个学生只能在一个行政班
            if (clazz.isPublicClazz() && student.getClazz().isPublicClazz() && !Objects.equals(student.getClazz().getId(), clazz.getId())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SYSTEM_STUDENT_DIFF);
                return resultMap;
            }
            // 学生所在学校
            if (!Objects.equals(student.getClazz().getSchoolId(), clazz.getSchoolId())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SCHOOL_STUDENT_DIFF);
                return resultMap;
            }
            // 学生不能加入不同级的班级
            if (student.getClazz().getClazzLevel() != clazz.getClazzLevel()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_LEVEL_STUDENT_DIFF);
                return resultMap;
            }
        }
        MapMessage mapMessage = studentSystemClazzServiceClient.studentJoinClazz(studentId, teacherId, clazzId, true, OperationSourceType.wap);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(studentId);
        userServiceRecord.setOperatorName("戴特");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师换班.name());
        userServiceRecord.setComments("学生加班, 班级ID:" + clazzId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 学生退班
     *
     * @return
     */
    @RequestMapping(value = "/deleteStudentRelation.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteStudentRelation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_CLASS_ID, "班级的ID");
            validateRequiredNumber(REQ_STUDENT_ID, "学生ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_CLASS_ID, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_STUDENT);
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteUserSchool(studentId, UserType.STUDENT) || isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        if (studentGroups != null && studentGroups.stream().noneMatch(g -> Objects.equals(g.getClazzId(), clazzId))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_CLAZZ_STUDENT_NOT_EXIST);
            return resultMap;
        }
        MapMessage mapMessage = clazzServiceClient.studentExitSystemClazz(studentId, clazzId);
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(studentId);
        userServiceRecord.setOperatorName("戴特");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师换班.name());
        userServiceRecord.setOperatorName("");
        userServiceRecord.setComments("学生退班, 班级ID:" + clazzId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 通过学校id查询所有班级
     *
     * @return
     */

    @RequestMapping(value = "/getSchoolClazz.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getSchoolClazz() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_SCHOOL_ID, "学校ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_SCHOOL_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        // 戴特合作校
        if (isNotDaiteSchool(schoolId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        List<Clazz> classList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId).toList();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("clazz", classList.stream().map(c ->
                MapUtils.m("id", c.getId(),
                        "level", c.getClassLevel(),
                        "name", c.getClassName(),
                        "class_type", c.getClazzType() != null ? c.getClazzType().getType() : 0,
                        "edu_system", c.getEduSystem() != null ? c.getEduSystem().name() : "")).collect(Collectors.toList())
        );
        return resultMap;
    }

    /**
     * 通过班级id查询所有老师
     *
     * @return
     */

    @RequestMapping(value = "/getClazzTeachersInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzTeachersInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_CLASS_ID, "班级ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_CLASS_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(clazzId);
        Map<Long, List<Long>> teachersClazzMap = teacherLoaderClient.loadTeachersClazzIds(clazzTeachers.stream().map(c -> c.getTeacher().getId()).collect(Collectors.toList()));
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("teachers",
                clazzTeachers.stream().map(
                        c -> MapUtils.m("id", c.getTeacher().getId(),
                                "name", c.getTeacher().fetchRealname(),
                                "subject", (c.getTeacher().getSubject() != null ? c.getTeacher().getSubject().name() : ""),
                                "clazz", teachersClazzMap.get(c.getTeacher().getId())
                        )
                ).collect(Collectors.toList()));
        return resultMap;
    }

    /**
     * 通过班级id查询所有学生
     *
     * @return
     */

    @RequestMapping(value = "/getClazzStudentsInfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzStudentsInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_CLASS_ID, "班级ID");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_CLASS_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long clazzId = getRequestLong(REQ_CLASS_ID);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_CLAZZ);
            return resultMap;
        }
        // 戴特合作校
        if (isNotDaiteSchool(clazz.getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_USER_DAITE);
            return resultMap;
        }
        List<User> students = studentLoaderClient.loadClazzStudents(clazzId);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("students",
                students.stream().map(
                        s -> MapUtils.m("id", s.getId(),
                                "name", s.fetchRealname()
                        )
                ).collect(Collectors.toList()));
        return resultMap;
    }


    /**
     * 通过学校id和手机号查询老师信息
     *
     * @return
     */
    @RequestMapping(value = "/getTeacherByMobile.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getTeacherByMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_SCHOOL_ID, "学校id");
            validateRequiredNumber(REQ_MOBILE, "手机号");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_SCHOOL_ID, REQ_MOBILE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        // 戴特合作校
        long schoolId = getRequestLong(REQ_SCHOOL_ID);
        if (isNotDaiteSchool(schoolId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_DAITE);
            return resultMap;
        }
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(getRequestString(REQ_MOBILE), UserType.TEACHER);
        if (userAuthentication == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NO_TEACHER);
            return resultMap;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userAuthentication.getId());
        if (!Objects.equals(teacherDetail.getTeacherSchoolId(), schoolId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "老师学校不匹配");
            return resultMap;
        }
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("id", teacherDetail.getId())
                .add("name", teacherDetail.fetchRealname())
                .add("subject", teacherDetail.getSubject() != null ? teacherDetail.getSubject().name() : "");
    }

    /**
     * 通过区域id查询学校
     *
     * @return
     */
    @RequestMapping(value = "/getSchoolsByRegionCode.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getSchoolsByRegionCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequired(REQ_REGION_PCODE, "区域code");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_REGION_PCODE);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Integer pcode = getRequestInt(REQ_REGION_PCODE);

        List<School> schools = raikouSystem.querySchoolLocations(pcode)
                .enabled()
                .waitingSuccess()
                .filter(s -> s.getType() != SchoolType.TRAINING.getType() && s.getType() != SchoolType.CONFIDENTIAL.getType())
                .transform()
                .asList();

        List<Map> schoolList = new ArrayList<>();
        schools.forEach(s ->
                schoolList.add(MapUtils.m("id", s.getId(), "name", s.getCname(), "level", s.getLevel(), "regionCode", s.getRegionCode()))
        );
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_SCHOOL_LIST, schoolList);
    }

    @RequestMapping(value = "/getAllRegion.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getAllRegion() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_TIMESTAMP, "时间戳");
            validateRequiredNumber(REQ_REGION_PCODE, "区域PCODE");
            validateRequestNoSessionKey(REQ_TIMESTAMP, REQ_REGION_PCODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        List<Map<String, Object>> regionList = new LinkedList<>();
        Integer pcode = getRequestInt(REQ_REGION_PCODE);
        List<ExRegion> regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        if (CollectionUtils.isNotEmpty(regionAll)) {
            for (ExRegion exRegion : regionAll) {
                Map<String, Object> region = new HashMap<>();
                region.put("regionCode", exRegion.getCode());
                region.put("regionName", exRegion.getName());
                regionList.add(region);
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_REGION_LIST, regionList);
        return resultMap;
    }

    private class Relation {
        Relation(Long parentId, CallName callName) {
            this.parentId = parentId;
            this.callName = callName;
        }

        private Long parentId;
        private CallName callName;
    }
}