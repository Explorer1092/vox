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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.service.user.AccountWebappService;
import com.voxlearning.ucenter.support.TeacherResourceDownloadHelper;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.mapper.TakeUpKlxStudent;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.ClazzService;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.Subjects.ALL_SUBJECTS;

/**
 * teache clazz controller
 * 未来考虑把TeacherSystemClazzController的部分东东移到这里
 * 然后把换班相关的移到单独的controller
 *
 * @author changyuan.liu
 * @since 2015.12.21
 */
@Controller
@RequestMapping("teacher/clazz")
public class TeacherClazzController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private AccountWebappService accountWebappService;
    @Inject private TeacherResourceDownloadHelper teacherResourceDownloadHelper;
    @Inject private SpecialTeacherServiceClient specialTeacherServiceClient;
    @Inject protected GlobalTagServiceClient globalTagServiceClient;

    /**
     * 班级管理主页
     */
    @RequestMapping(value = "managedclazzlist.vpage", method = RequestMethod.GET)
    public String listManagedClazzs(Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
            return "redirect:/teacher/index.vpage";
        }

        Map<String, Object> data = teacherSystemClazzServiceClient.loadSystemClazzManagementIndexData(teacher.getId());
        model.addAllAttributes(data);
        String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "/teacher/system/clazz/clazzindex", SafeConverter.toString(teacher.getId()));
        String mobile = phone != null ? phone : "";
        model.addAttribute("method", asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherMobileOrAccountCacheManager_getMethod(teacher.getId(), StringUtils.isNotBlank(mobile))
                .getUninterruptibly());
        model.addAttribute("mobile", mobile);
        model.addAttribute("validSubjects", ALL_SUBJECTS.stream().collect(Collectors.toMap(Subject::name, Subject::getValue)));

        //是否虚假老师
        if (teacherLoaderClient.isFakeTeacher(teacher.getId())) {
            model.addAttribute("isFakeTeacher", true);
        }
        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/systemclazz/kuailexue/clazzlist";
        } else {
            return "teacherv3/systemclazz/subpages/clazzlist";
        }
    }

    /**
     * 2014暑期改版 -- 认证教师点击【学生管理】或者非认证教师点击【学生详情】 -- 获取学生列表
     * 班级详情
     */
    @RequestMapping(value = "clazzsdetail.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String getDetailByClassId(Model model) {
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/teacher/index.vpage";
        }

        Teacher teacher = currentTeacher();
        Long teacherId = teacher.getId();
        Map<String, Object> detail;
        try {
            detail = teacherSystemClazzServiceClient.loadTeacherClazzDetail(teacherId, clazzId);
            // 绑定微信信息去掉，只留APP绑定信息
            List<Map<String, Object>> students = (List<Map<String, Object>>) detail.get("students");
            if (CollectionUtils.isNotEmpty(students)) {
                List<Long> studentIds = students.stream()
                        .filter(s -> s.get("studentId") != null)
                        .map(s -> SafeConverter.toLong(s.get("studentId")))
                        .collect(Collectors.toList());
                Map<Long, Set<Long>> studentBindAppParentMap = vendorServiceClient.studentBindAppParentMap(studentIds);
                students.stream().filter(s -> s.get("keyParentName") != null)
                        .forEach(s -> {
                            Long studentId = (Long) s.get("studentId");
                            s.put("keyParentAppBind", CollectionUtils.isNotEmpty(studentBindAppParentMap.get(studentId)));
                        });
            }
        } catch (Exception ex) {
            logger.error("FAILED TO LOAD TEACHER '{}' CLAZZ '{}' DETAIL", teacherId, clazzId, ex);
            detail = Collections.emptyMap();
        }
        if (MapUtils.isEmpty(detail)) {
            return "redirect:/";
        }

        model.addAllAttributes(detail);
        List<Clazz> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
        Collection<Clazz> nonterminalClazzs = teacherClazzs.stream()
                .filter(c -> c != null && (c.isPublicClazz() || c.isWalkingClazz()))
                .filter(t -> !t.isTerminalClazz())
                .collect(Collectors.toList());
        model.addAttribute("clazzs", nonterminalClazzs);
        model.addAttribute("isManager", teacherLoaderClient.isTeachingClazz(teacherId, clazzId));
        String phone = sensitiveUserDataServiceClient.showUserMobile(teacherId, "teacher/clazz/clazzsdetail", SafeConverter.toString(teacherId));
        String mobile = phone != null ? phone : null;
        model.addAttribute("method", asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherMobileOrAccountCacheManager_getMethod(teacherId, StringUtils.isNotBlank(mobile))
                .getUninterruptibly());
        model.addAttribute("mobile", mobile);
        boolean flag = BooleanUtils.toBoolean(String.valueOf(detail.get("klxScanMachineFlag")));
        if (teacher.isKLXTeacher() || (teacher.isJuniorEnglishOrChineseTeacher() && flag)) {
            //学生按照学号从小到大排序  没有学号排在前面
            List<Map<String, Object>> klxStudents = (List<Map<String, Object>>) detail.get("klxstudents");
            if (CollectionUtils.isNotEmpty(klxStudents)) {
                klxStudents.sort(Comparator.comparingLong(l -> SafeConverter.toLong(l.get("studentNumber"))));
            }
            model.addAttribute("klxstudents", klxStudents);
            model.addAttribute("klxNoScanNumberCount", detail.get("klxNoScanNumberCount"));
            model.addAttribute("klxScanMachineFlag", detail.get("klxScanMachineFlag"));
            model.addAttribute("scanNumberDigit", detail.get("scanNumberDigit"));
            if (teacher.isJuniorEnglishOrChineseTeacher() && flag) {
                return "teacherv3/systemclazz/subpages/clazzdetail";
            }
            return "teacherv3/systemclazz/kuailexue/clazzdetail";
        } else {
            return "teacherv3/systemclazz/subpages/clazzdetail";
        }
    }

    /**
     * 学生详情->点击模糊手机号->查看学生手机号
     *
     * @return
     */
    @RequestMapping(value = "getmobilebystuid.vpage")
    @ResponseBody
    public MapMessage getStudentMobile() {
        long studentId = getRequestLong("studentId");

        // 需要判断这个老师是否有知道学生手机号的权利
        List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachers(studentId);
        boolean hasPermission = clazzTeachers.stream().map(e -> e.getTeacher().getId())
                .anyMatch(e -> Objects.equals(e, currentUserId()));

        if (!hasPermission) {
            return MapMessage.errorMessage("非法的请求");
        }

        // redmine 21914
        // 1. 需要将PC端-我的班级-学生管理-“绑定手机”一列，只要绑定了学生手机or家长手机，都显示绑定手机号：
        // 1.1 优先显示学生手机，如果学生手机为空，则显示家长手机；
        // 1.2 如果有多个家长，优先显示关键家长，如果没有则任选一个；
        String studentOrParentMobile = studentLoaderClient.loadStudentOrParentMobile(studentId, SafeConverter.toString(currentUserId()));
        if (studentOrParentMobile == null) {
            return MapMessage.errorMessage("未查到绑定手机");
        }

        return MapMessage.successMessage().add("mobile", studentOrParentMobile);
    }

    /**
     * 2014暑期改版 -- 认证教师修改学生姓名 -- 班级管理互斥操作之一
     *
     * @param mapper
     * @return
     */
    @RequestMapping(value = "resetname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetname(@RequestBody Map<String, Object> mapper) {
        Long clazzId = ConversionUtils.toLong(mapper.get("clazzId"));
        Long studentId = ConversionUtils.toLong(mapper.get("userId"));
        String realname = ConversionUtils.toString(mapper.get("name"));

        if (badWordCheckerClient.containsUserNameBadWord(realname)) {
            return MapMessage.errorMessage("姓名请不要使用敏感词汇!");
        }

        // #36892 老师修改学生名字限制 非认证老师 && 同班有其他认证老师时不允许修改
        Teacher curTeacher = currentTeacher();
        if (curTeacher == null) {
            return MapMessage.errorMessage("请重新登陆!");
        }
        if (curTeacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(clazzId);
            for (ClazzTeacher clazzTeacher : clazzTeachers) {
                if (clazzTeacher.getTeacher() != null && clazzTeacher.getTeacher().fetchCertificationState() == AuthenticationState.SUCCESS) {
                    return MapMessage.errorMessage("需要达到认证，才能进行修改哦");
                }
            }
        }

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(studentId, clazzId)
                    .callback(() -> this.changeClazzStudentName(currentUser(), clazzId, studentId, realname))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        }
    }

    /**
     * 2014暑期改版 -- 认证教师修改学生密码 -- 班级管理互斥操作之二
     *
     * @param mapper
     * @return
     */
    @RequestMapping(value = "resetstudentpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetStudentPassword(@RequestBody Map<String, Object> mapper) {
        User teacher = currentUser();
        Long clazzId = ConversionUtils.toLong(mapper.get("clazzId"));
        Long studentId = ConversionUtils.toLong(mapper.get("userId"));
        String password = ConversionUtils.toString(mapper.get("password"));
        String confirmPassword = ConversionUtils.toString(mapper.get("confirmPassword"));

        // 如果学生未绑定手机，老师可以直接重置密码
        // 如果学生绑定了家长手机，则一天只能重置一次密码，且通过给手机发送随机密码的方式重置
        // 统一用一个接口并增加检查的原因是防止用户通过接口直接重置
        String studentOrParentMobile = studentLoaderClient.loadStudentOrParentMobile(studentId, SafeConverter.toString(teacher.getId()));
        if (StringUtils.isNotBlank(studentOrParentMobile)) {
            // 老师给学生重置密码行为，一天只能一次
            if (!asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherResetBindedStudentPWCacheManager_canResetPw(teacher.getId(), studentId)
                    .getUninterruptibly()) {
                return MapMessage.errorMessage("您已经帮助重置过，如果学生没有收到密码可以联系客服");
            }

            // 生成随机密码
            confirmPassword = password = RandomGenerator.generatePlainPassword();
        }

        // 修改密码
        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(studentId)
                    .proxy()
                    .changeClazzStudentPassword(teacher, clazzId, studentId, password, confirmPassword);
            if (mesg.isSuccess()) {
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(teacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("老师重置学生密码");
                userServiceRecord.setComments("老师重置学生[" + studentId + "]密码，操作端[pc]");
                userServiceRecord.setAdditions("refer:TeacherClazzController.resetStudentPassword");
                userServiceClient.saveUserServiceRecord(userServiceRecord);

                // 重置密码后处理
                User student = raikouSystem.loadUser(studentId);
                accountWebappService.onPasswordReset(student, password);

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

                // 老师修改学生密码,需要强制学生修改密码
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .unflushable_setUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, studentId, 1L, 0)
                        .awaitUninterruptibly();
            }

            return mesg;
        } catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        }
    }

    /**
     * 2014暑期改版 -- 认证教师删除单个学生 -- 班级管理互斥操作之三
     */
    @RequestMapping(value = "removestudent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage removeClazzStudent() {
        Long clazzId = getRequestLong("clazzId");
        Long studentId = getRequestLong("studentId");
        String klxStudentId = getRequestString("klxStudentUsername");

        if (clazzId == 0) {
            return MapMessage.errorMessage("班级ID错误");
        }
        // 希悦学校也不能操作
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        Teacher curTeacher = currentTeacher();
        // Feature #53519 开放初中英语、语文任课老师删除学生的权限
//        if (!curTeacher.isJuniorEnglishOrChineseTeacher()) {
//            // 检查教务老师
//            MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(curTeacher, "学校已有教务老师，请联系教务{}老师删除学生");
//            if (!checkMsg.isSuccess()) {
//                return checkMsg;
//            }
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(currentTeacherDetail().getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            return MapMessage.errorMessage("您不可自主调整班级或学生，如有疑问请联系一起客服");
        }

        // 删除快乐学用户
        if (StringUtils.isNotBlank(klxStudentId) && Objects.equals(studentId, 0L)) {
            return newKuailexueServiceClient.deleteKlxStudent(curTeacher.getId(), clazzId, klxStudentId);
        }

        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID错误");
        }
        try {
            User anotherTeacher = null;
            if (currentUser().fetchUserType() == UserType.TEACHER) {
                boolean isTeachingClazz = teacherLoaderClient.isTeachingClazz(currentUser().getId(), clazzId);
                if (!isTeachingClazz) { //如果老师不在这个班级中
                    Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(currentUser().getId());
                    for (Long tempTeacherId : relTeacherIds) {
                        User tempTeacher = teacherLoaderClient.loadTeacherDetail(tempTeacherId);
                        if (tempTeacher != null) {
                            if (teacherLoaderClient.isTeachingClazz(tempTeacher.getId(), clazzId)) {
                                anotherTeacher = tempTeacher;
                                break;
                            }
                        }
                    }
                }
            }

            final User user;
            if (null == anotherTeacher) {
                user = currentUser();
            } else {
                user = anotherTeacher;
            }
            Clazz c = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            MapMessage message = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(clazzId)
                    .proxy()
                    .deleteClazzStudent(user, c, studentId, null);
            if (message.isSuccess()) {
                Clazz clazz = (Clazz) message.get("clazz");
                User student = (User) message.get("student");
                String m = "{}老师将你移出了{}班，他/她可能觉得你不是这个班级的学生或者你已经有学号，如有疑问请直接联系老师！";
                m = StringUtils.formatMessage(m, user.fetchRealname(), clazz.formalizeClazzName());
                messageCommandServiceClient.getMessageCommandService().sendUserMessage(student.getId(), m);
            }
            return message;
        } catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        }
    }

    /**
     * 删除学生名单
     *
     * @author changyuan.liu
     */
    @RequestMapping(value = "removestudentname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeClazzStudentName() {
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("错误的班级ID");
        }
        String studentName = getRequestString("studentName");
        if (StringUtils.isBlank(studentName)) {
            return MapMessage.errorMessage("错误的学生姓名");
        }

        return clazzServiceClient.removeStudentNameFromList(Collections.singleton(studentName), clazzId, currentUserId());
    }

    /**
     * 批量下载班级学号
     */
    @RequestMapping(value = "batchdownload.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void batchDownloadClassNumber() throws IOException {
        List<Long> clazzIds = StringUtils.toLongList(getRequestString("clazzIds"));

        // 避免学生登录账号访问老师班级管理页面中的下载学生账号信息，抛异常。
        User user = currentUser();
        if (!User.isTeacherUser(user)) {
            getResponse().getWriter().write("老师帐号为空，请重新登录。");
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        TeacherDetail teacher = currentTeacherDetail();
        List<Long> cids = new ArrayList<>(clazzIds);
        DownloadContent downloadContent;
        try {
            Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(cids)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            // 记录下用户下载过名单
            userAttributeServiceClient.setExtensionAttribute(teacher.getId(), UserExtensionAttributeKeyType.TEACHER_DOWNLOAD_LIST);
            String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "/clazz/batchDownloadClassNumber", SafeConverter.toString(teacher.getId()));
            String mobile = (phone != null) ? phone : "";
            String method = asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherMobileOrAccountCacheManager_getMethod(teacher.getId(), StringUtils.isNotBlank(mobile))
                    .getUninterruptibly();
            downloadContent = teacherResourceDownloadHelper.downloadClazzStudentInformation(teacher, new ArrayList<>(clazzs.values()), mobile, method);
        } catch (Exception ex) {
            logger.error("FAILED TO DOWNLOAD TEACHER '{}' STUDENT INFORMATION", teacher.getId(), ex);
            downloadContent = null;
        }
        if (downloadContent == null) {
            getResponse().getWriter().write(teacher.getId() + "下载班级学号失败");
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            getWebRequestContext().downloadOctetStreamFile(
                    downloadContent.getFilename(),
                    downloadContent.getContent()
            );
        } catch (IOException ex) {
            getResponse().getWriter().write(teacher.getId() + "下载班级学号失败");
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * 批量下载班级学号（不含学生ID的）
     * 大概看了下逻辑，跟上面的是有一些不同的，这个用于老师下发学生注册方法
     * 上面的则是用于下载当前班级的学生信息的
     * TODO 跟上面的有大量代码重复，找时间重构吧
     * TODO 这块应该可以去掉了，前端有大量无用逻辑，找时间重构！！！
     * by changyuan.liu
     */
    @RequestMapping(value = "downloadnewnumber.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void downloadNewNumber() throws IOException {
        Long clazzId = ConversionUtils.toLong(getRequestString("clazzId"));
        // 避免学生登录账号访问老师班级管理页面中的下载学生账号信息，抛异常。
        User user = currentUser();
        if (!User.isTeacherUser(user)) {
            getResponse().getWriter().write("老师帐号为空，请重新登录。");
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        TeacherDetail teacher = currentTeacherDetail();
        List<Long> cids = Collections.singletonList(clazzId);
        DownloadContent downloadContent;
        try {
            Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(cids)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));
            // 记录下用户下载过名单
            userAttributeServiceClient.setExtensionAttribute(teacher.getId(), UserExtensionAttributeKeyType.TEACHER_DOWNLOAD_LIST);
            String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "/clazz/downloadnewnumber", SafeConverter.toString(teacher.getId()));
            String mobile = (phone != null) ? phone : "";
            String method = asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherMobileOrAccountCacheManager_getMethod(teacher.getId(), StringUtils.isNotBlank(mobile))
                    .getUninterruptibly();
            // 不需要老师输入了，默认60
            downloadContent = teacherResourceDownloadHelper.downloadNewNumber(teacher, new ArrayList<>(clazzs.values()), 60, mobile, method);
        } catch (Exception ex) {
            logger.error("FAILED TO DOWNLOAD TEACHER '{}' STUDENT INFORMATION", teacher.getId(), ex);
            downloadContent = null;
        }
        if (downloadContent == null) {
            getResponse().getWriter().write(teacher.getId() + "下载班级学号失败");
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            getWebRequestContext().downloadOctetStreamFile(
                    downloadContent.getFilename(),
                    downloadContent.getContent()
            );
        } catch (IOException ex) {
            getResponse().getWriter().write(teacher.getId() + "下载班级学号失败");
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * 学生管理 -> 导入学生名单
     * 读取老师班级列表
     *
     * @return
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzlist() {
        Teacher teacher = currentTeacher();
        try {
            MapMessage mapMessage = new MapMessage();
            List<Map<String, Object>> clazzMaps = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId())
                    .stream()
                    .filter(c -> !c.isTerminalClazz())
                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .map(c -> {
                        Map<String, Object> clazzMap = new HashMap<>();
                        clazzMap.put("clazzId", c.getId());
                        clazzMap.put("clazzName", c.formalizeClazzName());
                        return clazzMap;
                    })
                    .collect(Collectors.toList());
            mapMessage.setSuccess(true);
            mapMessage.add("clazzMaps", clazzMaps);
            return mapMessage;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("获取校讯通班级失败").add("errorInfo", ex);
        }
    }

    /**
     * 导入学生名单
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "importstudentnames.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importStudentNames(@RequestBody Map<String, Object> map) throws IOException {
        if (map == null) {
            return MapMessage.errorMessage();
        }

        long clazzId = SafeConverter.toLong(map.get("clazzId"));
        if (clazzId == 0) {
            return MapMessage.errorMessage();
        }

        String studentNamesStr = SafeConverter.toString(map.get("studentNames"));
        List<String> studentNames = new ArrayList<>();
        for (String userNameStr : studentNamesStr.split(",")) {
            if (!userNameStr.matches("^[\u2E80-\uFE4F]+(·[\u2E80-\uFE4F]+)*$")) {
                return MapMessage.errorMessage("学生姓名须导入中文");
            }
            if (userNameStr.length() > 12) {
                return MapMessage.errorMessage("学生姓名不超过12个字");
            }
            if (studentNames.contains(userNameStr)) {
                return MapMessage.errorMessage("导入名单有重名，请进行标记加以区分");
            }
            studentNames.add(userNameStr);
        }

        if (CollectionUtils.isEmpty(studentNames)) {
            return MapMessage.errorMessage();
        }

        User teacher = currentUser();
        if (!User.isTeacherUser(teacher)) {
            return MapMessage.errorMessage();
        }

        // 导入学生名单
        return clazzServiceClient.importStudentNameList(studentNames, clazzId, teacher.getId());
    }

    // 2014暑期改版 -- 认证教师批量删除学生 -- 班级管理互斥操作之四
    @RequestMapping(value = "batchremovestudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteStudents() {
        Map<String, Object> params;
        try {
            String json = IOUtils.toString(getRequest().getInputStream(), "UTF-8");
            params = JsonUtils.fromJson(json);
        } catch (Exception ignored) {
            params = null;
        }
        if (params == null) {
            return MapMessage.errorMessage("参数不全");
        }
        if (!(params.containsKey("deleteAll") && params.containsKey("studentIdList") && params.containsKey("clazzId"))) {
            return MapMessage.errorMessage("参数不全");
        }
        final User teacher = currentUser();
        try {
            final Long clazzId = ConversionUtils.toLong(params.get("clazzId"));
            Clazz c = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            List<Long> studentIdList = StringUtils.toLongList(params.get("studentIdList").toString());
            MapMessage message = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(teacher.getId(), clazzId)
                    .proxy()
                    .deleteClazzStudents(teacher, c, studentIdList, null);
            if (message.isSuccess()) {
                Clazz clazz = (Clazz) message.get("clazz");
                String m = "{}老师将你移出了{}班，他/她可能觉得你不是这个班级的学生或者你已经有学号，如有疑问请直接联系老师！";
                m = StringUtils.formatMessage(m, teacher.fetchRealname(), clazz.formalizeClazzName());
                Collection deletedStudentIds = (Collection) message.get("deletedStudentIds");
                for (Object deletedStudentId : deletedStudentIds) {
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage((Long) deletedStudentId, m);
                }
            }
            return message;
        } catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        }
    }

    /**
     * TODO 未来去掉
     * 目前因为消息能点进去，所以需要在这做个临时跳转，重定向到班级管理页面
     * 教师查看未处理的申请记录
     */
    @RequestMapping(value = "alteration/unprocessedapplication.vpage", method = RequestMethod.GET)
    public String pengdingList() {
        return "redirect:/teacher/systemclazz/clazzindex.vpage";
    }

    /**
     * 快乐学,老师批量导入学号/注册学生 excel模板
     */
    @RequestMapping(value = "kuailexue/clazzstutemplate.vpage", method = RequestMethod.GET)
    public void downloadKlxClazzStuTemplate() {
        String filePath = "/config/templates/klx_clazz_students_template.xls";
        try {
            Resource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                logger.error("download kuailexue clazz student template {} failed - template not exists", filePath);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "批量导入学生数据模版.xls";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download kuailexue clazz student template {} failed - ExMsg : {};", filePath, e);
        }
    }

    /**
     * 下载快乐学学生名单
     *
     * @param response
     */
    @RequestMapping(value = "kuailexue/downloadklxstuinfo.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public void downloadKlxStuInfo(HttpServletResponse response) {
        Long clazzId = getRequestLong("clazzId");
        Long teacherId = currentUserId();
        try {
            if (clazzId == 0) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 保证跟班级管理页面的数据一致
            Map<String, Object> detail = teacherSystemClazzServiceClient.loadTeacherClazzDetail(teacherId, clazzId);
            List<Map<String, Object>> students = (List<Map<String, Object>>) detail.get("students");
            List<Map<String, Object>> klxStudents = (List<Map<String, Object>>) detail.get("klxstudents");
            List<KlxStudent> mergedStudent = new LinkedList<>();

            // 页面取数的逻辑是，优先取klxstudents，如果没有再用students
            boolean flag = BooleanUtils.toBoolean(String.valueOf(detail.get("klxScanMachineFlag")));
            if (currentTeacher().isKLXTeacher() || (currentTeacher().isJuniorEnglishOrChineseTeacher() && flag)) {
                if (CollectionUtils.isNotEmpty(klxStudents)) {
                    klxStudents.forEach(student -> {
                        KlxStudent klxStudent = new KlxStudent();
                        klxStudent.setName(SafeConverter.toString(student.get("studentName")));
                        klxStudent.setA17id(SafeConverter.toLong(student.get("studentId")));
                        klxStudent.setStudentNumber(SafeConverter.toString(student.get("studentNumber")));
                        klxStudent.setScanNumber(SafeConverter.toString(student.get("scanNumber")));
                        mergedStudent.add(klxStudent);
                    });
                }
            } else {
                if (CollectionUtils.isNotEmpty(students)) {
                    students.forEach(student -> {
                        KlxStudent klxStudent = new KlxStudent();
                        klxStudent.setName(SafeConverter.toString(student.get("studentName")));
                        klxStudent.setA17id(SafeConverter.toLong(student.get("studentId")));
                        mergedStudent.add(klxStudent);
                    });
                }
            }


            String filename = "学生名单.xls";
            String teacherName = currentTeacher().fetchRealname();
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz != null) {
                String clazzName = clazz.getClassName();
                ClazzLevel clazzLevel = clazz.getClazzLevel();
                if (!teacherName.endsWith("老师")) {
                    teacherName = teacherName + "老师";
                }
                filename = teacherName + clazzLevel.getDescription() + clazzName + "学生名单.xls";
            }

            mergedStudent.sort(Comparator.comparingInt(mapper -> SafeConverter.toInt(mapper.getScanNumber())));
            //处理数据格式
            HSSFWorkbook hssfWorkbook = convertToHSSfWorkbook(mergedStudent);

            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            logger.error("下载学生名单失败: 老师{} 班级{} ! {}", teacherId, clazzId, ex.getMessage());
        }
    }

    /**
     * 在线添加快乐学学生
     */
    @ResponseBody
    @RequestMapping(value = "kuailexue/addstudentsonline.vpage", method = RequestMethod.POST)
    public MapMessage addStudentsOnline() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请老师登录后操作");
        }

        // 检查教务老师
//        MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(teacher, "学校已有教务老师，请联系教务{}老师导入学生名单");
//        if (!checkMsg.isSuccess()) {
//            return checkMsg;
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(currentTeacherDetail().getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            return MapMessage.errorMessage("您不可自主调整班级或学生，如有疑问请联系一起客服");
        }

        boolean checkRepeatStudent = getRequestBool("checkRepeatedStudent");
        boolean checkTakeUpStudent = getRequestBool("checkTakeUpStudent");
        Long clazzId = getRequestLong("clazzId");
        Long teacherId = getRequestLong("teacherId");
        String content = getRequestString("batchContext");
        String[] contents = content.split("\\n");
        Map<String, String> stuInfoMap = new HashMap<>();
        List<ImportStudentData> studentData = new LinkedList<>();
        List<String> dupStudentNames = new ArrayList<>();
        if (contents.length > 20) {
            return MapMessage.errorMessage("一次添加帐号不得超过20个");
        }
        for (int i = 0; i < contents.length; i++) {
            if (StringUtils.isEmpty(contents[i])) { //如果当前行为空则继续
                continue;
            }
            String[] contextArray = contents[i].trim().split("[\\s]+");
            String studentName = contextArray[0];
            String studentNumber = "";
            if (contextArray.length == 2) { // 学号存在才取
                studentNumber = contextArray[1];
            }
            // 检查姓名
            if (StringUtils.isNotBlank(studentName) && studentName.length() > 12) {
                return MapMessage.errorMessage("第{}行 填写的学生名{}过长", i + 1, studentName);
            }
            dupStudentNames.add(studentName);
            //批量导入学生
            stuInfoMap.put(studentName, studentNumber);
            studentData.add(new ImportStudentData(studentName, studentNumber, i + 1));
        }
        if (dupStudentNames.size() != dupStudentNames.stream().distinct().collect(Collectors.toList()).size()) {
            return MapMessage.errorMessage("学生姓名重复，请更改！");
        }

        return importKlxStudents(stuInfoMap, studentData, checkRepeatStudent, checkTakeUpStudent, clazzId, teacherId);
    }

    /**
     * 批量导入快乐学学生
     */
    @RequestMapping(value = "kuailexue/batchimportstudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage batchImportKlxStudents() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请老师登录后操作");
        }

        // 检查教务老师
//        MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(teacher, "学校已有教务老师，请联系教务{}老师导入学生名单");
//        if (!checkMsg.isSuccess()) {
//            return checkMsg;
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(currentTeacherDetail().getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            return MapMessage.errorMessage("您不可自主调整班级或学生，如有疑问请联系一起客服");
        }

        boolean checkRepeatStudent = getRequestBool("checkRepeatedStudent");
        boolean checkTakeUpStudent = getRequestBool("checkTakeUpStudent");
        Long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("班级信息有误");
        }

        //解析excel文档
        MapMessage mapMessage = readRequestWorkbook(getRequest(), "adjustExcel");
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        Workbook workbook = (Workbook) mapMessage.get("workbook");
        mapMessage = processImportByExcel(workbook);//解析excel文档内容
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        //批量导入学生
        Map<String, String> stuInfoMap = (Map<String, String>) mapMessage.get("stuInfoMap");
        List<ImportStudentData> studentData = (List<ImportStudentData>) mapMessage.get("studentData");

        return importKlxStudents(stuInfoMap, studentData, checkRepeatStudent, checkTakeUpStudent, clazzId, teacher.getId());
    }

    @RequestMapping(value = "kuailexue/editklxstudentname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editKlxStudentName() {
        Long clazzId = getRequestLong("clazzId");
        Long studentId = getRequestLong("studentId");
        String klxStudentUsername = getRequestString("klxStudentUserName");
        String klxStudentName = getRequestString("klxStudentName");
        if (StringUtils.isBlank(klxStudentName)) {
            return MapMessage.errorMessage("您有未输入的信息");
        }

        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || clazzId == 0) {
            return MapMessage.errorMessage("修改失败");
        }
        if (isCJLSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入数校平台，暂时无法进行调整。有疑问请联系客服。");
        }
        // 希悦学校也不能操作
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        if (studentId == 0 && StringUtils.isBlank(klxStudentUsername)) {//真实学生或虚拟学都不存在
            return MapMessage.errorMessage("修改失败");
        }

        return newKuailexueServiceClient.modifyKlxStudentName(studentId, klxStudentUsername, klxStudentName);
    }

    /**
     * 编辑快乐学学生
     *
     * @return
     */
    @RequestMapping(value = "kuailexue/editklxstudentinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editKlxStudentInfo() {
        Long clazzId = getRequestLong("clazzId");
        Long studentId = getRequestLong("studentId");
        String klxStudentUsername = getRequestString("klxStudentUserName");
        String klxStudentName = getRequestString("klxStudentName");
        String klxStudentNumber = getRequestString("klxStudentNumber");
        String klxStudentScanNumber = getRequestString("klxStudentScanNumber");
        Boolean isMarked = getRequestBool("isMarked");

        if (StringUtils.isBlank(klxStudentName) || StringUtils.isBlank(klxStudentNumber) || StringUtils.isBlank(klxStudentScanNumber)) {
            return MapMessage.errorMessage("您有未输入的信息");
        }

        if (klxStudentName.length() > 16) {
            return MapMessage.errorMessage("填写的学生名过长");
        }
        if (!StringUtils.isNumeric(klxStudentNumber)) {
            return MapMessage.errorMessage("请输入纯数字学号");
        }
        if (klxStudentNumber.length() > 14) {
            return MapMessage.errorMessage("填写的校内学号过长");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null || clazz.getSchoolId() == null) {
            return MapMessage.errorMessage("班级信息无效");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(clazz.getSchoolId())
                .getUninterruptibly();
        int digit = schoolExtInfo == null ? SchoolExtInfo.DefaultScanNumberDigit : schoolExtInfo.fetchScanNumberDigit();

        if (!StringUtils.isNumeric(klxStudentScanNumber) || klxStudentScanNumber.length() != digit) {
            return MapMessage.errorMessage("阅卷机号须为" + digit + "位数字");
        }

        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || clazzId == 0) {
            return MapMessage.errorMessage("修改失败");
        }
        if (isCJLSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入数校平台，暂时无法进行调整。有疑问请联系客服。");
        }
        // 希悦学校也不能操作
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        if (studentId == 0L && StringUtils.isBlank(klxStudentUsername)) {//真实学生或虚拟学都不存在
            return MapMessage.errorMessage("无效的学生");
        }

        return newKuailexueServiceClient.modifyKlxStudentInfo(teacher.getId(), clazzId, studentId, klxStudentUsername, klxStudentName, klxStudentNumber, klxStudentScanNumber, isMarked);
    }

    @RequestMapping(value = "kuailexue/changeartscience.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeClazzArtScienceType() {
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("参数错误");
        }

        String artScienceTypeStr = getRequestString("artScienceType");
        ArtScienceType artScienceType = ArtScienceType.of(artScienceTypeStr);
        if (artScienceType == ArtScienceType.UNKNOWN) {
            return MapMessage.errorMessage("参数错误");
        }

        return clazzServiceClient.changeClazzArtScienceType(clazzId, currentUserId(), artScienceType);
    }

    @RequestMapping(value = "kuailexue/getrelatedgroupinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getRelatedGroupInfo() {
        Long clazzId = getRequestLong("clazzId");
        Long studentId = getRequestLong("studentId");
        String klxStudentUsername = getRequestString("klxStudentUsername");
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        GroupMapper currentGroup = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), Collections.singleton(clazzId), false).get(clazzId);
        if (currentGroup == null) {
            return MapMessage.errorMessage("请重试");
        }

        List<Long> groupIds = null;
        if (!Objects.equals(studentId, 0L)) {//如果studentId存在,则是通过查studentId关联的group
            groupIds = groupLoaderClient.loadStudentGroups(studentId, false).stream()
                    .filter(groupMapper -> !Objects.equals(currentGroup.getId(), groupMapper.getId()))
                    .map(GroupMapper::getId)
                    .collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(klxStudentUsername)) {//如果studentId不存在,则是通过查klxStudentUsername关联的group
            groupIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findGroupKlxStudentRefsByStudent(klxStudentUsername)
                    .getUninterruptibly()
                    .stream().filter(ref -> !Objects.equals(ref.getGroupId(), currentGroup.getId()))
                    .map(GroupKlxStudentRef::getGroupId)
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(groupIds)) {
            return MapMessage.successMessage();
        }
        List<Map<String, String>> result = new ArrayList<>();
        teacherLoaderClient.loadGroupTeacher(groupIds).values()
                .stream().flatMap(Collection::stream)
                .filter(tempTeacher -> !Objects.equals(tempTeacher.getId(), teacher.getId()))
                .forEach(tempTeacher -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("teacherName", tempTeacher.fetchRealname());
                    map.put("subject", tempTeacher.getSubject() != null ? tempTeacher.getSubject().getValue() + "老师" : "老师");
                    result.add(map);
                });

        return MapMessage.successMessage().add("relatedTeacher", result);
    }

    @RequestMapping(value = "checkbeforeimport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkAffairTeacher() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null) {
            return MapMessage.errorMessage("请老师登录后操作");
        }

        // 检查教务老师
//        MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(teacher, "学校已有教务老师，请联系教务{}老师导入学生名单");
//        if (!checkMsg.isSuccess()) {
//            return checkMsg;
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(currentTeacherDetail().getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            return MapMessage.errorMessage("您不可自主调整班级或学生，如有疑问请联系一起客服");
        }
        // 陈经纶学校也不能操作
        if (isCJLSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入数校平台，暂时无法进行调整。有疑问请联系客服。");
        }
        // 希悦学校也不能操作
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        return MapMessage.successMessage();
    }

    /*
     * **************************************************private methods************************************
     * */

    private HSSFWorkbook convertToHSSfWorkbook(List<KlxStudent> klxStudents) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        //设置字体样式
        HSSFFont font = hssfWorkbook.createFont();
        font.setFontName("Verdana");
        font.setBoldweight((short) 100);
        font.setFontHeight((short) 300);
        font.setColor(HSSFColor.BLACK.index);

        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);
        stringStyle.setFont(font);

        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        hssfSheet.setColumnWidth(0, 6000);
        hssfSheet.setColumnWidth(1, 6000);
        hssfSheet.setColumnWidth(2, 6000);
        hssfSheet.setColumnWidth(3, 6000);

        HSSFRow firstRow = createRow(hssfSheet, 0, 3, stringStyle);
        setCellValue(firstRow, 0, stringStyle, "学生姓名");
        setCellValue(firstRow, 1, stringStyle, "校内学号");
        setCellValue(firstRow, 2, stringStyle, "一起作业id");
        setCellValue(firstRow, 3, stringStyle, "阅卷机填涂号");


        if (CollectionUtils.isNotEmpty(klxStudents)) {
            for (int index = 0; index < klxStudents.size(); index++) {
                KlxStudent klxStudent = klxStudents.get(index);
                String name = StringUtils.isBlank(klxStudent.getName()) ? "未添加" : klxStudent.getName();
                String studentNumber = StringUtils.isBlank(klxStudent.getStudentNumber()) ? "未添加" : klxStudent.getStudentNumber();
                String userId = klxStudent.isRealStudent() ? klxStudent.getA17id().toString() : "未注册";
                String scanNumber = StringUtils.isBlank(klxStudent.getScanNumber()) ? "未添加" : klxStudent.getScanNumber();

                HSSFRow row = createRow(hssfSheet, index + 1, 3, stringStyle);
                setCellValue(row, 0, stringStyle, name);
                setCellValue(row, 1, stringStyle, studentNumber);
                setCellValue(row, 2, stringStyle, userId);
                setCellValue(row, 3, stringStyle, scanNumber);
            }
        }

        return hssfWorkbook;
    }

    private MapMessage processImportByExcel(Workbook workbook) {
        if (workbook == null) {
            return MapMessage.errorMessage("文档解析错误");
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("excel文件中的sheet1内容不能为空");
        }

        int trLength = sheet.getLastRowNum() + 1;//总行数(包括首行)

        if (trLength < 2) {//excel第二行及以下的A列为空时，报错——excel为空
            return MapMessage.errorMessage("excel为空");
        }
        if (trLength > 151) {//excel内容>151行时，报错——导入学生不能超过150人
            return MapMessage.errorMessage("导入学生不能超过150人");
        }

        List<String> errorList = new ArrayList<>();
        Map<String, String> stuInfoMap = new LinkedHashMap<>();
        List<ImportStudentData> studentData = new LinkedList<>();
        Map<String, List<Integer>> studentNames = new LinkedHashMap<>();
        Map<String, List<Integer>> studentNumbers = new LinkedHashMap<>();
        for (int rowIndex = 1; rowIndex < trLength; ++rowIndex) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            String studentName = "";
            String studentNumber = "";

            Cell studentNameCell = row.getCell(0);
            if (studentNameCell != null) {
                studentNameCell.setCellType(Cell.CELL_TYPE_STRING);
                studentName = StringUtils.replaceAll(studentNameCell.getStringCellValue(), "\\s*", "");
            }

            Cell studentNumberCell = row.getCell(1);
            if (studentNumberCell != null) {
                studentNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                studentNumber = StringUtils.replaceAll(studentNumberCell.getStringCellValue(), "\\s*", "");
            }

            // 读到了一行空数据, 忽略咯
            if (StringUtils.isBlank(studentName) && StringUtils.isBlank(studentNumber)) continue;

            stuInfoMap.put(studentName, studentNumber);
            studentData.add(new ImportStudentData(studentName, studentNumber, rowIndex + 1));
            errorList.addAll(checkLine(studentName, studentNumber, rowIndex + 1));
            collect(studentNames, studentName, rowIndex + 1);
            collect(studentNumbers, studentNumber, rowIndex + 1);
        }

        // 收集错误信息
        for (Map.Entry<String, List<Integer>> entry : studentNames.entrySet()) {
            if (entry.getValue().size() > 1) {
                errorList.add("表格中学生姓名【" + entry.getKey() + "】重复，位于第" + StringUtils.join(entry.getValue(), "、") + "行");
            }
        }
        for (Map.Entry<String, List<Integer>> entry : studentNumbers.entrySet()) {
            if (entry.getValue().size() > 1) {
                errorList.add("表格中学生学号【" + entry.getKey() + "】重复，位于第" + StringUtils.join(entry.getValue(), "、") + "行");
            }
        }

        if (errorList.size() == 0) {
            return MapMessage.successMessage().add("stuInfoMap", stuInfoMap).add("studentData", studentData);
        }

        if (errorList.size() > 5) {
            errorList = errorList.subList(0, 5);
        }
        return MapMessage.errorMessage(StringUtils.join(errorList, "<br/>"));
    }

    private void write(InputStream in, OutputStream out) throws Exception {
        int BYTES_BUFFER_SIZE = 1024 * 8;
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    private MapMessage readRequestWorkbook(HttpServletRequest request, String name) {

        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return MapMessage.errorMessage("文档解析错误");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file == null || file.isEmpty()) {
                logger.warn("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return MapMessage.errorMessage("文档解析错误");
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.warn("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return MapMessage.errorMessage("仅支持上传excel文档");
            }
            @Cleanup InputStream in = file.getInputStream();
            return MapMessage.successMessage().add("workbook", WorkbookFactory.create(in));
        } catch (Exception e) {
            logger.warn("readRequestWorkbook -  文档解析格式有误: {}", name);
            return MapMessage.errorMessage("文档解析错误");
        }
    }

    private HSSFRow createRow(HSSFSheet sheet, int rowNum, int column, CellStyle style) {
        HSSFRow row = sheet.createRow(rowNum);
        for (int i = 0; i <= column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
        return row;
    }

    private void setCellValue(HSSFRow row, int column, CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value == null ? "" : value);
    }

    private MapMessage changeClazzStudentName(User teacher, Long clazzId, Long studentId, String realname) {
        if (!User.isTeacherUser(teacher)) {
            return MapMessage.errorMessage();
        }
        try {
            RealnameRule.validateRealname(realname);
        } catch (Exception ex) {
            return MapMessage.errorMessage("修改学生姓名失败");
        }
        if (!hasRelTeacherTeachingClazz(teacher.getId(), clazzId)) {
            return MapMessage.errorMessage("修改学生姓名失败");
        }

        Set<Long> clazzStudentIds = new HashSet<>(asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId));
        if (!clazzStudentIds.contains(studentId)) {
            return MapMessage.errorMessage("修改学生姓名失败");
        }
        try {
            // Feature #54929
//            if (ForbidModifyNameAndPortrait.check()) {
//                return ForbidModifyNameAndPortrait.errorMessage;
//            }
            User userOld = raikouSystem.loadUser(studentId);
            MapMessage message = userServiceClient.changeName(studentId, realname);

            User user = raikouSystem.loadUser(studentId);
            if (user != null) {
                com.voxlearning.alps.spi.bootstrap.LogCollector.info("backend-general", MiscUtils.map("usertoken", user.getId(),
                        "usertype", user.getUserType(),
                        "platform", "crm",
                        "version", "",
                        "op", "change user name",
                        "mod1", user.fetchRealname(),
                        "mod2", realname,
                        "mod3", user.getAuthenticationState(),
                        "mod4", teacher.getId()));
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(teacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("老师修改学生姓名");
                userServiceRecord.setComments("老师[" + teacher.getId() + "]修改学生[" + studentId + userOld.fetchRealname() + "]姓名为[" + realname + "]，操作端[pc]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }
            return message;
        } catch (Exception ex) {
            logger.error("Failed to change clazz student name", ex);
            return MapMessage.errorMessage("修改学生姓名失败");
        }
    }

    private boolean hasRelTeacherTeachingClazz(Long teacherId, Long clazzId) {
        if (teacherId == null || clazzId == null) {
            return false;
        }
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);// 包班制支持
        return relTeacherIds.stream().anyMatch(id -> teacherLoaderClient.isTeachingClazz(id, clazzId));
    }

    private MapMessage importKlxStudents(Map<String, String> stuInfoMap, List<ImportStudentData> studentData, boolean checkRepeatStudent, boolean checkTakeUpStudent, Long clazzId, Long teacherId) {
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxTeacherClazzStudents(teacherId, clazzId);
        Set<Integer> duplicateNumberList = new TreeSet<>();  // 重复学号的行数
        Map<String, KlxStudent> existNumbers;
        try {
            existNumbers = klxStudents.stream()
                    .filter(klxStudent -> StringUtils.isNotBlank(klxStudent.getStudentNumber()))
                    .collect(Collectors.toMap(KlxStudent::getStudentNumber, Function.identity(), (u, v) -> {
                        logger.error("Duplicate klxStudent StudentNumber found, please check it . studentId=({} , {}), studentNumber={}", u.getId(), v.getId(), u.getStudentNumber());
                        throw new IllegalArgumentException("班级内学生[" + u.getName() + "、" + v.getName() + "]学号[" + u.getStudentNumber() + "]重复，请先处理");
                    }, LinkedHashMap::new));
        } catch (IllegalArgumentException ex) {
            return MapMessage.errorMessage(ex.getMessage());
        }

        for (ImportStudentData data : studentData) {
            String stuNum = data.getStudentNumber();
            if (!existNumbers.containsKey(stuNum) || existNumbers.get(stuNum) == null) {
                continue;
            }
            // 姓名和填涂号都相同的话可以忽略
            if (!StringUtils.equals(data.getStudentName(), existNumbers.get(stuNum).getName())) {
                duplicateNumberList.add(data.getRow());
            }
        }

        if (CollectionUtils.isNotEmpty(duplicateNumberList)) {
            List<String> lines = duplicateNumberList.stream()
                    .map(line -> "第" + line + "行")
                    .collect(Collectors.toList());
            return MapMessage.errorMessage("导入的学生学号在班内重复，位于{}", StringUtils.join(lines, "、"));
        }

        //检查班内重名学生 无论是否有重复学生,一定会返回前端
        if (checkRepeatStudent) {
            //如果没有重复学生直接导入,否则返回重复的学生姓名
            List<String> repeatedStudentList = new ArrayList<>();

            Map<String, KlxStudent> existNames;
            try {
                existNames = klxStudents.stream()
                        .filter(klxStudent -> StringUtils.isNotBlank(klxStudent.realName()))
                        .collect(Collectors.toMap(KlxStudent::realName, Function.identity(), (u, v) -> {
                            logger.error("Duplicate klxStudent StudentName found, please check it . studentId=({} , {}), studentName={}", u.getId(), v.getId(), u.getName());
                            throw new IllegalArgumentException("班级内学生[" + u.getName() + "、" + v.getName() + "]姓名重复，请先处理");
                        }, LinkedHashMap::new));
            } catch (IllegalArgumentException ex) {
                return MapMessage.errorMessage(ex.getMessage());
            }

            for (ImportStudentData data : studentData) {
                String stuName = data.getStudentName();
                if (!existNames.containsKey(stuName) || existNames.get(stuName) == null) {
                    continue;
                }
                // 姓名和填涂号都相同的话可以忽略
                if (!StringUtils.equals(data.getStudentNumber(), existNames.get(stuName).getStudentNumber())) {
                    repeatedStudentList.add(stuName);
                }
            }

            // #55742 group内人数是否>100人 提示文案
            int count = 0;
            for (KlxStudent klxStudent : klxStudents) {
                if (stuInfoMap.keySet().contains(klxStudent.getName())) {
                    count++;
                }
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if ((klxStudents.size() + stuInfoMap.size() - count) > globalTagServiceClient.getGlobalTagBuffer()
                    .loadSchoolMaxClassCapacity(clazz.getSchoolId(), ClazzConstants.MAX_CLAZZ_CAPACITY)) {
                return MapMessage.errorMessage("班级学生数不能超过100人");
            }

            if (CollectionUtils.isNotEmpty(repeatedStudentList)) { // 如果有重名学生
                boolean moreStudent = false;
                if (repeatedStudentList.size() > 3) {
                    repeatedStudentList = repeatedStudentList.subList(0, 3);
                    moreStudent = true;
                }
                return MapMessage.successMessage().add("repeatedStudentList", repeatedStudentList).add("moreStudent", moreStudent);
            }
            return MapMessage.successMessage();
        }

        // 根据学号后N位,找到占用的学生班级老师信息;如果有占用的话,直接返回;否则,直接导入学生
        if (checkTakeUpStudent) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            Long schoolId = clazz == null ? null : clazz.getSchoolId();
            if (schoolId == null) {
                return MapMessage.errorMessage("导入失败，请联系客服");
            }

            GroupMapper groupMapper = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
            if (groupMapper == null) {
                return MapMessage.errorMessage("导入失败，请联系客服");
            }

            // 找到被占用的学生信息
            List<TakeUpKlxStudent> takeUpKlxStudents = newKuailexueLoaderClient.pickTakeUpKlxStudents(schoolId, teacherId, clazzId, stuInfoMap, 3);

            // 处理前端弹窗跳转
            if (CollectionUtils.isNotEmpty(takeUpKlxStudents)) {
                List<String> importNames = new ArrayList<>();
                for (TakeUpKlxStudent student : takeUpKlxStudents) {
                    String studentNumber = student.getOldStudentNumber();
                    stuInfoMap.forEach((name, number) -> {
                        if (Objects.equals(number, studentNumber)) {
                            importNames.add(name);
                        }
                    });
                }
                return MapMessage.successMessage().add("isTakeUp", true)
                        .add("importNames", importNames.size() > 3 ? importNames.subList(0, 3) : importNames)
                        .add("moreFlag", importNames.size() >= 3)
                        .add("takeUpInfo", takeUpKlxStudents);
            }
        }

        //处理本班级下的所有班组中(当前班组除外) 校内学号+姓名完全匹配的学生
        MapMessage matchStuMessage = newKuailexueServiceClient.batchImportKlxStudents(teacherId, clazzId, stuInfoMap, KlxStudent.ImportSource.ordinaryteacher);
        if (!matchStuMessage.isSuccess()) {
            return MapMessage.errorMessage("导入失败,请联系客服");
        }
        return matchStuMessage;
    }

    private List<String> checkLine(String studentName, String studentNumber, int rowIndex) {
        List<String> errorList = new ArrayList<>();
        if (!StringUtils.isNumeric(studentNumber)) {
            errorList.add("校内学号请填写数字,位于第" + rowIndex + "行");
        }
        if (studentNumber.length() > 14) {
            errorList.add("填写的校内学号过长,位于第" + rowIndex + "行");
        }
        if (StringUtils.isBlank(studentName)) {
            errorList.add("学生姓名不能为空，位于第" + rowIndex + "行");
        }
        if (studentName.length() > 12) {
            errorList.add("填写的学生名过长，位于第" + rowIndex + "行");
        }
        return errorList;
    }

    private void collect(Map<String, List<Integer>> map, String key, Integer value) {
        map.computeIfAbsent(key, k -> new ArrayList<>());
        map.get(key).add(value);
    }

    @Getter
    @Setter
    private static class ImportStudentData {
        private String studentName;
        private String studentNumber;
        private int row;

        ImportStudentData(String studentName, String studentNumber, int row) {
            this.studentName = studentName;
            this.studentNumber = studentNumber;
            this.row = row;
        }

    }

}
