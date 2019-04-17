package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.entity.task.*;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.mapper.ActivateMapper;
import com.voxlearning.utopia.service.business.api.TeacherMonthTaskService;
import com.voxlearning.utopia.service.business.api.TeacherRookieTaskService;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskServiceClient;
import com.voxlearning.utopia.service.invitation.api.TeacherActivateService;
import com.voxlearning.utopia.service.invitation.entity.TeacherActivate;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLevelServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;
import static com.voxlearning.utopia.api.constant.ActivationType.TEACHER_ACTIVATE_TEACHER_LEVEL_ONE;

/**
 * 老师任务 Controller
 * Created by haitian.gan on 2018/7/31.
 */
@Named
@RequestMapping("/teacherMobile/teacherTask/")
public class MobileTeacherTaskController extends AbstractMobileTeacherController {

    @Inject private TeacherTaskLoaderClient ttLoader;
    @Inject private TeacherTaskServiceClient ttService;
    @Inject private BusinessTeacherServiceClient busTchSrvCli;
    @Inject private TeacherLevelServiceClient teacherLevelService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;

    @ImportService(interfaceClass = TeacherRookieTaskService.class)
    private TeacherRookieTaskService teacherRookieTaskService;

    @ImportService(interfaceClass = TeacherMonthTaskService.class)
    private TeacherMonthTaskService teacherMonthTaskService;

    @ImportService(interfaceClass = TeacherActivateService.class)
    private TeacherActivateService teacherActivateService;

    /**
     * 老师任务首页
     */
    @RequestMapping("/index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        TeacherDetail td = (TeacherDetail) user;

        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(td.getId());

        MapMessage resultMsg = ttLoader.loadTaskList(td.getId());
        long integral = Optional.ofNullable(td.getUserIntegral()).map(ui -> ui.getUsable()).orElse(0L);

        Integer exp = 0;
        Integer level = 1;
        if (teacherExtAttribute != null && teacherExtAttribute.getExp() != null) {
            exp = teacherExtAttribute.getExp();
        }
        if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
            level = teacherExtAttribute.getNewLevel();
        }

        resultMsg.add("integral", integral);
        resultMsg.add("lvl_exp", exp);
        resultMsg.add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null));
        resultMsg.add("cityName", td.getCityName());
        resultMsg.add("level", level);
        resultMsg.add("rookieFinished", teacherRookieTaskService.rookieFinished(td.getId()));
        return resultMsg;
    }

    /**
     * 老师子任务信息
     */
    @RequestMapping("/sub_task.vpage")
    @ResponseBody
    public MapMessage loadSubTask() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        Long taskId = getRequestLong("taskId");
        if(taskId <= 0)
            return MapMessage.errorMessage("任务ID为空!");

        return ttLoader.loadSubTask(teacher.getId(), taskId);
    }

    /**
     * 领取任务
     */
    @RequestMapping(value = "/receive_task.vpage", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public MapMessage receiveTask() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        Long teacherId = teacher.getId();
        Long taskId = getRequestLong("taskId");
        if(taskId <= 0) {
            return MapMessage.errorMessage("任务ID为空!");
        }

        List<TeacherTask> list = ttLoader.getTtLoader().loadAndInitTaskList(teacherId);
        Map<Long, TeacherTask> map = list.stream().collect(Collectors.toMap(TeacherTask::getId,t -> t));
        if (map.get(taskId) == null) {
            return MapMessage.successMessage();
        }
        if (map.get(taskId).getTplId() == TeacherTaskTpl.Tpl.PRIMARY_THREE_HOMEWORK.getTplId()) {//只有三次练习任务需要完成
            // 需要先完成新手的任务
            if(!ttLoader.hadFinishedRookieTask(teacherId)){
                return MapMessage.errorMessage("请先完成新手任务再来领取哦！");
            }
            return ttService.receiveTask(teacherId, taskId);
        } else {
            return ttService.receiveTask(teacherId, taskId);
        }
    }

    /**
     * 历史任务
     */
    @RequestMapping("/history.vpage")
    @ResponseBody
    public MapMessage history() {
        TeacherDetail td = currentTeacherDetail();
        if (td == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage mapMessage = ttLoader.loadHistoryTask(td.getId());
        mapMessage.add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null));
        mapMessage.add("cityName", td.getCityName());
        return mapMessage;
    }

    private static String NEW_ACTIVATE_ONLINE_TIME_STRING = RuntimeMode.isProduction() ? "2019-04-02" : "2019-03-25";
    private static long NEW_ACTIVATE_ONLINE_TIME;

    static {
        try {
            NEW_ACTIVATE_ONLINE_TIME = DateUtils.parseDate(NEW_ACTIVATE_ONLINE_TIME_STRING, "yyyy-MM-dd").getTime();
        } catch (Exception ignore) {
        }
    }

    /**
     * 唤醒列表
     */
    @RequestMapping("/wake_up_list.vpage")
    @ResponseBody
    public MapMessage loadWakeUpInfo() {
        TeacherDetail td = currentTeacherDetail();
        if (td == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        List<Map<String, Object>> wakeUpMappers = new ArrayList<>();
        List<ActivateInfoMapper> activatingTeacher = busTchSrvCli.getActivatingTeacher(td.getId());

        if (new Date().getTime() > NEW_ACTIVATE_ONLINE_TIME) {
            Set<Long> wakeIngTeacherSet = activatingTeacher.stream().map(ActivateInfoMapper::getUserId).collect(Collectors.toSet());

            // 能进待唤醒列表肯定都是认证的主老师,所以这里不考虑包班制等情况
            List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(td.getTeacherSchoolId());
            Map<Long, Teacher> teacherMap = teachers.stream().collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
            List<Long> teacherIds = teachers.stream()
                    .filter(source -> !source.getId().equals(td.getId()) && source.fetchCertificationState() == SUCCESS)
                    .map(LongIdEntity::getId)
                    .collect(Collectors.toList());
            List<TeacherActivate> teacherActivates = teacherActivateService.loadActivateInitOrIng(teacherIds, td.getId());

            for (TeacherActivate item : teacherActivates) {
                // 老版本的激活中这里不再展示
                if (wakeIngTeacherSet.contains(item.getActivateId())) continue;

                wakeUpMappers.add(MapUtils.m(
                        "userId", item.getActivateId(),
                        "name", teacherMap.get(item.getActivateId()).fetchRealname(),
                        "status", Objects.equals(item.getStatus(), TeacherActivate.Status.ING.getCode()) ? "WAKENING" : "NOT_AWAKENING",
                        "historyId", item.getId()
                ));
            }
        } else {
            // 正在唤醒中的老师
            wakeUpMappers = activatingTeacher
                    .stream()
                    .map(m -> MapUtils.m(
                            "userId", m.getUserId(),
                            "name", m.getUserName(),
                            "status", "WAKENING",
                            "historyId", m.getHistoryId()))
                    .collect(Collectors.toList());

            // 待唤醒中的老师，添到唤醒中的老师里面
            busTchSrvCli.getPotentialTeacher(td)
                    .stream()
                    .map(m -> MapUtils.m("userId", m.getUserId(), "name", m.getUserName(), "status", "NOT_AWAKENING"))
                    .forEach(wakeUpMappers::add);
        }
        MapMessage mapMessage = MapMessage.successMessage().add("data", wakeUpMappers);
        mapMessage.add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null));
        mapMessage.add("cityName", td.getCityName());
        return mapMessage;
    }

    /**
     * 唤醒老师
     * @return
     */
    @RequestMapping(value = "/wake_up.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage wakeUp() {
        TeacherDetail td = currentTeacherDetail();
        if (td == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        Long wakeUpTeacherId = getRequestLong("wakeUpId");

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("MobileTeacherTaskController:signIn")
                    .keys(td.getId())
                    .callback(() -> {
                        if (new Date().getTime() > NEW_ACTIVATE_ONLINE_TIME) {
                            TeacherActivate teacherActivate = teacherActivateService.loadActivateInitOrIng(Collections.singleton(wakeUpTeacherId))
                                    .stream()
                                    .filter(i -> Objects.equals(i.getStatus(), TeacherActivate.Status.INIT.getCode()))
                                    .filter(i -> Objects.equals(i.getActivateId(), wakeUpTeacherId))
                                    .findFirst().orElse(null);
                            if (teacherActivate == null) {
                                return MapMessage.errorMessage("未找到唤醒记录");
                            }
                            return teacherActivateService.activate(teacherActivate.getId(), td.getId());
                        } else {
                            ActivateMapper mapper = new ActivateMapper();
                            ActivateInfoMapper info = new ActivateInfoMapper();
                            info.setUserId(wakeUpTeacherId);
                            info.setType(TEACHER_ACTIVATE_TEACHER_LEVEL_ONE);
                            mapper.setUserList(Collections.singletonList(info));
                            return businessTeacherServiceClient.activateTeacher(td, mapper);
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }

            logger.error("TT:ERROR occurs when activating teacher {}, msg is: {}", wakeUpTeacherId, ex.getMessage(), ex);
            return MapMessage.errorMessage("发送邀请失败，请重新选择教师");
        }
    }

    /**
     * 取消激活老师
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        String historyId = getRequestString("historyId");
        try {
            if (new Date().getTime() > NEW_ACTIVATE_ONLINE_TIME) {
                return teacherActivateService.unBindActivateRef(SafeConverter.toLong(historyId));
            } else {
                return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                        .proxy()
                        .deleteTeacherActivateTeacherHistory(currentUserId(), historyId);
            }
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN DELETE ACTIVATING TEACHER HISTORY {}, THE ERROR MESSAGE IS: {}", historyId, ex.getMessage(), ex);
            return MapMessage.errorMessage("删除失败，请重新操作");
        }
    }

    /**
     * 老师签到
     * @return
     * @author zhouwei
     */
    @RequestMapping(value = "/sign_in.vpage", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public MapMessage signIn() {
        TeacherDetail td = currentTeacherDetail();
        if (td == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        try {
            MapMessage mapMessage = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("MobileTeacherTaskController:signIn")
                    .keys(td.getId())
                    .callback(() -> {
                        return businessTeacherServiceClient.signIn(td.getId());
                    })
                    .build()
                    .execute();
            if (mapMessage.isSuccess()) {
                Map<Long,TeacherTaskProgress> progressMap = ttLoader.getTtLoader().loadTaskProgressMap(td.getId());
                TeacherTaskProgress progress = progressMap.values().stream().filter(p -> Objects.equals(p.getTplId(), TeacherTaskTpl.Tpl.PRIMARY_USER_SIGN_IN.getTplId())).findFirst().orElse(null);
                if (progress != null) {

                    /** 获取今天签到领取了多少园丁都，开始 **/
                    Date now = new Date();
                    int integral = 1;
                    String nowString = DateUtils.dateToString(now, DateUtils.FORMAT_SQL_DATE);
                    for (TeacherTaskProgress.SubTaskProgress subTaskProgress : progress.getSubTaskProgresses()) {
                        String dateTmp = SafeConverter.toString(subTaskProgress.getVars().get("date"));
                        if (Objects.equals(nowString, dateTmp)) {
                            for (TeacherTaskProgress.Reward r : subTaskProgress.getRewards()) {
                                if (Objects.equals(r.getUnit(),TeacherTaskTpl.RewardUnit.integral.name())) {
                                    integral = r.getValue();
                                }
                            }
                        }
                    }
                    /** 获取今天签到领取了多少园丁都，结束 **/

                    MapMessage succMessage = MapMessage.successMessage();
                    succMessage.put("info", "签到成功，+"+integral+"园丁豆，明天继续哦！");
                    succMessage.put("integral", integral);
                    return succMessage;
                } else {
                    return MapMessage.errorMessage("签到失败，请重新签到！");
                }
            } else {
                return MapMessage.errorMessage("签到失败，请重新签到！");
            }
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }

            logger.error("TT:ERROR occurs when sign_in teacher {}, msg is: {}", td.getId(), ex.getMessage(), ex);
            return MapMessage.errorMessage("签到失败，请重新签到！");
        }
    }

    /**
     * 老师分享
     * @return
     * @author zhouwei
     */
    @RequestMapping(value = "/share_article.vpage", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public MapMessage shareArticle() {
        User user = currentUser();
        if (user == null) {
            return errorMessage("请登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        if (!user.isTeacher()) {
            return MapMessage.successMessage();
        }
        TeacherDetail td = currentTeacherDetail();
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("MobileTeacherTaskController:shareArticle")
                    .keys(td.getId())
                    .callback(() -> {
                        return businessTeacherServiceClient.shareArticle(td.getId());
                    })
                    .build()
                    .execute();
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }

            logger.error("TT:ERROR occurs when share_article teacher {}, msg is: {}", td.getId(), ex.getMessage(), ex);
            return MapMessage.errorMessage("老师分享提交失败！");
        }
    }

    // App 首页的弹屏
    @RequestMapping(value = "rookie_home_pop.vpage")
    @ResponseBody
    public MapMessage homePagePop() {
        User teacherDetail = currentUser();
        if (teacherDetail == null || (!teacherDetail.isTeacher())) {
            return MapMessage.errorMessage();
        }
        return teacherRookieTaskService.loadHomePagePop(teacherDetail.getId());
    }

    // 任务中心的弹屏
    @RequestMapping(value = "rookie_center_pop.vpage")
    @ResponseBody
    public MapMessage centerPagePop() {
        User teacherDetail = currentUser();
        if (teacherDetail == null || (!teacherDetail.isTeacher())) {
            return MapMessage.errorMessage();
        }
        String flag = getRequestString("flag");
        return teacherRookieTaskService.loadCenterDetailPop(teacherDetail.getId(), flag);
    }

    // 任务中心-领取新手任务
    @RequestMapping(value = "receive_rookie_task.vpage")
    @ResponseBody
    public MapMessage receiveRookieTask() {
        User teacherDetail = currentUser();
        if (teacherDetail == null || (!teacherDetail.isTeacher())) {
            return MapMessage.errorMessage();
        }
        return teacherRookieTaskService.receiveRookieTask(teacherDetail.getId());
    }

    // 任务中心-新手任务
    @RequestMapping(value = "rookie_task.vpage")
    @ResponseBody
    public MapMessage rookieTask() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return MapMessage.errorMessage();
        }
        MapMessage mapMessage = MapMessage.successMessage();

        TeacherDetail teacherDetail = (TeacherDetail) user;

        boolean homeShow = DateUtils.dayDiff(new Date(), teacherDetail.getCreateTime()) <= 15;

        TeacherRookieTask teacherRookieTask = teacherRookieTaskService.loadRookieTask(teacherDetail.getId());
        if (teacherRookieTask == null) {
            if (teacherRookieTaskService.allowRookieTask(teacherDetail.getId())) {
                mapMessage.put("setup", 0);
                mapMessage.put("homeShow", homeShow);
                return mapMessage;
            }
        } else if (teacherRookieTask.fetchOnGoing()) {
            TeacherRookieTask.SubTask subTask = teacherRookieTask.getSubTask().stream().filter(TeacherRookieTask.SubTask::fetchOnGoing).findFirst().orElse(null);
            mapMessage.put("setup", subTask.getIndex());
            mapMessage.put("homeShow", DateUtils.dayDiff(new Date(), teacherRookieTask.getReceiveDate()) <= 15);

            if (Objects.equals(subTask.getIndex(), 2)) {
                mapMessage.put("curr", 0);
                mapMessage.put("surplus", 20);

                /*String homeworkId = MapUtils.getString(subTask.getExts(), HOME_WORK_ID);
                Long checkStudentSize = MapUtils.getLong(subTask.getExts(), CHECK_STUDENT_SIZE, 0L);

                if (homeworkId == null) {
                    NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
                    if (newHomework != null) {
                        mapMessage.put("curr", checkStudentSize);
                        mapMessage.put("surplus", Math.max(20 - checkStudentSize, 0));
                    }
                }*/
            }
            return mapMessage;
        }
        return MapMessage.errorMessage();
    }

    // 新手任务作业详情页
    @RequestMapping(value = "rookie_task_homework.vpage")
    @ResponseBody
    public MapMessage rookieTaskHomework() {
        return MapMessage.errorMessage("接口已下线");
    }

    // 每月活跃任务
    @RequestMapping(value = "month_task.vpage")
    @ResponseBody
    public MapMessage monthTask() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        Boolean allow = teacherMonthTaskService.allowMonthTask(teacherDetail.getId());
        if (!allow) {
            return MapMessage.errorMessage("不允许领取");
        }
        TeacherMonthTask monthTask = teacherMonthTaskService.loadMonthTask(teacherDetail.getId());

        // 补充一下给前端展示看
        if (monthTask != null) {
            for (TeacherMonthTask.GroupDetail group : monthTask.getGroups()) {
                int st = 3 - group.getHomework().size();
                for (int i = 0; i < st; i++) {
                    TeacherMonthTask.Homework homework = new TeacherMonthTask.Homework("", 0);
                    group.getHomework().add(homework);
                }
            }
        }

        MapMessage mapMessage = MapMessage.successMessage();

        if (monthTask == null) {
            return mapMessage.add("status", "INIT");
        } else {
            return mapMessage.add("status", "ONGOING");
        }
    }

    // 领取每月活跃任务
    @RequestMapping(value = "receive_month_task.vpage")
    @ResponseBody
    public MapMessage receiveMonthTask() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        return teacherMonthTaskService.receiveMonthTask(teacherDetail.getId());
    }

    // 每月活跃任务详情
    @RequestMapping(value = "month_task_detail.vpage")
    @ResponseBody
    public MapMessage monthTaskDetail() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        return teacherMonthTaskService.loadMonthTaskMsg(teacherDetail.getId());
    }

    // banner 图
    @RequestMapping(value = "center_banner.vpage")
    @ResponseBody
    public MapMessage centerBanner() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        Teacher teacher = currentTeacher();

        list.add(MapUtils.map(
                "name", "邀请新老师",
                "type", 1,
                "img", "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/task_system/banner1_v2.png",
                "url", "/view/mobile/teacher/activity2018/invite_teacher/index?source=banner"
        ));

        if (System.currentTimeMillis() > 1552838400000L) { // 3月18周一开启
            list.add(MapUtils.map(
                    "name", "月活跃任务",
                    "type", 2,
                    "img", "https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/task_system/banner2_v2.png",
                    "url", "/view/mobile/teacher/activity2018/primary/task_system/weekthreetask?source=gofinish"
            ));
        }
        return MapMessage.successMessage().add("data", list);
    }

    @RequestMapping(value = "teacher_info.vpage")
    @ResponseBody
    public MapMessage teacherInfo() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        TeacherDetail td = (TeacherDetail) user;
        return MapMessage.successMessage()
                .add("teacherId", td.getId())
                .add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null))
                .add("cityName", td.getCityName());

    }

    @RequestMapping(value = "week_task_detail.vpage")
    @ResponseBody
    public MapMessage weekTaskDetail() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        TeacherDetail td = (TeacherDetail) user;
        return ttLoader.loadTeacherWeekTask(td.getId());
    }

    @RequestMapping(value = "incr_invite.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage incrInvite() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }
        ttLoader.incrInviteTeacherCount(user.getId());
        return MapMessage.successMessage();
    }
}
