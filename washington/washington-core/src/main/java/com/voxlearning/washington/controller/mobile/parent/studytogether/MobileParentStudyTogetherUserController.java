package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.UserAvatar;
import com.voxlearning.utopia.service.parent.api.StudyTogetherLoader;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.washington.helpers.ParentRegisterHelper;
import com.voxlearning.washington.helpers.ParentStudentCallNameHelper;
import com.voxlearning.washington.helpers.ValidateStudentIdHelper;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2018-4-12
 */
@Controller
@RequestMapping(value = "/parentMobile/study_together/user/")
public class MobileParentStudyTogetherUserController extends AbstractMobileParentStudyTogetherController {

    //一起学注册C端孩子的默认头像
    private static final Set<Integer> YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY = new HashSet<>();

    static {
        YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.add(1);
        YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.add(2);
        YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.add(3);
        YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.add(4);
        YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.add(7);
        YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.add(8);
    }

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = StudyTogetherLoader.class)
    private StudyTogetherLoader studyTogetherLoader;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private ParentRegisterHelper parentRegisterHelper;
    @Inject
    private ParentStudentCallNameHelper parentStudentCallNameHelper;
    @Inject
    private ValidateStudentIdHelper validateStudentIdHelper;

    @RequestMapping(value = "code.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCode() {
        String mobile = getRequestString("mobile");
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("手机号不能为空");
        }
        //检查次数
        boolean canSendMobileCode = studyTogetherLoader.canSendMobileCode(mobile);
        if (!canSendMobileCode) {
            return MapMessage.errorMessage("对不起，手机号验证码次数超过限制");
        }
        //发验证码
        MapMessage message = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name(), false);
        if (message.isSuccess()) {
            //累计次数
            studyTogetherServiceClient.incrMobileCodeCount(mobile);
        }
        return message;
    }

    @RequestMapping(value = "verify_code.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCode() {
        String mobile = getRequestString("mobile");
        String code = getRequestString("code");
        String lessonId = getRequestString("lesson_id");
        //跳转到课程开启页=true.跳转到邀请卡页面=false
        boolean jumpStart = getRequestBool("is_jump_start", true);
        //邀请人ID
        Long inviteUserId = getRequestLong("inviter_id", 0L);
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("手机号不能为空");
        }
        if (StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("验证码不能为空");
        }
        MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name());
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        // 已注册。直接登录
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (userAuthentication != null && StringUtils.isNotBlank(userAuthentication.getSensitiveMobile())) {
            User parent = raikouSystem.loadUser(userAuthentication.getId());
            //跟原生登录一样有一堆处理要做
            doExtThingForLogin(parent);
            //报名
            if (jumpStart) {
                return studyTogetherServiceClient.parentSignUpLesson(lessonId, parent.getId(), false, inviteUserId);
            }
            return MapMessage.successMessage();
        }
        String lock = "YIQIXUE_REGISTER_" + mobile;
        try {
            AtomicLockManager.getInstance().acquireLock(lock);
            //注册C端家长
            MapMessage message = parentRegisterHelper.registerChannelCParent(mobile, RoleType.ROLE_PARENT, "", UserWebSource.parent_17xue.getSource());
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("注册家长号失败");
            }
            userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
            User parent = raikouSystem.loadUser(userAuthentication.getId());
            //跟原生登录一样有一堆处理要做
            doExtThingForLogin(parent);
            //报名
            if (jumpStart) {
                MapMessage mapMessage1 = studyTogetherServiceClient.parentSignUpLesson(lessonId, parent.getId(), true, inviteUserId);
                if (!mapMessage1.isSuccess())
                    return mapMessage1.setErrorCode("666");
                else
                    return mapMessage1;
            }
            return MapMessage.successMessage();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理中。请稍后");
        } finally {
            AtomicLockManager.getInstance().releaseLock(lock);
        }
    }

    @RequestMapping(value = "sign_up_lesson.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage signUpLesson() {
        String lessonId = getRequestString("lesson_id");
        //邀请人ID
        Long inviteUserId = getRequestLong("inviter_id", 0L);
        boolean isNewParent = getRequestBool("is_new_parent", false);

        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        return studyTogetherServiceClient.parentSignUpLesson(lessonId, parent.getId(), isNewParent, inviteUserId);
    }

    @RequestMapping(value = "verify_course_code.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateMobile() {
        String courseCode = getRequestString("course_code");
        String lessonId = getRequestString("lesson_id");
        Long studentId = getRequestLong("sid");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (StringUtils.isBlank(lessonId) || StringUtils.isBlank(courseCode)) {
            return MapMessage.errorMessage("课程信息不能为空");
        }
        MapMessage message = studyTogetherServiceClient.validateJoinGroupCode(lessonId, parent.getId(), courseCode);
        if (!message.isSuccess()) {
            return message;
        }
        //家长有孩子。不需要添加。直接跳课程页
        if (studentId != 0 && studentIsParentChildren(parent.getId(), studentId)) {
            String s = studyTogetherServiceClient.studentJoinStudyGroupByCode(studentId, parent.getId(), lessonId, courseCode);
            if (StringUtils.isBlank(s))
                return MapMessage.errorMessage("验证码错误哦~");
            return MapMessage.successMessage().add("add_child", false).add("goto_course", true);
        }
        //家长手机号
        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
        if (StringUtils.isBlank(authenticatedMobile)) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }

        //手机是否绑定了学生
        UserAuthentication studentAuthentication = userLoaderClient.loadMobileAuthentication(authenticatedMobile, UserType.STUDENT);
        if (studentAuthentication == null) {
            return MapMessage.successMessage().add("show_exist_student", false).add("add_child", true).add("goto_course", false);
        }
        Long existStudentId = studentAuthentication.getId();

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(existStudentId);
        if (studentDetail == null || !studentDetail.isPrimaryStudent()) {
            return MapMessage.successMessage().add("show_exist_student", false).add("add_child", true).add("goto_course", false);
        }
        validateStudentIdHelper.storeBindStudentIdWithParentId(parent.getId(), existStudentId);
        return MapMessage.successMessage()
                .add("show_exist_student", true)
                .add("add_child", true)
                .add("goto_course", false)
                .add("student_id", existStudentId)
                .add("student_name", studentDetail.fetchRealname())
                .add("img_url", getUserAvatarImgUrl(studentDetail.fetchImageUrl()));

    }

    //新注册一个C端孩子并绑定
    @RequestMapping(value = "register_channel_c_student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage registerChannelCStudent() {
        Integer clazzLevel = getRequestInt("clazz_level", -1);
        String gender = getRequestString("gender");
        String studentName = getRequestString("student_name");
        String uuid = getRequestString("uuid");
        Integer callNameId = getRequestInt("call_name_id");
        Integer avatarKey = getRequestInt("avatar_key");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //验证身份
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        if (!YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.contains(avatarKey)) {
            return MapMessage.errorMessage("头像选择错误");
        }
        UserAvatar userAvatar = UserAvatar.parsePrimaryStudentKey(avatarKey);
        if (userAvatar == null) {
            return MapMessage.errorMessage("头像选择错误");
        }
        //家长手机号
        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
        if (StringUtils.isBlank(authenticatedMobile)) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (!MobileRule.isMobile(authenticatedMobile)) {
            return MapMessage.errorMessage();
        }
        // Enhancement #48844 学生注册姓名字数限制
        if (StringUtils.isBlank(studentName)) {
            return MapMessage.errorMessage("学生姓名不能为空");
        }
        if (studentName.length() > 10) {
            return MapMessage.errorMessage("学生姓名需在10位汉字以内");
        }

        ChannelCUserAttribute.ClazzCLevel clazzCLevel = ChannelCUserAttribute.ClazzCLevel.parse(clazzLevel);
        if (clazzCLevel == null) {
            return MapMessage.errorMessage("错误的学生年级!");
        }

        Gender genderEnum = Gender.fromCode(gender);
        NeonatalUser source = new NeonatalUser();
        source.setGender(genderEnum.getCode());
        source.setRealname(StringUtils.cleanXSS(studentName));
        source.setMobile(authenticatedMobile);
        source.setWebSource(UserWebSource.parent_17xue.getSource());
        source.setUserType(UserType.STUDENT);
        source.setPassword(RandomGenerator.generateUserPassword().getPassword());
        source.attachPasswordState(PasswordState.AUTO_GEN);
        source.getExtensionAttributes().put("imgUrl", userAvatar.getUrl());

        ChannelCUserAttribute channelCUserAttribute = new ChannelCUserAttribute(null, UserType.STUDENT, null, "", clazzLevel, null);
        channelCUserAttribute.setUuid(uuid);
        try {
            MapMessage mapMessage = AtomicLockManager.getInstance().wrapAtomic(studentServiceClient)
                    .keyPrefix("registerChannelCStudent")
                    .keys(authenticatedMobile)
                    .proxy()
                    .registerChannelCStudent(source, channelCUserAttribute);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage("注册学生失败");
            }
            Long studentId = SafeConverter.toLong(mapMessage.get("student_id"));
            //绑定身份
            parentServiceClient.bindExistingParent(studentId, parent.getId(), true, callName.name());
            return MapMessage.successMessage().add("student_id", studentId);
        } catch (CannotAcquireLockException ignored) {
            return MapMessage.errorMessage("正在处理中，请稍后");
        }
    }

    @RequestMapping(value = "bind_identity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindIdentity() {
        Long studentId = getRequestLong("sid");
        Integer callNameId = getRequestInt("call_name_id");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //验证身份
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        //验证传入的sid是否是前面的后端接口返回的
        MapMessage mapMessage = validateStudentIdHelper.validateBindRequestStudentIdWithParentId(parent.getId(), studentId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        //验证选择的身份是否合法
        MapMessage message = parentStudentCallNameHelper.validateStudentParentRef(studentId, parent, callName);
        if (!message.isSuccess()) {
            return message;
        }
        boolean isKeyParent = SafeConverter.toBoolean(message.get("keyParent"));
        return parentServiceClient.bindExistingParent(studentId, parent.getId(), isKeyParent, callName.name());
    }

    //身份列表
    @RequestMapping(value = "call_name_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCallNameList() {
        List<Map<String, Object>> identityList = new ArrayList<>();
        for (CallName callName : CallName.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", callName.getKey());
            map.put("call_name", StringUtils.replace(callName.name(), "它", "他"));
            identityList.add(map);
        }
        return MapMessage.successMessage().add("identity_list", identityList);
    }

    //选完身份直接校验
    @RequestMapping(value = "validate_identity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateIdentity() {
        Integer callNameId = getRequestInt("call_name_id");
        Long studentId = getRequestLong("sid");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //验证身份
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        return parentStudentCallNameHelper.validateStudentParentRef(studentId, parent, callName);
    }

    @RequestMapping(value = "join_study_group.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinStudyGroup() {
        String lessonId = getRequestString("lesson_id");
        String code = getRequestString("course_code");
        Long studentId = getRequestLong("sid");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        String groupId = studyTogetherServiceClient.studentJoinStudyGroupByCode(studentId, parent.getId(), lessonId, code);
        return StringUtils.isBlank(groupId) ? MapMessage.errorMessage("开启课程失败") : MapMessage.successMessage();
    }

    @RequestMapping(value = "default_avatar.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDefaultAvatar() {
        List<UserAvatar> studentAvatars = UserAvatar.getPrimaryStudentAvatars();
        List<Map<String, Object>> mapList = new ArrayList<>();
        studentAvatars.stream().filter(p -> YIQIXUE_STUDENT_DEFAULT_AVATAR_KEY.contains(p.getKey()))
                .forEach(userAvatar -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("key", userAvatar.getKey());
                    map.put("url", getUserAvatarImgUrl(userAvatar.getUrl()));
                    mapList.add(map);
                });
        return MapMessage.successMessage().add("default_avatar_list", mapList);
    }

    @RequestMapping(value = "activate_lesson_by_url.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage activeLessonByUrl() {
        User currentParent = currentParent();
        if (currentParent == null) {
            return go2LoginPageResult;
        }
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("课程ID不能为空");
        }
        String vCode = getRequestString("vCode");
        long studentId = getRequestLong("student_id");
        ParentJoinLessonRef parentJoinLessonRef = studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, currentParent.getId());
        if (parentJoinLessonRef == null) {
            return MapMessage.successMessage().add("not_join", true);
        }
        if (studentId != 0L) {
            String groupId = studyTogetherServiceClient.studentJoinStudyGroupByCode(studentId, currentParent.getId(), lessonId, vCode);
            if (StringUtils.isBlank(groupId)) {
                return MapMessage.successMessage().add("active_fail", true);
            }
            StudyLesson studyLesson = getStudyLesson(lessonId);
            Date currentDate = new Date();
            return MapMessage.successMessage()
                    .add("lesson_title", studyLesson.getTitle())
                    .add("lesson_start_countdown", DateUtils.dayDiff(studyLesson.getOpenDate(), currentDate) + 1)
                    .add("is_start", studyLesson.getOpenDate().getTime() - currentDate.getTime() <= 0);
        } else {
            List<User> students = studentLoaderClient.loadParentStudents(currentParent.getId());
            if (students.size() > 1) {
                List<Map<String, Object>> returnList = new ArrayList<>();
                for (User student : students) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("student_id", student.getId());
                    map.put("name", student.fetchRealnameIfBlankId());
                    map.put("img_url", getUserAvatarImgUrl(student));
                    returnList.add(map);
                }
                return MapMessage.successMessage().add("student_list", returnList);
            } else if (students.size() == 1) {
                User student = students.get(0);
                String groupId = studyTogetherServiceClient.studentJoinStudyGroupByCode(student.getId(), currentParent.getId(), lessonId, vCode);
                if (StringUtils.isBlank(groupId)) {
                    return MapMessage.successMessage().add("active_fail", true);
                }
                StudyLesson studyLesson = getStudyLesson(lessonId);
                Date currentDate = new Date();
                return MapMessage.successMessage()
                        .add("lesson_title", studyLesson.getTitle())
                        .add("lesson_start_countdown", DateUtils.dayDiff(studyLesson.getOpenDate(), currentDate) + 1)
                        .add("is_start", studyLesson.getOpenDate().getTime() - currentDate.getTime() <= 0);
            } else {
                return MapMessage.successMessage().add("no_child", true);
            }
        }
    }
}
