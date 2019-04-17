package com.voxlearning.utopia.mizar.controller.course;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.entity.o2o.TrusteeReserveRecord;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.mizar.utils.XssfUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseTargetType;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourseUserRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.TalkFunCourse;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourse;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourseTarget;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.mapper.MicroCourseSummary;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_CourseReport;
import com.voxlearning.utopia.service.mizar.api.service.talkfun.TalkFunService;
import com.voxlearning.utopia.service.mizar.api.utils.MicroCourseMsgTemplate;
import com.voxlearning.utopia.service.mizar.client.AsyncMizarCacheServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MicroCourseLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MicroCourseServiceClient;
import com.voxlearning.utopia.service.mizar.talkfun.TalkFunConstants;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微课堂管理页面
 * Created by Yuechen.Wang on 2016/12/9.
 */
@Controller
@RequestMapping(value = "/course/manage")
public class MicroCourseController extends AbstractMizarController {

    @Inject private AsyncMizarCacheServiceClient asyncMizarCacheServiceClient;

    @Inject private MicroCourseLoaderClient microCourseLoaderClient;
    @Inject private MicroCourseServiceClient microCourseServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    @ImportService(interfaceClass = TalkFunService.class)
    private TalkFunService talkFunService;

    // 用于比较两个实体的字段
    private static final List<String> COMPARE_FIELDS = Arrays.asList(
            "theme"          // 课时主题
            , "startTime"    // 上课时间
            , "endTime"      // 下课时间
            , "duration"     // 时长
            , "price"        // 原价
            , "info"         // 课时介绍
            , "photo"        // 详情图片
            , "url"          // 课时视频
            , "tkCourse"     // 欢拓自助ID
            , "btnContent"   // 按钮文字
            , "smsNotify"    // 短信提醒
            , "liveUrl"      // 直播地址
            , "replayUrl"    // 回放地址
            , "tip"          // 备注提示文字
            , "spreadText"   // 推广文字
            , "spreadUrl"    // 推广链接
            , "longClassUrl" // 配套长期班URL
            , "longClassPhoto" // 配套长期班图片
    );

    private static final List<String> validCategory = Arrays.asList(
            MizarCourseCategory.MICRO_COURSE_OPENING.name(),
            MizarCourseCategory.MICRO_COURSE_NORMAL.name()
    );

    // 课程列表页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String courseIndex(Model model) {
        Integer status = getRequestInt("status", 3); // 状态
        String category = requestString("category");      // 分类
        String courseName = requestString("course");      // 课程名称
        int pageNum = Integer.max(1, getRequestInt("page")); // 页码
        model.addAttribute("status", status);
        model.addAttribute("course", courseName);
        model.addAttribute("category", category);

        MicroCourseStatus st = MicroCourseStatus.parse(status);
        Pageable pageable = new PageRequest(pageNum - 1, 5);
        Page<MicroCourseSummary> coursePage;
        if (getCurrentUser().isMicroTeacher()) {
            coursePage = microCourseLoaderClient.loadUserCourses(currentUserId(), pageable);
        } else {
            coursePage = microCourseLoaderClient.findCoursesByParam(courseName, category, st, pageable);
        }
        // 排序
        List<MicroCourseSummary> courseList = coursePage.getContent().stream()
                .sorted(MicroCourseSummary::compareTo)
                .collect(Collectors.toList());

        model.addAttribute("courseList", courseList);
        model.addAttribute("pageIndex", pageNum);
        model.addAttribute("totalPage", Integer.max(1, coursePage.getTotalPages()));
        return "course/index";
    }

    // 课程上下线
    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeStatus() {
        String courseId = getRequestString("course");
        String st = getRequestString("status");
        try {
            MicroCourseStatus status = MicroCourseStatus.parse(st);
            if (status == null) {
                return MapMessage.errorMessage("无效的状态");
            }
            MicroCourseSummary course = microCourseLoaderClient.loadCourseById(courseId);
            if (course == null) {
                return MapMessage.errorMessage("无效的课程信息");
            }
            // 上线之前查看相关课时是否有效
            if (CollectionUtils.isEmpty(course.getPeriodRefs()) || course.getLatestPeriod() == null) {
                return MapMessage.errorMessage("该课程没有有效的课时信息");
            }
            // 状态已经变更直接返回成功
            if (status == course.getStatus()) {
                return MapMessage.successMessage();
            }
            return microCourseServiceClient.updateCourseStatus(courseId, status);

        } catch (Exception ex) {
            logger.error("Failed change Micro Course Status, course={}, status={}", courseId, st, ex);
            return MapMessage.errorMessage("状态变更失败:" + ex.getMessage());
        }
    }

    // 课程详情页面
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        model.addAttribute("mode", getRequestString("mode"));
        String courseId = getRequestString("cid");
        if (StringUtils.isBlank(courseId)) {
            return "course/new_and_edit"; //　此时认为是新增课程
        }
        // 课程详情
        MicroCourseSummary course = microCourseLoaderClient.loadCourseById(courseId);
        if (course == null) {
            return redirect("index.vpage");
        }
        model.addAttribute("course", course);
        // 课时列表
        List<MicroCoursePeriod> periodList = microCourseLoaderClient
                .getCourseLoader()
                .loadCoursePeriods(course.getPeriodRefs())
                .values()
                .stream()
                .sorted(MicroCoursePeriod::sortByTime)
                .collect(Collectors.toList());
        model.addAttribute("periods", mapCoursePeriod(periodList));
        // 同部门老师列表
        List<MizarUser> teacherList = mizarUserLoaderClient.loadSameDepartmentUser(currentUserId(), MizarUserRoleType.MicroTeacher)
                .stream()
                .filter(MizarUser::isValid)
                .collect(Collectors.toList());
        model.addAttribute("lecturers", mapTeacherInfo(course.getLecturers(), teacherList));
        model.addAttribute("assistants", mapTeacherInfo(course.getAssistants(), teacherList));
        return "course/new_and_edit";
    }

    // 老师查看课程详情
    @RequestMapping(value = "classroom.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String classroom(Model model) {
        String courseId = getRequestString("cid");
        // 课程详情
        MicroCourseSummary course = microCourseLoaderClient.loadCourseById(courseId);
        if (course == null || course.getLatestPeriod() == null) {
            return redirect("index.vpage");
        }
        // 最近一节课时
        MicroCoursePeriod latestPeriod = course.getLatestPeriod();
        MicroCourseUserRef.CourseUserRole role = microCourseLoaderClient.checkCourseUserRole(course.getCourseId(), currentUserId());
        if (role == null) {
            return redirect("index.vpage");
        }
        model.addAttribute("course", course);
        model.addAttribute("period", latestPeriod);
        model.addAttribute("role", role.name());
        MapMessage launch = talkFunService.launch(latestPeriod.getId());
        if (launch.isSuccess()) {
            Map<String, Object> dataMap = (Map<String, Object>) launch.get("data");
            model.addAttribute("helpUrl", dataMap.get("url"));
            model.addAttribute("protocol", dataMap.get("protocol"));
            model.addAttribute("downloadUrl", dataMap.get("url"));
        } else {
            model.addAttribute("error", launch.getInfo());
        }
        if (role == MicroCourseUserRef.CourseUserRole.Assistant) {
            // 放一个头像玩玩吧
            MizarUser user = mizarUserLoaderClient.loadUser(currentUserId());
            Map<String, Object> options = MapUtils.m("avatar", SafeConverter.toString(user.getPortrait()), "ssl", true);
            MapMessage message = talkFunService.courseEntrance(latestPeriod.getId(), currentUserId(), getCurrentUser().getRealName(), TalkFunConstants.ROLE_ADMIN, options);
            if (message.isSuccess()) {
                String liveUrl = SafeConverter.toString(message.get("entrance"));
                model.addAttribute("liveUrl", liveUrl);
            } else {
                model.addAttribute("error", message.getInfo());
            }
        }
        return "course/classroom";
    }

    // 老师下载直播软件
    @RequestMapping(value = "tk_download.vpage", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public String talkFunDownload(Model model) {
        model.addAttribute("downloadUrl", "http://t.talk-fun.com/live");
        return "course/downloadclient";
    }

    // 保存课程实体
    @RequestMapping(value = "savecourse.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveCourse() {
        String courseId = null;
        try {
            MicroCourse course = requestEntity(MicroCourse.class);
            if (course == null) {
                return MapMessage.errorMessage("无效的参数");
            }
            if (Boolean.TRUE.equals(course.getPayAll()) && !getRequestString("price").matches(MONEY_PATTERN)) {
                return MapMessage.errorMessage("【课程价格】格式不正确!");
            }
            // 校验课程
            MapMessage validMsg = validateCourse(course);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            course.setStatus(MicroCourseStatus.OFFLINE.getOrder());
            course.setDisabled(false);
            courseId = course.getId();
            if (StringUtils.isNotBlank(courseId) && microCourseLoaderClient.getCourseLoader().loadMicroCourse(courseId) == null) {
                return MapMessage.errorMessage("无效的课程ID：[" + courseId + "]");
            }
            return microCourseServiceClient.saveCourse(course);
        } catch (Exception ex) {
            logger.error("Failed Save Micro Course, course={}", SafeConverter.toString(courseId), ex);
            return MapMessage.errorMessage("添加老师异常:" + ex.getMessage());
        }
    }

    // 删除课程实体
    @RequestMapping(value = "removecourse.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeCourse() {
        String courseId = getRequestString("courseId");
        try {
            return microCourseServiceClient.removeCourse(courseId);
        } catch (Exception ex) {
            logger.error("Failed Save Micro Course, course={}", SafeConverter.toString(courseId), ex);
            return MapMessage.errorMessage("添加老师异常:" + ex.getMessage());
        }
    }

    // 增加关联老师
    @RequestMapping(value = "appendteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage appendTeacher() {
        List<String> teachers = requestStringList("teachers"); // 老师ID参数, 以英文逗号分隔的字符串
        String courseId = getRequestString("course");  // 课程ID参数
        String roleType = getRequestString("role"); // 关联角色参数, 值为[Lecturer/Assistant]
        try {
            MicroCourseUserRef.CourseUserRole role = MicroCourseUserRef.CourseUserRole.parse(roleType);
            if (role == null) {
                return MapMessage.errorMessage("无效的老师角色：[" + roleType + "]");
            }
            MicroCourse course = microCourseLoaderClient.getCourseLoader().loadMicroCourse(courseId);
            if (course == null || course.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的课程ID：[" + courseId + "]");
            }
            if (CollectionUtils.isNotEmpty(teachers)) {
                Map<String, MizarUser> userMap = mizarUserLoaderClient.loadUsers(teachers);
                if (userMap.isEmpty()) {
                    return MapMessage.errorMessage("请选择有效的老师");
                }
                teachers = new ArrayList<>(userMap.keySet());
            }
            return microCourseServiceClient.appendTeacher(courseId, teachers, role);
        } catch (Exception ex) {
            logger.error("Failed append Micro Course Teacher, course={}, teacher={}, role={}", courseId, Arrays.toString(teachers.toArray()), roleType, ex);
            return MapMessage.errorMessage("添加老师异常:" + ex.getMessage());
        }
    }

    // 修改课程状态
    @RequestMapping(value = "valar-morghulis.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetTalkFunCourseStatus() {
        String periodId = getRequestString("id");
        String operation = getRequestString("op");
        if (StringUtils.isAnyBlank(periodId, operation)) {
            return MapMessage.errorMessage("参数异常");
        }
        TalkFunCourse.Status status;
        try {
            status = TalkFunCourse.Status.valueOf(operation);
        } catch (Exception ignored) {
            return MapMessage.errorMessage("无效的指令");
        }
        return talkFunService.changeTalkFunStatus(periodId, status);
    }

    // 获取课时实体
    @RequestMapping(value = "periodinfo.vpage", method = RequestMethod.GET)
    public String periodEditor(Model model) {
        String periodId = getRequestString("periodId");
        String courseId = getRequestString("courseId");
        String mode = getRequestString("mode");
        model.addAttribute("mode", mode);
        if (!"view".equals(mode) && (StringUtils.isBlank(courseId) || microCourseLoaderClient.getCourseLoader().loadMicroCourse(courseId) == null)) {
            return redirect("index.vpage");
        }
        if (StringUtils.isNotBlank(periodId)) {
            MicroCoursePeriod period = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(periodId);
            if (period == null || period.isDisabledTrue()) {
                return redirect("detail.vpage?cid=" + courseId);
            }
            model.addAttribute("period", period);
            TalkFunCourse tkCourse = talkFunService.loadTalkFunCourse(periodId);
            if (tkCourse != null) {
                model.addAttribute("talkFun", JsonUtils.toJsonPretty(tkCourse));
                model.addAttribute("tkCourse", tkCourse.getCourseId());
                model.addAttribute("manual", tkCourse.isManually());
            }
        }
        model.addAttribute("courseId", courseId);
        return "course/period";
    }

    // 添加课时实体
    @RequestMapping(value = "appendperiod.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage appendPeriod() {
        String courseId = getRequestString("course");
        try {
            MicroCourse course = microCourseLoaderClient.getCourseLoader().loadMicroCourse(courseId);
            if (course == null) {
                return MapMessage.errorMessage("无效的课程ID：[" + courseId + "]");
            }
            MicroCoursePeriod period = requestEntity(MicroCoursePeriod.class);
            if (period == null) {
                return MapMessage.errorMessage("无效的参数");
            }
            if (!getRequestString("price").matches(MONEY_PATTERN)) {
                return MapMessage.errorMessage("【课时价格】格式不正确!");
            }
            String beforeBtn = getRequestString("before");
            String afterBtn = getRequestString("after");
            Map<String, String> btnContent = new HashMap<>();
            btnContent.put(MicroCoursePeriod.BTN_BEFORE_PURCHASE, beforeBtn);
            btnContent.put(MicroCoursePeriod.BTN_AFTER_PURCHASE, afterBtn);
            period.setBtnContent(btnContent);
            period.setDisabled(false);
            // 校验课时输入
            MapMessage validMsg = validatePeriod(period);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            // 先保存课时实体
            MapMessage message = microCourseServiceClient.savePeriod(period);
            if (!message.isSuccess()) {
                return message;
            }
            // 拿到返回的实体ID
            String periodId = SafeConverter.toString(message.get("periodId"));
            // 增加关联
            message = microCourseServiceClient.appendPeriod(courseId, periodId);
            if (!message.isSuccess()) {
                return message;
            }

            String funTalkCourse = getRequestString("talkfun");
            if (StringUtils.isNotBlank(funTalkCourse)) {
                // 如果手动填写了欢拓的课程，那么关联一条记录
                message = talkFunService.manualRegisterCourse(periodId, funTalkCourse);
            } else {
                // 根据课时ID 于欢拓注册课程
                message = talkFunService.registerCourse(periodId);
            }
            if (!message.isSuccess()) {
                // 注册失败删除这条数据
                microCourseServiceClient.removeCoursePeriod(courseId, periodId);
                return message;
            }
            return message;
        } catch (Exception ex) {
            logger.error("Failed to append Micro Course's Period, course={}", courseId, ex);
            return MapMessage.errorMessage("添加课程失败!");
        }
    }

    // 编辑保存课时实体
    @RequestMapping(value = "saveperiod.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePeriod() {
        String periodId = getRequestString("period"); // 课时ID
        try {
            MicroCoursePeriod oldPeriod = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(periodId);
            if (oldPeriod == null) {
                return MapMessage.errorMessage("无效的课时信息");
            }
            MicroCoursePeriod newPeriod = requestEntity(MicroCoursePeriod.class);
            if (newPeriod == null) {
                return MapMessage.errorMessage("无效的参数");
            }
            if (!getRequestString("price").matches(MONEY_PATTERN)) {
                return MapMessage.errorMessage("【课时价格】格式不正确!");
            }
            String beforeBtn = getRequestString("before");  // 购买前按钮文字
            String afterBtn = getRequestString("after");    // 购买后按钮文字
            Map<String, String> btnContent = new HashMap<>();
            btnContent.put(MicroCoursePeriod.BTN_BEFORE_PURCHASE, beforeBtn);
            btnContent.put(MicroCoursePeriod.BTN_AFTER_PURCHASE, afterBtn);
            newPeriod.setBtnContent(btnContent);
            // 校验课时输入
            MapMessage validMsg = validatePeriod(newPeriod);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            boolean manually = getRequestBool("manual");

            // 查看字段是否有变更, 没有变更的话直接返回 success
            boolean changed = manually && BeanUtils.getInstance().beanEquals(oldPeriod, newPeriod, COMPARE_FIELDS);
            if (changed) {
                return MapMessage.successMessage();
            }
            // 保存前设置
            newPeriod.setDisabled(false);
            newPeriod.setId(oldPeriod.getId());
            // 同步之后再保存
            MapMessage message = microCourseServiceClient.savePeriod(newPeriod);
            if (!message.isSuccess()) {
                return message;
            }
            String funTalkCourse = getRequestString("talkfun");
            if (StringUtils.isNotBlank(funTalkCourse)) {
                // 如果手动填写了欢拓的课程，那么关联一条记录
                message = talkFunService.manualRegisterCourse(periodId, funTalkCourse);
            } else {
                // 在欢拓同步
                message = talkFunService.registerCourse(periodId);
            }
            if (!message.isSuccess()) {
                // 保存失败回退
                microCourseServiceClient.savePeriod(oldPeriod);
                return message;
            }
            return message;
        } catch (Exception ex) {
            logger.error("Failed to save Micro Course Period", ex);
            return MapMessage.errorMessage("保存实体失败");
        }
    }

    // 删除课时关联
    @RequestMapping(value = "removeperiod.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removePeriod() {
        String courseId = getRequestString("cid");
        String periodId = getRequestString("pid");
        try {
            MapMessage message = microCourseServiceClient.removeCoursePeriod(courseId, periodId);
            if (!message.isSuccess()) {
                return message;
            }
            // 根据课时ID 于欢拓删除课程
            // 不用关心到底有没有删除成功
            talkFunService.deleteCourse(periodId);
            return message;
        } catch (Exception ex) {
            logger.error("Failed remove Micro Course Period, course={},period={}", courseId, periodId, ex);
            return MapMessage.errorMessage("删除老师失败");
        }
    }

    // 进入课时直播
    @RequestMapping(value = "live.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage live() {
        String periodId = getRequestString("period");
        String testUserId = "test";
        String testUserName = "微微家长";
        String options = requestString("options", null);
        try {
            return talkFunService.accessCourse(periodId, testUserId, testUserName, TalkFunConstants.ROLE_USER, null);
        } catch (Exception ex) {
            return MapMessage.errorMessage("进入直播间失败：" + StringUtils.firstLine(ex.getMessage()));
        }
    }

    // 于欢拓后台添加 OR 更新课程
    @RequestMapping(value = "register-{type}.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage register(@PathVariable String type) {
        String id = getRequestString("id");
        try {
            if (StringUtils.equals("course", type)) {
                return talkFunService.registerCourse(id);
            }
            if (StringUtils.equals("teacher", type)) {
                String pwd = getRequestString("password");
                if (StringUtils.isBlank(pwd) || pwd.length() < 8) {
                    return MapMessage.errorMessage("无效的密码");
                }
                // 顺便修改这边老师的密码
                MapMessage message = mizarUserServiceClient.getRemoteReference()
                        .editMizarUserPassWord(id, pwd);
                if (!message.isSuccess()) {
                    return message;
                }
                message = talkFunService.registerTeacher(id, pwd);
                return MapMessage.successMessage(message.isSuccess() ? null : "欢拓：" + message.getInfo())
                        .add("pwd", pwd);
            }
            return MapMessage.errorMessage("无效的注册类型：" + type);
        } catch (Exception ex) {
            return MapMessage.errorMessage("进入直播间失败：" + StringUtils.firstLine(ex.getMessage()));
        }
    }

    // 老师管理列表页
    @RequestMapping(value = "teachers.vpage", method = RequestMethod.GET)
    public String teacherList(Model model) {
        List<MizarUser> teacherList = mizarUserLoaderClient.loadSameDepartmentUser(currentUserId(), MizarUserRoleType.MicroTeacher)
                .stream()
                .filter(MizarUser::isValid)
                .collect(Collectors.toList());

        model.addAttribute("teachers", teacherList);
        return "course/teachers";
    }

    // 新增/编辑老师
    @RequestMapping(value = "saveteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeacher() {
        String code1 = requestString("code1", null); // 第一遍输入密码
        String code2 = requestString("code2", null); // 第二遍输入密码
        try {
            // 获取当前用户是运营角色的部门
            List<MizarDepartment> userDepartments = mizarUserLoaderClient.loadUserDepartments(currentUserId(), MizarUserRoleType.Operator);
            if (CollectionUtils.isEmpty(userDepartments)) {
                return MapMessage.errorMessage("当前用户配置异常");
            }
            MizarUser user = requestEntity(MizarUser.class);
            boolean isNew = StringUtils.isBlank(user.getId()); // 是否是新增模式
            // 新增的时候校验密码
            if (isNew) {
                if (StringUtils.isBlank(code1) || code1.length() < 8) {
                    return MapMessage.errorMessage("请输入至少8位的密码");
                }
                if (!StringUtils.equals(code1, code2)) {
                    return MapMessage.errorMessage("两次密码输入不一致");
                }
                user.setPassword(code1);
            }
            // 校验用户信息输入
            MapMessage validMsg = validateUser(user);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            if (mizarUserLoaderClient.checkAccountAndMobile(user.getAccountName(), "", user.getId())) {
                return MapMessage.errorMessage("重复的账号名");
            }
            MapMessage message;
            if (isNew) {
                message = mizarUserServiceClient.addMizarUser(user);
            } else {
                message = mizarUserServiceClient.editMizarUser(user);
            }
            if (!message.isSuccess()) {
                return message;
            }
            String userId = isNew ? SafeConverter.toString(message.get("newId")) : user.getId();

            List<Integer> roles = Collections.singletonList(MizarUserRoleType.MicroTeacher.getId());
            // 将用户归到相同部门之下
            for (MizarDepartment department : userDepartments) {
                mizarUserServiceClient.addDepartmentUser(department.getId(), userId, roles);
            }
            // 将账号同步到欢拓后台 2017-01-10
            message = talkFunService.registerTeacher(userId, code1);
            return MapMessage.successMessage(message.isSuccess() ? null : "欢拓：" + message.getInfo());
        } catch (Exception ex) {
            logger.error("Failed save Micro Teacher, currentUser={}", currentUserId(), ex);
            return MapMessage.errorMessage("添加老师失败：" + ex.getMessage());
        }
    }

    // 重置密码
    @RequestMapping(value = "resetpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPassword() {
        String userId = getRequestString("teacher");
        if (!getCurrentUser().isOperator()) {
            return MapMessage.errorMessage("您没有重置密码的权限");
        }
        String code1 = requestString("code1", null); // 第一遍输入密码
        String code2 = requestString("code2", null); // 第二遍输入密码
        if (StringUtils.isBlank(code1) || code1.length() < 8) {
            return MapMessage.errorMessage("请输入至少8位的密码");
        }
        if (!StringUtils.equals(code1, code2)) {
            return MapMessage.errorMessage("两次密码输入不一致");
        }
        try {
            MapMessage message = mizarUserServiceClient.getRemoteReference()
                    .editMizarUserPassWord(userId, code1);
            if (!message.isSuccess()) {
                return message;
            }
            // 将账号同步到欢拓后台 2017-01-10
            message = talkFunService.registerTeacher(userId, code1);
            return MapMessage.successMessage(message.isSuccess() ? null : "欢拓：" + message.getInfo())
                    .add("pwd", code1);
        } catch (Exception ex) {
            logger.error("Failed to delete Micro Teacher Account, teacher={}", userId, ex);
            return MapMessage.errorMessage("老师账号密码更新失败：" + ex.getMessage());
        }
    }

    // 删除老师实际上就是关闭账号
    @RequestMapping(value = "deleteteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTeacher() {
        String userId = getRequestString("teacher");
        try {
            // 关闭账号之前先检查一下是够有关联的主讲课程，如果有提示先去取消课程关联吧
            if (microCourseLoaderClient.isLecturerBindCourse(userId)) {
                return MapMessage.errorMessage("该老师尚有分配的主讲课程，请先取消关联再做删除");
            }
            return mizarUserServiceClient.closeAccount(userId);
        } catch (Exception ex) {
            logger.error("Failed to delete Micro Teacher Account, teacher={}", userId, ex);
            return MapMessage.errorMessage("老师账号删除失败：" + ex.getMessage());
        }
    }

    // 微课堂展示列表
    @RequestMapping(value = "itemlist.vpage", method = RequestMethod.GET)
    public String itemList(Model model) {
        // 获取课程列表
        int page = Integer.max(1, getRequestInt("page", 1));
        String title = getRequestString("title");
        String category = requestString("category", MizarCourseCategory.MICRO_COURSE_OPENING.name());
        String status = getRequestString("status");
        model.addAttribute("pageIndex", page);
        model.addAttribute("title", title);
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        if (!validCategory.contains(category)) {
            return "course/itemlist";
        }
        Pageable pageable = new PageRequest(page - 1, PAGE_SIZE);
        Page<MizarCourse> coursePage = mizarLoaderClient.loadPageCourseByParams(pageable, title, status, category);
        model.addAttribute("itemList", mapCourseItem(coursePage.getContent()));
        model.addAttribute("totalPage", Integer.max(1, coursePage.getTotalPages()));
        return "course/itemlist";
    }

    // 微课堂列表编辑页面
    @RequestMapping(value = "itemdetail.vpage", method = RequestMethod.GET)
    public String itemDetail(Model model) {
        String itemId = getRequestString("itemId");
        model.addAttribute("categories", validCategory);
        if (StringUtils.isNotBlank(itemId)) {
            MizarCourse course = mizarLoaderClient.loadMizarCourseById(itemId);
            if (course != null) {
                model.addAttribute("period", microCourseLoaderClient.getCourseLoader().loadCoursePeriod(course.getTitle()));
                model.addAttribute("course", course);
                Map<Integer, List<MizarCourseTarget>> targetMap = mizarLoaderClient.loadCourseTargetsGroupByType(itemId);
                List<KeyValuePair<Integer, String>> targetTypes = MizarCourseTargetType.toKeyValuePairs();
                for (KeyValuePair<Integer, String> target : targetTypes) {
                    model.addAttribute("has_" + target.getKey(), targetMap.containsKey(target.getKey()));
                }
            }
        }
        return "course/itemdetail";
    }

    @RequestMapping(value = "choose.vpage", method = RequestMethod.GET)
    public String choosePeriod(Model model) {
        String theme = requestString("theme");      // 课时名称
        String courseId = requestString("course");      // 课程ID
        int pageNum = Integer.max(1, getRequestInt("page")); // 页码
        model.addAttribute("theme", theme);
        model.addAttribute("course", courseId);

        Pageable pageable = new PageRequest(pageNum - 1, PAGE_SIZE);

        Page<MicroCoursePeriod> coursePage = microCourseLoaderClient.findPeriodPage(courseId, theme, pageable);
        List<MicroCoursePeriod> courseList = coursePage.getContent();
        int totalPage = Integer.max(1, coursePage.getTotalPages());
        model.addAttribute("courseList", courseList);
        model.addAttribute("pageIndex", pageNum);
        model.addAttribute("totalPage", totalPage);
        return "course/choose";
    }

    // 添加编辑
    @RequestMapping(value = "saveitem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveItem() {
        try {
            MizarCourse course = requestEntity(MizarCourse.class);
            if (course == null) {
                return MapMessage.errorMessage("参数获取错误！");
            }
            if (!MizarCourseCategory.MICRO_COURSE_OPENING.name().equals(course.getCategory())
                    && !MizarCourseCategory.MICRO_COURSE_NORMAL.name().equals(course.getCategory())) {
                return MapMessage.errorMessage("无效的类型：[" + course.getCategory() + "]");
            }
            String periodId = course.getTitle();
            MicroCoursePeriod period = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(periodId);
            if (period == null || period.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的课时ID：[" + periodId + "]");
            }
            course.setDescription(period.getTheme());
            course.setStatus(MizarCourse.Status.valueOf(requestString("status", MizarCourse.Status.OFFLINE.name())));
            course.setRedirectUrl(MicroCourseMsgTemplate.linkUrl(null, periodId)); // 默认是APP投放
            course.setClazzLevels(requestStringList("levels"));
            // 检验是否有相同的课时
            List<MizarCourse> content = mizarLoaderClient
                    .loadPageCourseByParams(new PageRequest(1, 1), course.getTitle(), null, course.getCategory())
                    .getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                return MapMessage.errorMessage("重复的课时ID: " + periodId);
            }
            return mizarServiceClient.saveMizarCourse(course);
        } catch (Exception ex) {
            logger.error("Save Mizar course failed.", ex);
            return MapMessage.errorMessage("保存课程失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "deleteitem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteItem() {
        String itemId = getRequestString("itemId");
        try {
            MizarCourse course = mizarLoaderClient.loadMizarCourseById(itemId);
            if (course == null) {
                return MapMessage.errorMessage("无效的信息");
            }
            return mizarServiceClient.removeMizarCourse(itemId);
        } catch (Exception ex) {
            logger.error("Remove Mizar Course failed, courseId={}", itemId, ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    // 配置投放策略
    @RequestMapping(value = "itemconfig.vpage", method = RequestMethod.GET)
    public String itemConfig(Model model) {
        String itemId = getRequestString("itemId");
        MizarCourse course = mizarLoaderClient.loadMizarCourseById(itemId);
        if (course == null) {
            model.addAttribute("error", "无效的ID信息");
            return "course/itemconfig";
        }
        model.addAttribute("course", course);
        generateDetailTargets(itemId, model);
        return "course/itemconfig";
    }

    @RequestMapping(value = "saveregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetRegion() {
        String itemId = getRequestString("itemId");
        Integer type = getRequestInt("type");
        String regions = getRequestString("regionList");
        if (MizarCourseTargetType.of(type) != MizarCourseTargetType.TARGET_TYPE_REGION) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(regions)) {
            return MapMessage.errorMessage("选择地区不能为空！");
        }
        Map<Integer, List<MizarCourseTarget>> itemMap = mizarLoaderClient.loadCourseTargetsGroupByType(itemId);
        itemMap.remove(type);
        if (MapUtils.isNotEmpty(itemMap)) {
            return MapMessage.errorMessage("只能选择一种策略！");
        }
        try {
            List<String> regionList = Arrays.asList(regions.split(","));
            return mizarServiceClient.saveCourseTargets(itemId, type, regionList, false);
        } catch (Exception ex) {
            logger.error("Failed Save Mizar Course Regions, id={},type={}, ex={}", itemId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放地区失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "savetarget.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetIds() {
        String itemId = getRequestString("itemId");
        Integer type = getRequestInt("type");
        String targetIds = getRequestString("targetIds");
        Boolean append = getRequestBool("append");
        MizarCourseTargetType targetType = MizarCourseTargetType.of(type);
        if (targetType != MizarCourseTargetType.TARGET_TYPE_SCHOOL && targetType != MizarCourseTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(targetIds)) {
            return MapMessage.errorMessage("请输入有效的内容！");
        }
        Map<Integer, List<MizarCourseTarget>> itemMap = mizarLoaderClient.loadCourseTargetsGroupByType(itemId);
        itemMap.remove(type);
        if (MapUtils.isNotEmpty(itemMap)) {
            return MapMessage.errorMessage("只能选择一种策略！");
        }
        try {
            // 没有校验用户输入是否符合规范
            List<String> targetList = Arrays.stream(targetIds.split(DEFAULT_LINE_SEPARATOR))
                    .map(t -> t.replaceAll("\\s", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            return mizarServiceClient.saveCourseTargets(itemId, type, targetList, append);
        } catch (Exception ex) {
            logger.error("Failed Save Mizar Course Target, id={},type={},ex={}", itemId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放用户失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "cleartargets.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearTargets() {
        String itemId = getRequestString("courseId");
        Integer type = getRequestInt("type");
        MizarCourseTargetType targetType = MizarCourseTargetType.of(type);
        if (targetType != MizarCourseTargetType.TARGET_TYPE_SCHOOL
                && targetType != MizarCourseTargetType.TARGET_TYPE_REGION
                && targetType != MizarCourseTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            return mizarServiceClient.clearCourseTargets(itemId, type);
        } catch (Exception ex) {
            logger.error("Failed Clear Mizar Course Target, id={},type={},ex={}", itemId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("清空投放对象失败:" + ex.getMessage(), ex);
        }
    }

    private void generateDetailTargets(String courseId, Model model) {
        Map<Integer, List<MizarCourseTarget>> targetMap = mizarLoaderClient.loadCourseTargetsGroupByType(courseId);
        int type = 3;
        List<Integer> regions = new ArrayList<>();
        String targetSchool = null;
        if (targetMap.get(MizarCourseTargetType.TARGET_TYPE_REGION.getType()) != null) {
            type = MizarCourseTargetType.TARGET_TYPE_REGION.getType();
            regions = targetMap.get(type).stream().map(ad -> SafeConverter.toInt(ad.getTargetStr())).collect(Collectors.toList());
        }
        if (targetMap.get(MizarCourseTargetType.TARGET_TYPE_SCHOOL.getType()) != null) {
            type = MizarCourseTargetType.TARGET_TYPE_SCHOOL.getType();
            List<String> schools = targetMap.get(type).stream().map(MizarCourseTarget::getTargetStr).collect(Collectors.toList());
            targetSchool = StringUtils.join(PageableUtils.listToPage(schools, new PageRequest(0, 1000)).getContent(), DEFAULT_LINE_SEPARATOR);
            model.addAttribute("schoolSize", schools.size());
        }
        List<KeyValuePair<Integer, String>> targetTypes = MizarCourseTargetType.toKeyValuePairs();
        for (KeyValuePair<Integer, String> target : targetTypes) {
            model.addAttribute("has_" + target.getKey(), targetMap.containsKey(target.getKey()));
        }
        model.addAttribute("targetType", type);
        model.addAttribute("targetRegion", JsonUtils.toJson(buildRegionTree(regions)));
        model.addAttribute("targetSchool", targetSchool);
        model.addAttribute("courseId", courseId);
    }

    // 课时预约/购买 列表
    @RequestMapping(value = "periodorder.vpage", method = RequestMethod.GET)
    public String periodOrders(Model model) {
        String id = getRequestString("period");
        int pageNum = Integer.max(getRequestInt("pageNum"), 1);
        MicroCourse course = microCourseLoaderClient.getCourseLoader().loadMicroCourse(id);
        MicroCoursePeriod period = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(id);
        if (course == null && period == null) {
            model.addAttribute("resultList", Collections.emptyList());
            model.addAttribute("page", 1);
            model.addAttribute("totalPage", 1);
            // 无效的课时ID
            return "course/periodorder";
        }

        double price = course != null ? course.getPrice() : period.getPrice();
        String name = course != null ? course.getName() : period.getTheme();

        Pageable pageable = new PageRequest(pageNum - 1, PAGE_SIZE);
        List<List<String>> resultList;
        int totalPage;
        // 根据价格区分购买还是预约
        if (price == 0D) {
            // 预约
            Page<TrusteeReserveRecord> reserveRecordsPage = trusteeOrderServiceClient.loadTrusteeReservesPageByTargetId(id, pageable);
            resultList = mapReserveRecord(reserveRecordsPage.getContent(), true);
            totalPage = reserveRecordsPage.getTotalPages();
        } else {
            // 订单
            Page<TrusteeOrderRecord> orderRecordsPage = trusteeOrderServiceClient.loadTrusteeOrderPageByTargetId(id, pageable);
            resultList = mapOrderRecord(orderRecordsPage.getContent(), true);
            totalPage = orderRecordsPage.getTotalPages();
        }

        model.addAttribute("resultList", resultList);
        model.addAttribute("pageIndex", pageNum);
        model.addAttribute("totalPage", Integer.max(1, totalPage));
        model.addAttribute("name", name);
        model.addAttribute("id", id);
        return "course/periodorder";
    }

    @RequestMapping(value = "downloadinfo.vpage", method = RequestMethod.GET)
    public void downloadInfo(HttpServletResponse response) {
        String id = getRequestString("period");
        MicroCourse course = microCourseLoaderClient.getCourseLoader().loadMicroCourse(id);
        MicroCoursePeriod period = microCourseLoaderClient.getCourseLoader().loadCoursePeriod(id);
        if (course == null && period == null) {
            // 无效的课时ID
            return;
        }

        double price = course != null ? course.getPrice() : period.getPrice();
        String name = course != null ? course.getName() : period.getTheme();

        List<List<String>> dataList;
        // 根据价格区分购买还是预约
        if (price == 0D) {
            // 预约
            dataList = mapReserveRecord(trusteeOrderServiceClient.loadTrusteeReservesByTargetId(id), false);
        } else {
            // 订单
            dataList = mapOrderRecord(trusteeOrderServiceClient.loadTrusteeOrderByTargetId(id), false);
        }
        String[] titles = new String[]{
                "预约/支付时间", "学号", "学生姓名", "家长号", "称谓", "年级"
                , "学生手机号", "家长手机号", "状态", "用户备注", "来源", "外部流水号"
        };
        int[] widths = new int[]{
                6000, 4500, 4500, 4500, 4500, 4500
                , 4500, 4500, 4500, 4500, 4500, 4500
        };
        try {
            XSSFWorkbook xssfWorkbook = XssfUtils.convertToXSSFWorkbook(titles, widths, dataList);
            String filename = XssfUtils.generateFilename(name, DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download Mizar-Period excel exception!", e);
            }
        }
    }

    // 数据统计
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "statistic.vpage", method = RequestMethod.GET)
    public String courseStatistic(Model model) {
        String period = getRequestString("id"); // 课时ID
        Date start = getRequestDate("start", "yyyy-MM-dd HH:mm");
        Date end = getRequestDate("end", "yyyy-MM-dd HH:mm");
        int page = Integer.max(1, getRequestInt("page"));
        int totalPage = 1;
        boolean live = getRequestBool("live");
        model.addAttribute("id", period);
        model.addAttribute("start", getRequestString("start"));
        model.addAttribute("end", getRequestString("end"));
        model.addAttribute("page", page);
        model.addAttribute("live", live);
        model.addAttribute("totalPage", totalPage);
        if (StringUtils.isBlank(period)) {
            return "course/statistic";
        }
        Pageable pager = new PageRequest(page, 100);
        MapMessage report = talkFunService.report(period, start, end, pager, live);

        List<TK_CourseReport> dataList = new ArrayList<>();
        String error = null;
        if (report.isSuccess()) {
            dataList = (List<TK_CourseReport>) report.get("dataList");
            totalPage = BigDecimal.valueOf(SafeConverter.toLong(report.get("total"))).divide(BigDecimal.valueOf(100L), 0, BigDecimal.ROUND_CEILING).intValue();
        } else {
            error = report.getInfo();
        }
        model.addAttribute("dataList", dataList);
        model.addAttribute("error", error);
        model.addAttribute("totalPage", Integer.max(1, totalPage));
        return "course/statistic";
    }

    // 数据统计
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "exportpage.vpage", method = RequestMethod.GET)
    public void exportPage(HttpServletResponse response) {
        String period = getRequestString("id"); // 课时ID
        Date start = getRequestDate("start", "yyyy-MM-dd HH:mm");
        Date end = getRequestDate("end", "yyyy-MM-dd HH:mm");
        int page = Integer.max(1, getRequestInt("page"));
        boolean live = getRequestBool("live");
        if (StringUtils.isBlank(period)) {
            return;
        }
        Pageable pager = new PageRequest(page, 100);
        MapMessage report = talkFunService.report(period, start, end, pager, live);

        List<List<String>> dataList = new ArrayList<>();
        String error = null;
        if (report.isSuccess()) {
            dataList = mapCourseReport((List<TK_CourseReport>) report.get("dataList"));
        } else {
            error = report.getInfo();
        }
        String[] titles = new String[]{
                "用户ID", "进入房间的时间", "离开房间的时间", "ip地址", "用户地理位置", "终端类型"
                , "浏览器", "用户停留的时间(单位：秒)", "用户停留时间(时:分:秒)", "用户终端类型"
        };
        int[] widths = new int[]{
                6000, 6000, 6000, 6000, 6000, 6000
                , 6000, 6000, 6000, 6000,
        };
        try {
            int totalPage = BigDecimal.valueOf(SafeConverter.toLong(report.get("total"))).divide(BigDecimal.valueOf((long) page), 0, BigDecimal.ROUND_CEILING).intValue();
            String filename = XssfUtils.generateFilename(SafeConverter.toString(report.get("title")), StringUtils.formatMessage("第{}页/共{}页", page, Integer.max(1, totalPage)));
            XSSFWorkbook xssfWorkbook = XssfUtils.convertToXSSFWorkbook(titles, widths, dataList, error);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download Mizar-Period excel exception!", e);
            }
        }
    }

    private MapMessage validateCourse(MicroCourse course) {
        StringBuilder validInfo = new StringBuilder();
        // 校验必填项
        if (StringUtils.isBlank(course.getName())) {
            validInfo.append("【课程名称】不能为空").append("<br />");
        }
        if (StringUtils.isBlank(course.getCategory())) {
            validInfo.append("【课程分类】不能为空").append("<br />");
        }
        // 支持按课程购买时，价格以及按钮文字必填
        if (Boolean.TRUE.equals(course.getPayAll())) {
            if (SafeConverter.toDouble(course.getPrice()) < 0) {
                validInfo.append("请填写正确的【课程价格】").append("<br />");
            }
            if (!SafeConverter.toString(course.getPrice()).matches(MONEY_PATTERN)) {
                validInfo.append("【课程价格】格式不正确!").append("<br />");
            }
            if (StringUtils.isBlank(course.getBtnContent())) {
                validInfo.append("【按钮文字】不能为空").append("<br />");
            }
        }
        if (StringUtils.isBlank(validInfo.toString())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(validInfo.toString());
    }

    private MapMessage validatePeriod(MicroCoursePeriod period) {
        StringBuilder validInfo = new StringBuilder();
        // 校验必填项
        if (StringUtils.isBlank(period.getTheme())) {
            validInfo.append("【课时名称】不能为空!").append("<br />");
        } else {
            if (period.getTheme().contains("【") || period.getTheme().contains("】")) {
                validInfo.append("【课时名称】里请不要加中文方括号:(【、】)").append("<br />");
            }
        }

        if (StringUtils.isBlank(period.getInfo())) {
            validInfo.append("【课时介绍】不能为空!").append("<br />");
        }
        if (period.getStartTime() == null || period.getEndTime() == null) {
            validInfo.append("请填写【开始时间】以及【结束时间】");
        } else if (period.getStartTime().after(period.getEndTime())) {
            validInfo.append("【结束时间】不能早于【开始时间】");
        } else if (DateUtils.dayDiff(period.getEndTime(), period.getStartTime()) > 1) {
            validInfo.append("课时时间间隔过长!");
        }
        if (period.getPrice() < 0D) {
            validInfo.append("【课时价格】不能小于0!").append("<br />");
        }
        if (!SafeConverter.toString(period.getPrice()).matches(MONEY_PATTERN)) {
            validInfo.append("【课时价格】格式不正确!").append("<br />");
        }
        if (CollectionUtils.isEmpty(period.getPhoto()) || StringUtils.isBlank(period.getPhoto().get(0))) {
            validInfo.append("请上传【课时图片】").append("<br />");
        }
        if (period.emptyBtnContent()) {
            validInfo.append("【按钮文本】不能为空!").append("<br />");
        }
        if (period.getSmsNotify() == null) {
            validInfo.append("请选择【是否需要短信提醒】").append("<br />");
        }
        if (StringUtils.isBlank(validInfo.toString())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(validInfo.toString());
    }

    private MapMessage validateUser(MizarUser user) {
        StringBuilder validInfo = new StringBuilder();
        // 校验必填项
        if (StringUtils.isBlank(user.getAccountName())) {
            validInfo.append("【老师账号】不能为空!").append("<br />");
        } else if (!user.getAccountName().matches("^[0-9A-Za-z_@.]+$")) {
            // 账号不推荐用中文
            validInfo.append("账号只能输入字母、数字以及下划线以及@符号").append("<br />");
        }
        if (StringUtils.isBlank(user.getRealName())) {
            validInfo.append("【老师名称】不能为空!").append("<br />");
        }
        if (StringUtils.isBlank(user.getPortrait())) {
            validInfo.append("请上传【老师头像】!").append("<br />");
        }
        if (StringUtils.isBlank(validInfo.toString())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(validInfo.toString());
    }

    private List<Map<String, Object>> mapCoursePeriod(List<MicroCoursePeriod> periodList) {
        if (CollectionUtils.isEmpty(periodList)) {
            return Collections.emptyList();
        }
        List<String> pids = periodList.stream().map(MicroCoursePeriod::getId)
                .distinct().collect(Collectors.toList());
        Map<String, TalkFunCourse> tkMap = talkFunService.loadTalkFunCourses(pids);
        return periodList.stream().map(period -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", period.getId());
            info.put("theme", period.getTheme());
            info.put("startTime", period.getStartTime());
            info.put("endTime", period.getEndTime());
            info.put("price", period.getPrice());
            info.put("tk", tkMap.get(period.getId()));
            return info;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapTeacherInfo(List<MicroCourseSummary.TeacherInfo> teachers, List<MizarUser> users) {
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<String> selected;
        if (CollectionUtils.isEmpty(teachers)) {
            selected = new ArrayList<>();
        } else {
            selected = teachers.stream()
                    .map(MicroCourseSummary.TeacherInfo::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return users.stream().map(user -> {
            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getId());
            info.put("userName", user.getRealName());
            info.put("account", user.getAccountName());
            info.put("selected", selected.contains(user.getId()));
            return info;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapCourseItem(List<MizarCourse> content) {
        if (CollectionUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        List<String> periodIds = content.stream().map(MizarCourse::getTitle).distinct().collect(Collectors.toList());
        Map<String, MicroCoursePeriod> periodMap = microCourseLoaderClient.getCourseLoader().loadCoursePeriods(periodIds);
        return content.stream()
                .filter(c -> periodMap.containsKey(c.getTitle()))
                .map(course -> {
                    Map<String, Object> info = new HashMap<>();
                    MicroCoursePeriod period = periodMap.get(course.getTitle());
                    info.put("title", period.getTheme());
                    info.put("periodId", period.getId());
                    info.put("desc", course.getDescription());
                    info.put("status", course.getStatus());
//                    info.put("createTime", course.getCreateAt());
                    info.put("createTime", period.getStartTime());
                    info.put("category", course.getCategory());
                    info.put("id", course.getId());
                    info.put("priority", course.getPriority());
                    info.put("viewCnt", asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                            .MizarCourseReadCountManager_loadReadCount(course.getId())
                            .getUninterruptibly());
                    return info;
                }).collect(Collectors.toList());
    }

    // 学生ID还是有点多，分成100组吧
    private List<List<String>> mapReserveRecord(List<TrusteeReserveRecord> reserveRecords, boolean withPhone) {
        if (CollectionUtils.isEmpty(reserveRecords)) {
            return Collections.emptyList();
        }
        List<List<String>> records = new LinkedList<>();
        CollectionUtils.splitList(reserveRecords, 100)
                .forEach(reserves -> records.addAll($mapReserveRecord(reserves, withPhone)));
        return records;
    }

    private List<List<String>> $mapReserveRecord(List<TrusteeReserveRecord> reserveRecords, boolean withPhone) {
        if (CollectionUtils.isEmpty(reserveRecords)) {
            return Collections.emptyList();
        }
        // 准备基础信息
        Set<Long> studentIds = reserveRecords.stream().map(TrusteeReserveRecord::getStudentId).collect(Collectors.toSet());
        Map<Long, StudentDetail> detailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, List<StudentParentRef>> refMap = studentLoaderClient.loadStudentParentRefs(studentIds);
        Map<Long, UserAuthentication> studentAuthMap = new HashMap<>();
        Map<Long, UserAuthentication> parentAuthMap = new HashMap<>();

        if (withPhone) {
            Set<Long> parentIds = reserveRecords.stream().map(TrusteeReserveRecord::getParentId).collect(Collectors.toSet());
            studentAuthMap = userLoaderClient.loadUserAuthentications(studentIds);
            parentAuthMap = userLoaderClient.loadUserAuthentications(parentIds);
        }

        // 按顺序封装数据
        List<List<String>> dataList = new LinkedList<>();
        for (TrusteeReserveRecord reserveRecord : reserveRecords) {
            // 准备数据
            Long studentId = reserveRecord.getStudentId();
            Long parentId = reserveRecord.getParentId();
            StudentDetail detail = detailMap.get(studentId);
            UserAuthentication studentAuth = studentAuthMap.get(studentId);
            UserAuthentication parentAuth = parentAuthMap.get(parentId);
            List<StudentParentRef> refList = refMap.get(studentId);
            StudentParentRef ref = CollectionUtils.isEmpty(refList) ? null :
                    refList.stream().filter(r -> Objects.equals(r.getParentId(), parentId)).findAny().orElse(null);

            // 按顺序封装数据
            List<String> data = new ArrayList<>();
            data.add(DateUtils.dateToString(reserveRecord.getCreateDatetime()));  // 预约时间
            data.add(studentId.toString()); // 学生号
            data.add(detail == null ? "--" : detail.fetchRealname()); // 学生姓名
            data.add(parentId.toString()); //家长号
            data.add(ref == null ? "--" : ref.getCallName());// 称谓
            data.add(detail != null && detail.getClazz() != null ? detail.getClazz().formalizeClazzName() : "--"); // 年级
            // 学生手机号
            data.add("--");
            // 家长手机号
            data.add("--");
            data.add(reserveRecord.getStatus().name()); // 状态
            data.add(reserveRecord.getSignPics()); // 备注
            data.add(reserveRecord.getTrack()); // 来源
            data.add("--"); // 外部流水
            dataList.add(data);
        }
        return dataList;
    }

    // 学生ID还是有点多，分成100组吧
    private List<List<String>> mapOrderRecord(List<TrusteeOrderRecord> orderRecords, boolean withPhone) {
        if (CollectionUtils.isEmpty(orderRecords)) {
            return Collections.emptyList();
        }
        List<List<String>> records = new LinkedList<>();
        CollectionUtils.splitList(orderRecords, 100)
                .forEach(orders -> records.addAll($mapOrderRecord(orders, withPhone)));
        return records;
    }

    private List<List<String>> $mapOrderRecord(List<TrusteeOrderRecord> orderRecords, boolean withPhone) {
        if (CollectionUtils.isEmpty(orderRecords)) {
            return Collections.emptyList();
        }
        // 准备基础信息
        Set<Long> studentIds = orderRecords.stream().map(TrusteeOrderRecord::getStudentId).collect(Collectors.toSet());
        Map<Long, StudentDetail> detailMap = studentLoaderClient.loadStudentDetails(studentIds);
        Map<Long, List<StudentParentRef>> refMap = studentLoaderClient.loadStudentParentRefs(studentIds);
        Map<Long, UserAuthentication> studentAuthMap = new HashMap<>();
        Map<Long, UserAuthentication> parentAuthMap = new HashMap<>();

        if (withPhone) {
            Set<Long> parentIds = orderRecords.stream().map(TrusteeOrderRecord::getParentId).collect(Collectors.toSet());
            studentAuthMap = userLoaderClient.loadUserAuthentications(studentIds);
            parentAuthMap = userLoaderClient.loadUserAuthentications(parentIds);
        }

        List<List<String>> dataList = new LinkedList<>();
        for (TrusteeOrderRecord orderRecord : orderRecords) {
            // 准备数据
            Long studentId = orderRecord.getStudentId();
            Long parentId = orderRecord.getParentId();
            StudentDetail detail = detailMap.get(studentId);
            UserAuthentication studentAuth = studentAuthMap.get(studentId);
            UserAuthentication parentAuth = parentAuthMap.get(parentId);
            List<StudentParentRef> refList = refMap.get(studentId);
            StudentParentRef ref = CollectionUtils.isEmpty(refList) ? null :
                    refList.stream().filter(r -> Objects.equals(r.getParentId(), parentId)).findAny().orElse(null);

            // 按顺序封装数据
            List<String> data = new ArrayList<>();
            data.add(DateUtils.dateToString(orderRecord.getCreateTime()));  // 支付时间
            data.add(studentId.toString()); // 学生号
            data.add(detail == null ? "--" : detail.fetchRealname()); // 学生姓名
            data.add(parentId.toString()); //家长号
            data.add(ref == null ? "--" : ref.getCallName());// 称谓
            data.add(detail != null && detail.getClazz() != null ? detail.getClazz().formalizeClazzName() : "--"); // 年级
            // 学生手机号
            data.add("--");
            // 家长手机号
            data.add("--");
            data.add(orderRecord.getStatus().name()); // 状态
            data.add(orderRecord.getRemark()); // 备注
            data.add(orderRecord.getTrack()); // 来源
            data.add(orderRecord.getOutTradeNo()); // 外部流水
            dataList.add(data);
        }
        return dataList;
    }

    private List<List<String>> mapCourseReport(List<TK_CourseReport> reportList) {
        if (CollectionUtils.isEmpty(reportList)) {
            return Collections.emptyList();
        }
        // 准备基础信息
        List<List<String>> dataList = new LinkedList<>();
        for (TK_CourseReport report : reportList) {
            // 按顺序封装数据
            List<String> data = new ArrayList<>();
            data.add(report.getUserId()); // 用户ID
            data.add(report.getJoinTime()); // 进入房间的时间
            data.add(report.getLeaveTime()); // 离开房间的时间
            data.add(report.getIp()); // ip地址
            data.add(report.getLocation()); // 用户地理位置
            data.add(report.getOs()); // 终端类型
            data.add(report.getUserAgent()); // 浏览器
            data.add(SafeConverter.toString(report.getDuration(), "0")); // 用户停留的时间(单位：秒)
            data.add(report.getDurationTime()); // 用户停留时间(时:分:秒)
            data.add(report.fetchTerminal()); // 用户终端类型
            dataList.add(data);
        }
        return dataList;
    }

}
