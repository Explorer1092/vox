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

package com.voxlearning.wechat.controller.teacher;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.content.consumer.ContentLoaderClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.ClazzService;
import com.voxlearning.utopia.service.user.api.TeacherAlterationService;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreateSourceType;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ClazzIntegralHistoryPagination;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.log.UserOperatorType;
import com.voxlearning.utopia.service.user.api.mappers.ClassTeacherAlterationInfoMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.wechat.controller.AbstractTeacherWebController;
import com.voxlearning.wechat.support.SessionUtils;
import lombok.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.CLAZZ_MANAGEMENT_PREFIX;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2016/8/3
 * 班级管理
 */
@Controller
@RequestMapping(value = "/teacher/clazzmanage")
public class TeacherClazzManagementController extends AbstractTeacherWebController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private ContentLoaderClient contentLoaderClient;
    @Inject private GroupServiceClient groupServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject private TeacherServiceClient teacherServiceClient;
    @Inject private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject private TeacherSystemClazzInfoServiceClient teacherSystemClazzInfoServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;

    @Inject private RaikouSDK raikouSDK;

    // 1
    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    public String clazzListPage() {
        return "/teacher/clazzmanage/list";
    }

    @RequestMapping(value = "/editclazz.vpage", method = RequestMethod.GET)
    public String editClazz(Model model) {
        //是否虚假老师
        long uid = getRequestContext().getUserId();
        long clazzId = getRequestLong("clazzId");
        String clazzName = getRequestString("clazzName");
        boolean isFakeTeacher = teacherLoaderClient.isFakeTeacher(uid);

        // 未认证状态，不能显示排行榜
        // 假老师的话，也不显示排行榜
        Teacher teacher = teacherLoaderClient.loadTeacher(uid);
        if (teacher != null && teacher.isPrimarySchool()) {
            model.addAttribute("displayShowRank",
                    AuthenticationState.safeParse(teacher.getAuthenticationState())
                            == AuthenticationState.SUCCESS &&
                            !isFakeTeacher);
        } else
            model.addAttribute("displayShowRank", false);

        // 排行榜显示选项，默认是显示
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        model.addAttribute("showRank", clazz == null ? true : clazz.needShowRank());

        model.addAttribute("isFakeTeacher", isFakeTeacher);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("clazzName", clazzName);
        return "/teacher/clazzmanage/editclazz";
    }

    /**
     * 获取班级学生
     */
    @RequestMapping(value = "/getStudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getStudentsByClazzId() {
        int clazzId = getRequestInt("clazzId");
        //todo

        return null;
    }

    // 1

    /**
     * 添加老师页面
     */
    @RequestMapping(value = "/addteacher.vpage", method = RequestMethod.GET)
    public String addTeacher() {
        return "/teacher/clazzmanage/addteacher";
    }

    // 1

    /**
     * 搜索相应老师列表页面
     */
    @RequestMapping(value = "/teacherlist.vpage", method = RequestMethod.GET)
    public String teachersList() {
        return "/teacher/clazzmanage/teacherlist";

    }

    /**
     * 获取班级的三科任课老师
     */
    @RequestMapping(value = "/getclazzotherteachers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getClazzOtherTeachers() {
        long clazzId = getRequestLong("cid");
        long userId = getRequestContext().getUserId();
        try {
            // 主副账号老师
            Teacher teacher = teacherLoaderClient.loadTeacher(userId);

            // 获取各添加学科状态
            List<TeacherSystemClazzService.CanAddSubjectStatus> canAddSubjects = teacherSystemClazzServiceClient.findCanAddSubject(userId, clazzId);

            // 获取主账号
            List<Long> subTeacherIds = canAddSubjects.stream().filter(e -> e.getTeacherId() != null).map(e -> e.getTeacherId()).collect(Collectors.toList());
            Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(subTeacherIds);

            Map<String, Object> map = new HashMap<>();
            List<String> linkApplicationSentSubjects = new ArrayList<>();
            canAddSubjects.forEach(s -> {
                if (s.getStatus() == 0 || s.getStatus() == 2) {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("id", mainTeacherIds.get(s.getTeacherId()) != null ? mainTeacherIds.get(s.getTeacherId()) : s.getTeacherId());
                    teacherMap.put("name", s.getTeacherName());
                    teacherMap.put("authenticationState", s.getAuthState());
                    if (s.getStatus() == 2) {
                        linkApplicationSentSubjects.add(s.getSubject().name());
                    }
                    map.put(s.getSubject().name(), teacherMap);
                } else {
                    map.put(s.getSubject().name(), null);
                }
            });
            return MapMessage.successMessage().add("teachers", map).add("linkApplicationSentSubjects", linkApplicationSentSubjects);
        } catch (Exception ex) {
            logger.error("wechat clazz mgn load clazz teachers failed," + ex.getMessage());
            return MapMessage.errorMessage("获取班级老师失败");
        }
    }

    /**
     * 添加老师--保存
     */
    @RequestMapping(value = "/addteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTeacherSave() {
        //todo

        return MapMessage.successMessage();

    }


    /**
     * 操作学生
     */
    @RequestMapping(value = "/editstudent.vpage", method = RequestMethod.GET)
    public String editStudent() {
        return "/teacher/clazzmanage/editstudent";

    }


    /**
     * 重置学生密码
     */
    @RequestMapping(value = "/password.vpage", method = RequestMethod.GET)
    public String restPassword() {


        return "/teacher/clazzmanage/password";

    }

    /**
     * 重置学生密码 -- 保存
     */
    @RequestMapping(value = "/password.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage restPasswordSave() {


        return null;
    }

    // 1

    /**
     * 转让班级
     */
    @RequestMapping(value = "/transferclazz.vpage", method = RequestMethod.GET)
    public String transferClazz(Model model) {
        long userId = getRequestContext().getUserId();
        long clazzId = getRequestLong("clazzId");

        List<Subject> subjects = teacherLoaderClient.findTeacherAllTeachingSubjectsInClazz(userId, clazzId);
        List<Map<String, Object>> subjectsMapList = subjects.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.name());
            map.put("value", s.getValue());
            return map;
        }).collect(Collectors.toList());

        model.addAttribute("subjects", JsonUtils.toJson(subjectsMapList));
        return "/teacher/clazzmanage/transferclazz";
    }


    /**
     * 转让班级-保存
     */
    @RequestMapping(value = "/transferclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage transferClazzSave() {


        return null;
    }

    // 1

    /**
     * 创建班级 - 班级列表页面
     */
    @RequestMapping(value = "/createclazz.vpage", method = RequestMethod.GET)
    public String createClazz(Model model) {
        // 五四制支持
        long userId = getRequestContext().getUserId();
        UserSchoolRef userSchoolRef = schoolLoaderClient.getSchoolLoader()
                .findUserSchoolRefsByUserId(userId)
                .getUninterruptibly()
                .stream()
                .findFirst()
                .orElse(null);
        if (userSchoolRef != null) {
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(userSchoolRef.getSchoolId()).getUninterruptibly();
            String eduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
            if (StringUtils.isNoneBlank(eduSystem)) {
                model.addAttribute("eduSystem", EduSystemType.of(eduSystem));
            }
        }

        return "/teacher/clazzmanage/createclazz";
    }

    // 1

    /**
     * 老师的班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzList() {
        long userId = getRequestContext().getUserId();
        try {
            // 老师的所有分组信息
            Set<Long> handledClazzIds = new HashSet<>();
            List<Map<String, Object>> groupClazzMappers = teacherSystemClazzServiceClient.loadTeacherAllGroupsData(userId).stream()
                    .filter(g -> {
                        boolean notContain = !handledClazzIds.contains(g.getClazzId());
                        if (notContain) handledClazzIds.add(g.getClazzId());
                        return notContain;
                    })
                    .map(g -> MiscUtils.m("clazzId", g.getClazzId(),
                            "clazzName", g.getClazzName(),
                            "subjectText", g.getMultiSubjects() == null ? "" : g.getMultiSubjects().stream().map(Subject::getValue).collect(Collectors.joining(" ")),
                            "studentCount", g.getStudentCount()))
                    .collect(Collectors.toList());

            MapMessage message = MapMessage.successMessage();
            message.add("teacherClazzList", groupClazzMappers);
            message.add("isFakeTeacher", teacherLoaderClient.isFakeTeacher(userId));
            return message;
        } catch (Exception ex) {
            logger.error("load teacher clazz list failed.", ex);
            return MapMessage.errorMessage("查询老师的班级列表失败");
        }
    }

    //获取未处理请求数量
    @RequestMapping(value = "pendingapplicationcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage pendingApplicationCount() {
        long userId = getRequestContext().getUserId();
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            int count = teacherAlterationServiceClient.countPendingApplicationSendIn(teacher.getId());
            return MapMessage.successMessage().add("count", count);
        } catch (Exception ex) {
            logger.error("load teacher applicationcount failed.", ex);
            return MapMessage.errorMessage().setInfo("获取未处理请求数量失败");
        }
    }

    // 1

    /**
     * 获取管理学生 学生列表，班级详情
     */
    @RequestMapping(value = "clazzdetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzDetail(HttpServletRequest request) {
        long userId = getRequestContext().getUserId();
        long clazzId = getRequestLong("cid", Long.MIN_VALUE);
        if (clazzId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid params");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            Map<String, Object> detail = teacherSystemClazzServiceClient.loadTeacherClazzDetail(teacher.getId(), clazzId);
            if (MapUtils.isEmpty(detail)) {
                return MapMessage.errorMessage("没有数据");
            }
            return MapMessage.successMessage().add("students", detail.get("students"));
        } catch (Exception ex) {
            logger.error("load teacher clazz student list failed.", ex);
            return MapMessage.errorMessage("查询老师班级学生列表失败");
        }
    }

    // student detail
    @RequestMapping(value = "studentdetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentDetail() {
        long userId = getRequestContext().getUserId();
        Long studentId = getRequestLong("studentId", Long.MIN_VALUE);
        List<User> teacherStudents = studentLoaderClient.loadTeacherStudents(userId);
        // 判断该学生是否是该老师的学生
        List<User> theStudents = teacherStudents.stream().filter(p -> p.getId().equals(studentId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(theStudents)) {
            return MapMessage.errorMessage("illegal");
        }
        if (studentId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid params");
        }
        try {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            Map<String, Object> detail = teacherSystemClazzServiceClient.loadTeacherClazzDetail(userId, studentDetail.getClazzId());
            if (MapUtils.isEmpty(detail)) {
                return MapMessage.errorMessage("没有数据");
            }
            // filter specific user
            List<Map<String, Object>> students = (List<Map<String, Object>>) detail.get("students");
            Map<String, Object> student = students.stream().filter(p -> p.get("studentId").equals(studentId)).findFirst().orElse(new HashMap<>());
            if (MapUtils.isEmpty(student)) {
                return MapMessage.errorMessage("没有数据");
            }
            student.put("clazzId", studentDetail.getClazz().getId());
            return MapMessage.successMessage().add("student", student);
        } catch (Exception ex) {
            logger.error("load student detail failed.", ex);
            return MapMessage.errorMessage("查询学生详情失败");
        }
    }


    //管理学生 修改密码
    @RequestMapping(value = "resetstudentpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetStudentPassword(HttpServletRequest request) {

        long userId = getRequestContext().getUserId();
        long studentId = getRequestLong("sid", Long.MIN_VALUE);
        long clazzId = getRequestLong("cid", Long.MIN_VALUE);
        String password = getRequestString("p");
        String confirmPassword = getRequestString("cp");
        if (studentId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }

        // 如果学生未绑定手机，老师可以直接重置密码
        // 如果学生绑定了家长手机，则一天只能重置一次密码，且通过给手机发送随机密码的方式重置
        // 统一用一个接口并增加检查的原因是防止用户通过接口直接重置
        String studentOrParentMobile = studentLoaderClient.loadStudentOrParentMobile(studentId, SafeConverter.toString(userId));
        if (StringUtils.isNotBlank(studentOrParentMobile)) {
            // 老师给学生重置密码行为，一天只能一次
            if (!asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherResetBindedStudentPWCacheManager_canResetPw(userId, studentId)
                    .getUninterruptibly()) {
                return MapMessage.errorMessage("您已经帮助重置过，如果学生没有收到密码可以联系客服");
            }

            // 生成随机密码
            confirmPassword = password = RandomGenerator.generatePlainPassword();
        }


        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            MapMessage message = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(clazzId)
                    .proxy()
                    .changeClazzStudentPassword(teacher, clazzId, studentId, password, confirmPassword);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage(message.getInfo());
            } else {

                // 重置密码后处理
                User student = userLoaderClient.loadUser(studentId);

                // 如果是绑定手机的学生，则发送重置密码短信
                if (StringUtils.isNotBlank(studentOrParentMobile)) {
                    String smsPayload = StringUtils.formatMessage(
                            "{}同学好，老师正在帮你重置密码，请用新密码：{}登录做作业（如孩子在学校使用，请尽快将新密码转发给老师）",
                            student.fetchRealname(),
                            password
                    );
                    smsServiceClient.createSmsMessage(studentOrParentMobile)
                            .content(smsPayload)
                            .type(SmsType.TEACHER_RESET_STUDENT_PASSWORD.name())
                            .send();
                }
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(teacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("修改密码");
                userServiceRecord.setComments("老师修改学生密码，操作端：wechat");
                userServiceRecord.setAdditions("refer:WechatTeacherClazzManagementController.resetStudentPassword");
                userServiceClient.saveUserServiceRecord(userServiceRecord);

                // 如果学生修改密码，更新学生端sessionkey
                VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", studentId);
                if (vendorAppsUserRef != null) {
                    vendorServiceClient.expireSessionKey(
                            "17Student",
                            studentId,
                            SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), studentId));
                }

                // 老师修改学生密码,需要强制学生修改密码
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .unflushable_setUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, studentId, 1L, 0)
                        .awaitUninterruptibly();
                return MapMessage.successMessage();
            }
        } catch (Exception ex) {
            logger.error("update student password failed.", ex);
            return MapMessage.successMessage("修改学生密码失败");
        }
    }

    //管理学生 删除学生
    @RequestMapping(value = "batchremovestudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteStudents() {
        long teacherId = getRequestContext().getUserId();
        long clazzId = getRequestLong("cid", Long.MIN_VALUE);
        String studentIds = getRequestString("sids");
        if (clazzId == Long.MIN_VALUE || StringUtils.isBlank(studentIds)) {
            return MapMessage.errorMessage("invalid data");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        try {
            List<Long> studentIdList = StringUtils.toLongList(studentIds);
            Clazz c = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            MapMessage message = atomicLockManager.wrapAtomic(teacherServiceClient)
                    .keyPrefix(CLAZZ_MANAGEMENT_PREFIX)
                    .keys(clazzId)
                    .proxy()
                    .deleteClazzStudents(teacher, c, studentIdList, null);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage(message.getInfo());
            } else {
                Clazz clazz = (Clazz) message.get("clazz");
                String m = "{}老师将你移出了{}班，他/她可能觉得你不是这个班级的学生或者你已经有学号，如有疑问请直接联系老师！";
                m = StringUtils.formatMessage(m, teacher.fetchRealname(), clazz.formalizeClazzName());
                Collection deletedStudentIds = (Collection) message.get("deletedStudentIds");
                for (Object deletedStudentId : deletedStudentIds) {
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage((Long) deletedStudentId, m);
                }
                return MapMessage.successMessage();
            }
        } catch (Exception ex) {
            logger.error("batch delete student failed.", ex);
            return MapMessage.errorMessage("批量删除学生失败");
        }
    }


    //加入班级 获取我的班级列表
    @RequestMapping(value = "loadteacherclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadTeacherClazz() {
        long userId = getRequestContext().getUserId();
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
            List<Clazz> teachClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
            Long schoolId = teacher.getTeacherSchoolId();
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            if (school == null) {
                return MapMessage.errorMessage("No school for this teacher found.");
            }
            EduSystemType eduSystem = EduSystemType.of(
                    schoolExtServiceClient.getSchoolExtService()
                            .getSchoolEduSystem(school)
                            .getUninterruptibly()
            );
            teachClazzs = teachClazzs.stream()
                    .filter(c -> c.matchEduSystem(eduSystem))
                    .collect(Collectors.toList());
            //我教的班级
            List<Map<String, Object>> teacheClazzList = new LinkedList<>();
            Map<String, List<Clazz>> levelTeachClazzs = teachClazzs.stream()
                    .collect(Collectors.groupingBy(Clazz::getClassLevel));

            levelTeachClazzs.forEach((k, v) -> {
                List<SystemClazzInfo> list = v.stream()
                        .sorted(new ClazzComparator())
                        .map(c -> new SystemClazzInfo(c.getId(), c.getClassName()))
                        .collect(Collectors.toList());
                Map<String, Object> levelObj = new HashMap<>();
                levelObj.put("clazzLevel", k);
                levelObj.put("clazzs", list);
                teacheClazzList.add(levelObj);
            });

            // ugc info
            // 执教班级数
//            TeacherUGCInfo teacherUGCInfo = teacherUgcServiceClient.getTeacherUgcService()
//                    .loadTeacherUGCInfo(teacher.getId())
//                    .getUninterruptibly();
//            int actualTeachClazzCount = teacherUGCInfo == null ? 0 : teacherUGCInfo.getTeachClazzNum();
            return MapMessage.successMessage().add("teachClazzs", teacheClazzList);

        } catch (Exception ex) {
            logger.error("load teacher clazz list failed.", ex);
            return MapMessage.errorMessage("获取我的班级列表失败");
        }
    }

    // 1

    /**
     * 创建班级，根据系统班级列表以及老师执教班级列表
     */
    @RequestMapping(value = "chooseclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chooseClazz() {
        Long userId = getTeacherIdBySubject();
        try {
            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(userId)
                    .getUninterruptibly();
            if (school == null) {
                return MapMessage.errorMessage("学校不存在");
            }

            // 老师执教的班级
            Set<Long> teacherClazzIds = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(userId).stream()
                    .map(Clazz::getId)
                    .collect(Collectors.toSet());

            // 学校班级
            int maxJie = ClassJieHelper.fromClazzLevel(ClazzLevel.FIRST_GRADE);
            int minJie = ClassJieHelper.fromClazzLevel(ClazzLevel.SIXTH_GRADE);
//            List<Clazz> clazzs = clazzLoaderClient.loadSchoolClazzs(school.getId())
//                    .clazzType(ClazzType.PUBLIC)
//                    .enabled()
//                    .filter(c -> c.getJie() >= minJie && c.getJie() <= maxJie)
//                    .toList();

            List<Clazz> clazzs = teacherSystemClazzServiceClient.loadSystemClazzsInfoByTeacherIdAndSchoolId(userId, school.getId(), minJie, maxJie);

            // 生成返回结果
            List<List<SystemClazzInfo>> levelClazzs = new ArrayList<>();
            clazzs.stream()
//                    .filter(c -> !c.isTerminalClazz())
//                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .collect(Collectors.groupingBy(Clazz::getClazzLevel, LinkedHashMap::new, Collectors.mapping(p -> p, Collectors.toList())))
                    .values()
                    .forEach(cs -> levelClazzs.add(cs.stream().map(c -> {
                        SystemClazzInfo systemClazzInfo = new SystemClazzInfo(c.getId(), c.getClassName());
                        systemClazzInfo.setChecked(teacherClazzIds.contains(c.getId()));
                        return systemClazzInfo;
                    }).collect(Collectors.toList())));
            return MapMessage.successMessage().add("levelClazzs", levelClazzs);
        } catch (Exception ex) {
            logger.error("load teacher join clazz list failed.", ex);
            return MapMessage.errorMessage("获取班级列表失败");
        }
    }

    // 1

    /**
     * 获取已存在老师班级信息
     * 用于创建班级时，查找活跃的班级老师
     *
     * @param clazzMap
     * @return
     */
    @RequestMapping(value = "findclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findClazzInfo(@RequestBody Map<String, Object> clazzMap) {
        long userId = getRequestContext().getUserId();
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);

        if (teacher == null) {
            return MapMessage.errorMessage("invalid data");
        }
        try {
            Set<Long> clazzIdSet = new LinkedHashSet<>();
            clazzMap.forEach((k, v) -> {
                List<Object> levelClazzs = (List<Object>) v;
                levelClazzs.forEach(lc -> {
                    Map<String, Object> m = (Map<String, Object>) lc;
                    List<SystemClazzInfo> clazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(m.get("clazzs")), SystemClazzInfo.class);
                    clazzs.forEach(c -> clazzIdSet.add(c.getId()));
                });
            });

            // TODO 封装返回
            MapMessage message = teacherSystemClazzInfoServiceClient.getNewAddAndAdjustClazzs(teacher.getId(), clazzIdSet, ClazzCreateSourceType.wechat, false);
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("newClazzs", message.get("newClazzs"));
            mapMessage.add("adjustClazzs", message.get("adjustClazzs"));
            return mapMessage;
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher find clazz info failed.", ex);
            return MapMessage.errorMessage("获取已存在老师班级信息");
        }
    }


    // 如果有需要申请的班级 走这个借口
    @RequestMapping(value = "adjustclazzs2.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage adjustClazzs2() throws IOException {
        Long userId = getRequestContext().getUserId();
        String subject = getRequestString("subject");
        Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(userId, Subject.of(subject));
        if (relTeacherId == null) {
            logger.error("get rel teacher id failed," + userId + ":" + subject);
            // 没有命中就用原来的吧
            relTeacherId = userId;
        }
        Teacher curTeacher = teacherLoaderClient.loadTeacher(relTeacherId);
        try {
            ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
            JsonNode rootNode = mapper.readTree(getRequestString("datajson"));
            CollectionType LongListType = mapper.getTypeFactory().constructCollectionType(List.class, Long.class);

            List<Long> adjustClazzIds = new LinkedList<>();

            // 处理加入班级已有资源请求
            JsonNode node = rootNode.get("newClazzs");
            if (node != null) {
                Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(curTeacher.getId());

                for (JsonNode childNode : node) {
                    Long clazzId = childNode.get("clazzId").asLong();
                    adjustClazzIds.add(clazzId);// 对于发送请求的加入班级，老师先进班，请求是另外一回事
                    List<Long> teacherIds = new LinkedList<>();
                    for (JsonNode groupNode : childNode.get("groups")) {
                        if (groupNode.get("teachers") != null) {
                            for (JsonNode teacherNode : groupNode.get("teachers")) {
                                teacherIds.add(teacherNode.get("id").asLong());
                            }
                        }
                    }

                    // fix redmine 29376
                    // 对于包班制老师，当进班的时候是直接替换原有同学科老师，不需要处理请求了
                    if (CollectionUtils.isNotEmpty(teacherIds) && !CollectionUtils.containsAny(relTeacherIds, teacherIds)) {
                        MapMessage msg = handleTeacherRequestStudentResource(curTeacher, clazzId, teacherIds);
                        if (!msg.isSuccess()) {
                            return msg;
                        }
                    }
                }
            }

            node = rootNode.get("adjustClazzs");
            adjustClazzIds.addAll(mapper.readValue(node.traverse(), LongListType));

            node = rootNode.get("adjustWalkingClazzs");
            Map<String, List<Map<String, Object>>> map = mapper.convertValue(node, Map.class);

//            // 老师UGC信息 - 执教班级数
//            JsonNode actualTeachClazzCountNode = rootNode.get("actualTeachClazzCount");
//            if (actualTeachClazzCountNode != null) {
//                int actualTeachClazzCount = actualTeachClazzCountNode.asInt();
//                teacherServiceClient.setTeacherUGCTeachClazzCount(curTeacher.getId(), actualTeachClazzCount);
//            }

            // 处理直接加入班级请求
            MapMessage message = MapMessage.successMessage();
            if (CollectionUtils.isNotEmpty(adjustClazzIds) || MapUtils.isNotEmpty(map)) {// 当教学班有班级时，允许退出所有行政班
                message = clazzServiceClient.teacherAdjustSystemClazzs(curTeacher.getId(), adjustClazzIds, OperationSourceType.wechat);
            }

            // 处理直接加入教学班级
            if (curTeacher.isJuniorTeacher()) {
                if (message.isSuccess() && (MapUtils.isNotEmpty(map) || CollectionUtils.isNotEmpty(adjustClazzIds))) {// 当行政班不为空时，允许退出所有教学班级
                    message = clazzServiceClient.teacherAdjustWalkingCLazz(curTeacher.getId(), map);
                }
            }

            //记录点击调整按钮
            asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .TeacherAdjustClazzRemindCacheManager_record(curTeacher.getId())
                    .awaitUninterruptibly();
            return message;
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher adjust clazz 2 failed.", ex);
            return MapMessage.errorMessage("加入班级失败");
        }
    }

    /**
     * 教师查看未处理的申请记录
     */
    @RequestMapping(value = "unprocessedapplication.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage pengdingList() {
        long userId = getRequestContext().getUserId();
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);

        OperationSourceType sourceType = OperationSourceType.wechat;
        try {
            MapMessage mapMessage = MapMessage.successMessage();

            //ClassTeacherAlterationInfoMapper中存有Teacher的详情，这里的返回并不需要，所以去掉
            List<ClassTeacherAlterationInfoMapper> unprocessedApplicationSendIn = teacherAlterationServiceClient.findUnprocessedApplicationSendIn(teacher.getId(), sourceType);
            List<ClassTeacherAlterationInfoMapper> unprocessedApplicationSendOut = teacherAlterationServiceClient.findUnprocessedApplicationSendOut(teacher.getId(), sourceType);
            List<Map<String, Object>> unprocessedApplicationSendInMaps = new ArrayList<>();
            List<Map<String, Object>> unprocessedApplicationSendOutMaps = new ArrayList<>();
            for (ClassTeacherAlterationInfoMapper classTeacherAlterationInfoMapper : unprocessedApplicationSendIn) {
                Map<String, Object> map = new HashMap();
                map.put("recordId", classTeacherAlterationInfoMapper.getRecordId());
                map.put("classId", classTeacherAlterationInfoMapper.getClassId());
                map.put("className", classTeacherAlterationInfoMapper.getClassName());
                map.put("studentCount", classTeacherAlterationInfoMapper.getStudentCount());
                map.put("datetime", classTeacherAlterationInfoMapper.getDatetime());
                map.put("message", classTeacherAlterationInfoMapper.getMessage());
                map.put("type", classTeacherAlterationInfoMapper.getType());
                unprocessedApplicationSendInMaps.add(map);
            }
            for (ClassTeacherAlterationInfoMapper classTeacherAlterationInfoMapper : unprocessedApplicationSendOut) {
                Map<String, Object> map = new HashMap();
                map.put("recordId", classTeacherAlterationInfoMapper.getRecordId());
                map.put("classId", classTeacherAlterationInfoMapper.getClassId());
                map.put("className", classTeacherAlterationInfoMapper.getClassName());
                map.put("studentCount", classTeacherAlterationInfoMapper.getStudentCount());
                map.put("datetime", classTeacherAlterationInfoMapper.getDatetime());
                map.put("message", classTeacherAlterationInfoMapper.getMessage());
                map.put("type", classTeacherAlterationInfoMapper.getType());
                unprocessedApplicationSendOutMaps.add(map);
            }
            mapMessage.add("unprocessedApplicationSendIn", unprocessedApplicationSendInMaps);
            mapMessage.add("unprocessedApplicationSendOut", unprocessedApplicationSendOutMaps);
            return mapMessage;
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher find unprocessedapplication failed.", ex);
            return MapMessage.errorMessage("查询换班请求失败");
        }
    }


    /**
     * 老师取消关联学生申请
     */
    @RequestMapping(value = "cancellinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelLinkApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = cancelApplication(userId, recordId, ClazzTeacherAlterationType.LINK, sourceType);
            if (message.isSuccess()) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher cancellinkapp failed.", ex);
            return MapMessage.errorMessage("取消关联学生申请失败");
        }
    }


    /**
     * 老师拒绝其他老师关联学生申请
     */
    @RequestMapping(value = "rejectlinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectLinkApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = rejectApplication(userId, recordId, ClazzTeacherAlterationType.LINK, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, false);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher rejectlinkapp failed.", ex);
            return MapMessage.errorMessage("拒绝关联学生申请失败");
        }
    }


    /**
     * 老师同意其他老师的关联学生申请
     */
    @RequestMapping(value = "approvelinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveLinkApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }

        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = approveApplication(userId, recordId, ClazzTeacherAlterationType.LINK, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, true);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher approvelinkapp failed.", ex);
            return MapMessage.errorMessage("同意关联学生申请失败");
        }
    }

    /**
     * 老师取消接管学生资源申请
     */
    @RequestMapping(value = "cancelreplaceapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelReplaceApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = cancelApplication(userId, recordId, ClazzTeacherAlterationType.REPLACE, sourceType);
            if (message.isSuccess()) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher cancelreplaceapp failed.", ex);
            return MapMessage.errorMessage("取消接管学生资源申请失败");
        }
    }

    /**
     * 老师拒绝其他老师的接管学生申请
     */
    @RequestMapping(value = "rejectreplaceapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectReplaceApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = rejectApplication(userId, recordId, ClazzTeacherAlterationType.REPLACE, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, false);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher rejectreplaceapp failed.", ex);
            return MapMessage.errorMessage("拒绝接管学生资源申请失败");
        }
    }

    /**
     * 老师同意其他老师的接管学生申请
     */
    @RequestMapping(value = "approvereplaceapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveReplaceApplication() {

        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = approveApplication(userId, recordId, ClazzTeacherAlterationType.REPLACE, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, true);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher approvereplaceapp failed.", ex);
            return MapMessage.errorMessage("同意接管学生资源申请失败");
        }
    }

    /**
     * 取消转让给其他老师申请
     */
    @RequestMapping(value = "canceltransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cancelTransferApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = cancelApplication(userId, recordId, ClazzTeacherAlterationType.TRANSFER, sourceType);
            if (message.isSuccess()) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher canceltransferapp failed.", ex);
            return MapMessage.errorMessage("取消转让给其他老师申请失败");
        }
    }

    /**
     * 拒绝其他老师的转让班级申请
     */
    @RequestMapping(value = "rejecttransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectTransferApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = rejectApplication(userId, recordId, ClazzTeacherAlterationType.TRANSFER, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, false);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher rejecttransferapp failed.", ex);
            return MapMessage.errorMessage("拒绝转让给其他老师申请失败");
        }
    }

    /**
     * 同意其他老师的转让班级申请
     */
    @RequestMapping(value = "approvetransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveTransferApplication() {
        long userId = getRequestContext().getUserId();
        long recordId = getRequestLong("recordId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        OperationSourceType sourceType = OperationSourceType.wechat;

        try {
            MapMessage message = approveApplication(userId, recordId, ClazzTeacherAlterationType.TRANSFER, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, true);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage(message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("wechat clazz mgn teacher approvetransferapp failed.", ex);
            return MapMessage.errorMessage("同意转让给其他老师申请失败");
        }
    }

    // 1

    /**
     * 转让班级申请
     */
    @RequestMapping(value = "sendtransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTransferApplication() {
        // 获取其对应的学科的老师id
        long userId = getTeacherIdBySubject();
        long clazzId = getRequestLong("cid", Long.MIN_VALUE);
        long respondentId = getRequestLong("respondentId", Long.MIN_VALUE);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || respondentId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            return MapMessage.errorMessage("invalid data");
        }
        return teacherAlterationServiceClient.sendTransferApplication(userId, respondentId, clazzId, OperationSourceType.wechat);
    }

    // 1

    /**
     * 转让/添加班级-老师列表
     */
    @RequestMapping(value = "findlinkteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findLinkTeacher() {
        // 老师id
        long clazzId = getRequestLong("cid", Long.MIN_VALUE);
        // 学科
        String subject = getRequestString("subject");

        Long teacherId = getTeacherIdBySubject();
        if (teacherId == null || clazzId == Long.MIN_VALUE || StringUtils.isBlank(subject)) {
            return MapMessage.errorMessage("invalid data");
        }
        try {
            List<Teacher> teachers = teacherAlterationServiceClient.getTeacherOfSpecificSubjectInTheSameSchool(teacherId, Subject.of(subject), true);
            List<Map<String, Object>> teacherMaps = new ArrayList<>();
            for (Teacher t : teachers) {
                Map<String, Object> map = new HashMap<>();
                map.put("mainTeacherId", t.getId());
                map.put("teacherId", t.getId());
                map.put("teacherName", t.getProfile().getRealname());
                map.put("authenticationState", t.getAuthenticationState());
                teacherMaps.add(map);
            }
            teacherMaps = teacherMaps.stream().sorted((o1, o2) -> {
                int a1 = SafeConverter.toInt(o1.getOrDefault("authenticationState", 0));
                int a2 = SafeConverter.toInt(o2.getOrDefault("authenticationState", 0));
                return a2 - a1;
            }).collect(Collectors.toList());
            return MapMessage.successMessage().add("teachers", teacherMaps);
        } catch (Exception ex) {
            logger.error("wechat clazz mgn find link teacher failed.", ex);
            return MapMessage.errorMessage("查询老师失败");
        }
    }

    // 1

    /**
     * 添加老师申请
     */
    @RequestMapping(value = "sendlinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendLinkApplication() {
        // reminds:现在的user可能不具有这个班级的权限，如果没有，则使用起关联账号当applicant
        long userId = getRequestContext().getUserId();
        long clazzId = getRequestLong("cid", Long.MIN_VALUE);

        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(userId);

        // 选择老师在该班执教的账号发送请求
        Long clazzOnwerId = null;
        for (long t : teacherIds) {
            if (teacherLoaderClient.isTeachingClazz(t, clazzId)) {
                // 找到一个是教该班级的关联ids中的一个！
                clazzOnwerId = t;
                break;
            }
        }
        if (clazzOnwerId == null) {
            return MapMessage.errorMessage("老师不在该班级执教");
        }

        // 添加老师主账号id
        long respondentId = getRequestLong("respondentId", Long.MIN_VALUE);

        // 添加老师学科
        String subject = getRequestString("subject");

        if (respondentId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE || StringUtils.equals("", subject)) {
            return MapMessage.errorMessage("invalid parameters");
        }

        // 发送添加老师请求
        MapMessage message = teacherAlterationServiceClient.sendLinkApplication(clazzOnwerId, respondentId, Subject.of(subject), clazzId, OperationSourceType.wechat);

        // 发送消息通知及弹窗
        if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("{}老师申请一起教{}",
                            applicant.getProfile().getRealname(),
                            clazz.formalizeClazzName());
                }
            };
            sendAppMessageAndJpushForApplication(message, messageBuilder);
            sendApplicationMessageToRespondent(message, messageBuilder, true, true);
        }

        return message;
    }

    // 班级积分历史
    @RequestMapping(value = "integralhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage integralHistory() {
        long teacherId = getRequestContext().getUserId();
        long groupId = getRequestLong("gid", Long.MIN_VALUE);
        int pageNumber = getRequestInt("pn", 1);
        boolean ge0 = getRequestBool("ge0", true);

        User teacher = userLoaderClient.loadUser(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("invalid parameters");
        }

        if (pageNumber < 1) {
            pageNumber = 1;
        }

        // 获取前三个月的历史数据
        ClazzIntegralHistoryPagination pagination = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralHistories(groupId, 3, pageNumber - 1, 5, ge0)
                .getUninterruptibly();
        MapMessage mapMessage = MapMessage.successMessage();

        mapMessage.add("pagination", pagination);
        mapMessage.add("currentPage", pageNumber);
        mapMessage.add("integral", pagination.getTotalIntegral());
        return mapMessage;
    }

    // 加入或推出班级页面
    @RequestMapping(value = "add.vpage", method = RequestMethod.GET)
    public String add() {
        return "teacher/clazzmanage/add";
    }


    /**
     * 申请记录页面
     */
    @RequestMapping(value = "unprocessedapplication.vpage", method = RequestMethod.GET)
    public String unprocessedApplication() {
        return "teacher/homework/unprocessedapplication";
    }

    // 管理学生页面
    @RequestMapping(value = "studentmanage.vpage", method = RequestMethod.GET)
    public String studentManage() {
        return "teacher/homework/studentmanage";
    }

    // 修改密码
    @RequestMapping(value = "rsp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetStudentPassword() {
        return MapMessage.errorMessage();
    }

    // 批量删除学生
    @RequestMapping(value = "brs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchRemoveStudents() {
        return MapMessage.errorMessage();
    }

    // 转让班级搜索老师
    @RequestMapping(value = "findteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findTeacher() {
        return MapMessage.errorMessage();
    }


    // 班级积分页面
    @RequestMapping(value = "clazzintegral.vpage", method = RequestMethod.GET)
    public String clazzIntegral() {
        return "teacher/clazzmanage/clazzintegral";
    }

    // 获取班级学豆历史
    @RequestMapping(value = "loadintegralhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadIntegralHistory() {
        return MapMessage.errorMessage();
    }

    /*
            private methods
     */

    /**
     * 处理老师请求加入有学生资源的老师
     * 注意，如果老师需要加入班级，调用的是强行加入班级，从而可以跳过班级数量检查
     * 见redmine 28964
     * <p>
     * TODO 这个代码重复了3遍，分别在ucenter、washington、wechat三个项目中
     * TODO 原因是发消息部分，JPUSH依赖vendor，微信消息依赖微信，实际上这些基础的服务，应该在更底层，上层只是负责解析出发送所需的对象
     * TODO 找时间必须重构了
     *
     * @param curTeacher
     * @param clazzId
     * @param teacherIds
     * @return
     */
    @SuppressWarnings("unchecked")
    private MapMessage handleTeacherRequestStudentResource(Teacher curTeacher, Long clazzId, List<Long> teacherIds) {
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        Set<Subject> subjectSet = teachers.values().stream().map(Teacher::getSubject).collect(Collectors.toSet());

        // 如果teacherIds中包含curTeacher.getId()，表示老师想重新加回到组中(退班不退组)，把该班级id直接返回回去，让后面的逻辑处理
        if (teacherIds.contains(curTeacher.getId())) return MapMessage.successMessage();

        // 当无同科老师时，认证老师可以直接关联
        if (curTeacher.getAuthenticationState().equals(AuthenticationState.SUCCESS.getState())) {
            Teacher respondent = teachers.values().stream().filter(t -> t.getSubject() != curTeacher.getSubject()).findFirst().orElse(null);
            if (respondent != null) {
                // 处理老师当前的关联状况
                MapMessage result = clazzServiceClient.handleTeacherLinkOperation(curTeacher, respondent, clazzId, OperationSourceType.wechat);
                if (!result.isSuccess()) {
                    return result;
                }

                Map<Teacher, Teacher> replaceTeachers = (Map<Teacher, Teacher>) result.remove("replaceTeachers");
                Map<Teacher, Teacher> linkTeachers = (Map<Teacher, Teacher>) result.remove("linkTeachers");

                if (MapUtils.isNotEmpty(replaceTeachers) || MapUtils.isNotEmpty(linkTeachers)) {
                    boolean needSendApp = SafeConverter.toBoolean(result.remove("needSendApp"));

                    if (!needSendApp) {// 不需要发送请求的状况，直接接管
                        List<Long> addClazzTeacherIds = (List<Long>) result.remove("addClazzTeacherIds");
                        for (Long teacherId : addClazzTeacherIds) {
                            MapMessage message = clazzServiceClient.teacherJoinSystemClazzForce(teacherId, clazzId);// 强制加入
                            if (!message.isSuccess()) {
                                return message;
                            }
                        }
                        MapMessage m = MapMessage.successMessage();
                        for (Map.Entry<Teacher, Teacher> entry : replaceTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.replaceTeacherGroupForReplace(fromT.getId(), toT.getId(), clazzId, toT.getId().toString(), UserOperatorType.TEACHER);
                        }
                        for (Map.Entry<Teacher, Teacher> entry : linkTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.shareTeacherGroup(fromT.getId(),
                                    toT.getId(), fromT.getSubject(), toT.getSubject(), clazzId, curTeacher.getId().toString(), UserOperatorType.TEACHER);
                        }
                        if (m.isSuccess()) {// 已加入班级，不需要发送请求
                            return MapMessage.successMessage().add("join", true);
                        }
                    }
                }
            }

        }

        for (Teacher teacher : teachers.values()) {
            if (teacher.getSubject().equals(curTeacher.getSubject())) {// 同学科，进行接管申请
                MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(), ClazzTeacherAlterationType.REPLACE,
                        "向班级任课教师申请接管学生资源失败");
                // 发送消息通知及弹窗
                if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                    ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                        @Override
                        String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                            return StringUtils.formatMessage("{}老师申请接管您任课的{}的学生资源",
                                    applicant.getProfile().getRealname(),
                                    clazz.formalizeClazzName());
                        }
                    };
                    sendAppMessageAndJpushForApplication(message, messageBuilder);
                    sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                } else {
                    return message;
                }
            } else {// 不同学科，判断是否有同学科的老师，没有发送关联申请
                // 或者老师为认证老师，则发送关联请求，一旦关联，之前的关联老师被接管
                if (teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState()
                        || !subjectSet.contains(curTeacher.getSubject())) {
                    MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(),
                            ClazzTeacherAlterationType.LINK, "向班级任课教师申请关联学生资源失败");

                    // 发送消息通知及弹窗
                    if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                        ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                            @Override
                            String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                                return StringUtils.formatMessage("{}老师申请关联您任课的{}的学生资源",
                                        applicant.getProfile().getRealname(),
                                        clazz.formalizeClazzName());
                            }
                        };
                        sendAppMessageAndJpushForApplication(message, messageBuilder);
                        sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                    } else {
                        return message;
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 发班级申请时,给对方发送app消息,同时推送jpush.
     * 发link replace 以及transfer类申请.
     *
     * @param message
     * @param applicationMessageBuilder
     * @return
     */
    private void sendAppMessageAndJpushForApplication(MapMessage message, ApplicationMessageBuilder applicationMessageBuilder) {

        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");
        Long recordId = (Long) message.get("recordId");

        //TODO 新前端域名处理
        String messageContent = applicationMessageBuilder.buildMessage(applicant, respondent, clazz);

        String messageUrl = TeacherMessageType.getApplicationUrlTemplate() + recordId;
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(respondent.getId());
        appMessage.setLinkUrl(messageUrl);
        appMessage.setLinkType(1);
        appMessage.setMessageType(TeacherMessageType.APPLICATION.getType());
        appMessage.setContent(messageContent);
        appMessage.setTitle("班级请求");
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);

        List<Long> userIdList = new ArrayList<>();
        userIdList.add(respondent.getId());
        String key = "";
        if (Ktwelve.JUNIOR_SCHOOL.equals(respondent.getKtwelve()))
            key = "m";
        if (Ktwelve.PRIMARY_SCHOOL.equals(respondent.getKtwelve()))
            key = "j";
        Map<String, Object> extroInfo = MiscUtils.m("s", TeacherMessageType.APPLICATION.getType(), "key", key, "link", fetchMainsiteUrlByCurrentSchema() + messageUrl, "t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(messageContent, AppMessageSource.JUNIOR_TEACHER, userIdList, extroInfo);

    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class SystemClazzInfo {
        @Getter
        @Setter
        @NonNull
        Long id;
        @Getter
        @Setter
        @NonNull
        String name;
        @Getter
        @Setter
        Boolean checked;
    }

    private static class ClazzComparator implements Comparator<Clazz> {
        @Override
        public int compare(Clazz o1, Clazz o2) {
            String n1 = o1.getClassName();
            String n2 = o2.getClassName();

            int p1 = n1.lastIndexOf("班");
            int p2 = n2.lastIndexOf("班");

            if (p1 != -1 && p2 != -1) {
                n1 = n1.substring(0, p1);
                n2 = n2.substring(0, p2);
                try {
                    return Integer.valueOf(n1).compareTo(Integer.valueOf(n2));
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }

            return n1.compareTo(n2);
        }
    }

    /**
     * 取消申请
     *
     * @param applicantId 申请人ID
     * @param recordId    申请ID
     * @param type        申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage cancelApplication(long applicantId,
                                         long recordId,
                                         ClazzTeacherAlterationType type,
                                         OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.cancelApplication(applicantId, recordId, type, sourceType);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationServiceClient.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("操作失败");
        }
        return msg;
    }

    /**
     * 发送申请
     *
     * @param applicantId  申请人ID
     * @param clazzId      班级ID
     * @param respondentId 被申请人ID
     * @param type         申请类型
     * @param errMsg       出错信息
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     * @author changyuan.liu
     */
    private MapMessage sendApplication(long applicantId,
                                       long clazzId,
                                       long respondentId,
                                       ClazzTeacherAlterationType type,
                                       String errMsg) {
        MapMessage msg = teacherAlterationServiceClient.sendApplication(applicantId, respondentId, clazzId, type, OperationSourceType.wechat);
        if (!msg.isSuccess() && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)
                && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.NO_STUDENT_GROUP_ERR_MSG)
                && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.UNUSUAL_APPLICATIONS_ERR_MSG)) {
            msg = MapMessage.errorMessage(errMsg);
        }
        return msg;
    }

    /**
     * 申请消息生成器
     *
     * @author changyuan.liu
     */
    private abstract class ApplicationMessageBuilder {
        /**
         * 生成申请消息
         *
         * @param applicant  申请人
         * @param respondent 被申请人
         * @param clazz      班级
         * @return 申请消息
         */
        abstract String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz);
    }

    private void sendMessage(Teacher receiver, String payload) {
        if (StringUtils.isBlank(payload)) {
            return;
        }
        payload = StringUtils.replace(payload, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiver.getId(), payload);
    }

    private void doSendApplicationMessage(Teacher user,
                                          String message,
                                          boolean appendCheckDetailBtn,
                                          boolean needPopup) {
        // 发送站内通知
        if (appendCheckDetailBtn) {
            // FIXME 查看申请地址的链接应该不用改
            message = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【查看详情】</a>",
                    "/teacher/clazz/alteration/unprocessedapplication.vpage?type=someBodyToMe"
            );
            sendMessage(user, message);
        } else {
            sendMessage(user, message);
        }
        if (needPopup) {
            // 发送教师首页通知
            userPopupServiceClient.createPopup(user.getId())
                    .content(message)
                    .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
        }
    }

    //发送微信模版消息通知
    private void doSendApplicationMessageByWechat(Teacher applicant, Teacher respondent) {
        // 查询微信
        Map<String, Object> extensionInfo = MiscUtils.m("applicantId", applicant.getId(),
                "applicantName", applicant.fetchRealname(),
                "respondentId", respondent.getId(),
                "respondentName", respondent.fetchRealname());
        wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherClazzAlterationNotice,
                respondent.getId(), extensionInfo, WechatType.TEACHER);
    }

    //发送短信通知
    private void doSendApplicationMessageBySMS(Teacher applicant, Teacher respondent) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(respondent.getId());
        if (ua.isMobileAuthenticated()) {
            // 每个手机号，每天最多收到3个类似的班级调整的短信
            String userPhone = sensitiveUserDataServiceClient.loadUserMobile(ua.getId());
            if (smsServiceClient.getSmsService().canSendClazzManagement(userPhone).getUninterruptibly()) {
                String content = "您收到" + applicant.fetchRealname() + "老师的班级请求，点击链接立刻处理，" + "http://www.17zyw.cn/ABvANn";
                userSmsServiceClient.buildSms().to(ua)
                        .content(content)
                        .type(SmsType.CLAZZ_ALTERATION_NOTIFY)
                        .send();
            }
        }
    }

    /**
     * 给申请发送者发送消息提醒
     *
     * @param message              申请消息
     * @param messageBuilder       发送消息生成器
     * @param appendCheckDetailBtn 是否在消息提醒中添加‘查看详情’按钮
     * @param needPopup            是否弹窗
     * @author changyuan.liu
     */
    private void sendApplicationMessageToRespondent(MapMessage message,
                                                    ApplicationMessageBuilder messageBuilder,
                                                    boolean appendCheckDetailBtn,
                                                    boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(respondent, sendMsg, appendCheckDetailBtn, needPopup);

            Map<Long, List<UserWechatRef>> tid_refs_map = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(respondent.getId()));
            List<UserWechatRef> refs = tid_refs_map.get(respondent.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                //发送微信模版消息，如果applicant绑定了微信的话
                doSendApplicationMessageByWechat(applicant, respondent);
            } else {
                //否则，发送短信提醒消息
                doSendApplicationMessageBySMS(applicant, respondent);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            logger.error("Send application succeed but send message failed.", ex.getMessage());
        }
    }

    private void setClazzBook(Teacher teacher, long clazzId, int clazzLevel) {
        // TODO 需要加个检查，避免重复设定班级教材
        ExRegion region = userLoaderClient.loadUserRegion(teacher);
        RaikouRegionBufferDelegator buffer = new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer());
        Long bookId = contentLoaderClient.getExtension().initializeClazzBook(
                teacher.getSubject(), clazzLevel, region.getCode(), buffer);
        ChangeBookMapper cbm = new ChangeBookMapper();
        cbm.setType(0);
        cbm.setBooks(String.valueOf(bookId));
        cbm.setClazzs(String.valueOf(clazzId));
        try {
            contentServiceClient.setClazzBook(teacher, cbm);
        } catch (Exception ignored) {
            logger.warn("Failed to set clazz books [bookIds={},clazzIds={}]", cbm.getBooks(), cbm.getClazzs(), ignored);
        }
    }


    /**
     * 同意申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return
     * @author changyuan.liu
     */
    private MapMessage approveApplication(long respondentId,
                                          long recordId,
                                          ClazzTeacherAlterationType type,
                                          OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.approveApplication(respondentId, recordId, type, sourceType);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationServiceClient.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("批准申请失败");
        }
        return msg;
    }

    /**
     * 拒绝申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage rejectApplication(long respondentId,
                                         long recordId,
                                         ClazzTeacherAlterationType type,
                                         OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, type, sourceType);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationServiceClient.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("拒绝申请失败");
        }
        return msg;
    }

    private void sendApplicationMessageToApplicant(MapMessage message,
                                                   ApplicationMessageBuilder messageBuilder,
                                                   boolean appendCheckDetailBtn,
                                                   boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(applicant, sendMsg, appendCheckDetailBtn, needPopup);
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            logger.error("Send application succeed but send message failed.", ex.getMessage());
        }
    }

    private Map<String, String> passwordChangeTrackMap(Long userId, Long operatorId, String pos) {
        Map<String, String> map = new HashMap<>();
        map.put("user", SafeConverter.toString(userId));
        map.put("operator", SafeConverter.toString(operatorId));
        map.put("date", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
        map.put("pos", pos);
        map.put("env", RuntimeMode.current().name());
        return map;
    }

}
