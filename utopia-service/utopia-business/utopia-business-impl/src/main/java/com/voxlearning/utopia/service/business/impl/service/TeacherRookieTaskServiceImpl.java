package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.service.business.api.TeacherRookieTaskService;
import com.voxlearning.utopia.service.business.constant.TeacherTaskConstant;
import com.voxlearning.utopia.service.business.impl.dao.TeacherRookieTaskDao;
import com.voxlearning.utopia.service.business.impl.loader.TeacherTaskLoaderImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.invitation.api.InviteRewardHistoryService;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.entity.task.TeacherRookieTask.CHECK_STUDENT_SIZE;
import static com.voxlearning.utopia.entity.task.TeacherRookieTask.RookieTaskTrigger.*;
import static com.voxlearning.utopia.service.business.constant.TeacherTaskConstant.ROOKIE_COMMENT_STUDENT_SIZE;

@Named
@Slf4j
@SuppressWarnings("all")
@ExposeService(interfaceClass = TeacherRookieTaskService.class)
public class TeacherRookieTaskServiceImpl implements TeacherRookieTaskService {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;
    @Inject
    private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject
    private TeacherRookieTaskDao teacherRookieTaskDao;
    @Inject
    private TeacherTaskLoaderImpl teacherTaskLoader;
    @Inject
    private BusinessCacheSystem businessCacheSystem;

    @ImportService(interfaceClass = InviteRewardHistoryService.class)
    private InviteRewardHistoryService inviteRewardHistoryService;

    private static final long TYPE_ID = 1L; // 2019年上学期的新手任务


    @Override
    public Boolean allowRookieTask(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        long createTime = teacherDetail.getCreateTime().getTime();

        if (createTime < TeacherTaskConstant.ROOKIE_TASK_SPLIT) {
            Boolean oldRookieFinished = oldRookieFinished(teacherDetail);
            if (oldRookieFinished) {
                return false;
            }
        }

        Integer authenticationState = teacherDetail.getAuthenticationState();
        return (!Objects.equals(AuthenticationState.SUCCESS.getState(), authenticationState))
                && teacherDetail.getKtwelve() == Ktwelve.PRIMARY_SCHOOL;
    }

    @Override
    public MapMessage receiveRookieTask(Long teacherId) {
        try {
            AtomicCallback<MapMessage> callback = () -> {
                TeacherRookieTask task = loadRookieTask(teacherId);
                if (task == null) {
                    Boolean allow = allowRookieTask(teacherId);
                    if (!allow) {
                        return MapMessage.errorMessage("不符合领取新手任务的条件").add("code", 1);
                    }
                    TeacherRookieTask newTask = beginTask(teacherId);
                    return MapMessage.successMessage().add("task", newTask);
                } else {
                    return MapMessage.errorMessage("不可重复领取").add("code", 2);
                }
            };

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("business:beginRookieTask")
                    .keys(teacherId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public TeacherRookieTask loadRookieTask(Long teacherId) {
        TeacherRookieTask task = teacherRookieTaskDao.load(teacherId, TYPE_ID);
        return task;
    }

    @Override
    public MapMessage loadHomePagePop(Long teacherId) {
        return MapMessage.errorMessage("接口已下线");
    }

    @Override
    public MapMessage loadCenterDetailPop(Long teacherId, String flag) {
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("alert", false);

        // 任务中心不能帮忙领,首页弹窗进详情页需要帮忙领
        boolean allowOffOn = false;
        if (Objects.equals(flag, "taskSystem")) {
            allowOffOn = allowRookieTask(teacherId);
        } else {
            MapMessage receiveRookieTaskMsg = receiveRookieTask(teacherId);
            boolean allow = receiveRookieTaskMsg.isSuccess();
            boolean allowEd = Objects.equals(MapUtils.getInteger(receiveRookieTaskMsg, "code"), 2); // 曾经允许过
            allowOffOn = allow || allowEd;
        }

        if (allowOffOn) {
            TeacherRookieTask teacherRookieTask = loadRookieTask(teacherId);
            if (teacherRookieTask == null) {
                teacherRookieTask = mockTeacherRookieTask(teacherId);
            }

            for (TeacherRookieTask.SubTask subTask : teacherRookieTask.getSubTask()) {
                if (subTask.fetchOnGoing()) {
                    subTask.setStatus(TeacherRookieTask.Status.NEXT.name());
                    mapMessage.put("nextTask", subTask);
                    break;
                }
            }
            mapMessage.put("task", teacherRookieTask);

            // 如果今天任务中心弹过, 不再弹
            boolean alert = getCenterPopCache(teacherId) == null;
            mapMessage.put("alert", alert);

            if (alert) setCenterPopCache(teacherId);

            // 如果完成了 也不弹
            if (teacherRookieTask.fetchFinished()) {
                mapMessage.put("alert", false);
            }
        }

        return mapMessage;
    }

    @Override
    public MapMessage updateProgress(Long teacherId, TeacherRookieTask.RookieTaskTrigger rookieTaskTrigger, Map<String, Object> exts) {
        TeacherRookieTask teacherRookieTask = loadRookieTask(teacherId);
        if ((teacherRookieTask == null) || (!teacherRookieTask.fetchOnGoing())) {
            return MapMessage.errorMessage("任务已过期");
        }

        TeacherRookieTask.SubTask subTaskProgress = teacherRookieTask.getSubTask().stream()
                .filter(i -> Objects.equals(i.getStatus(), TeacherRookieTask.Status.ONGOING.name()))
                .findFirst().orElse(null);

        assert subTaskProgress != null;

        if (rookieTaskTrigger == subTaskProgress.getTrigger()) {
            updateProgress(teacherRookieTask, subTaskProgress, exts);
        } else if (rookieTaskTrigger == SHARE_HOMEWORK) {
            teacherRookieTask.setShare(true);
            teacherRookieTaskDao.upsert(teacherRookieTask);
        } else {
            return MapMessage.errorMessage("还未到该子任务");
        }
        return MapMessage.successMessage();
    }

    public Boolean oldRookieFinished(TeacherDetail teacherDetail) {
        long tplId = 0L;
        if (Objects.equals(teacherDetail.getSubject(), Subject.MATH)) {
            tplId = 14L;
        } else if (Objects.equals(teacherDetail.getSubject(), Subject.ENGLISH)
                || Objects.equals(teacherDetail.getSubject(), Subject.CHINESE)) {
            tplId = 1L;
        }

        TeacherTask oldRookieTask = teacherTaskLoader.internalLoadTaskList(teacherDetail.getId(), tplId);
        return oldRookieTask != null && (
                Objects.equals(oldRookieTask.getStatus(), TeacherTask.Status.FINISHED.name())
                        || Objects.equals(oldRookieTask.getStatus(), TeacherTask.Status.ONGOING.name())
        );
    }

    @Override
    public Boolean oldRookieFinished(Long teacher) {
        TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacher);
        return oldRookieFinished(td);
    }

    @Override
    public Boolean rookieFinished(Long teacher) {
        TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacher);
        long tplId = 0L;
        if (Objects.equals(td.getSubject(), Subject.MATH)) {
            tplId = 14L;
        } else if (Objects.equals(td.getSubject(), Subject.ENGLISH)
                || Objects.equals(td.getSubject(), Subject.CHINESE)) {
            tplId = 1L;
        }

        TeacherTask oldRookieTask = teacherTaskLoader.internalLoadTaskList(td.getId(), tplId);
        if (oldRookieTask != null && Objects.equals(oldRookieTask.getStatus(), TeacherTask.Status.FINISHED.name())) {
            return true;
        }
        TeacherRookieTask newRookieTask = loadRookieTask(td.getId());
        return newRookieTask != null && Objects.equals(newRookieTask.getStatus(), TeacherTask.Status.FINISHED.name());
    }

    private void updateProgress(TeacherRookieTask task, TeacherRookieTask.SubTask subTaskProgress, Map<String, Object> exts) {
        String finishedStatus = TeacherRookieTask.Status.FINISHED.name();

        // 寻找当前要处理的子任务
        TeacherRookieTask.SubTask currSubTask = null;
        for (TeacherRookieTask.SubTask currSubTaskItem : task.getSubTask()) {
            if (Objects.equals(currSubTaskItem.getStatus(), TeacherRookieTask.Status.ONGOING.name())
                    && currSubTaskItem.getIndex().equals(subTaskProgress.getIndex())) {
                currSubTask = currSubTaskItem;
                break;
            }
        }

        if (currSubTask.getShowProgress()) {
            if (currSubTask.getIndex() == 3) {
                Set<Long> studentSet = commentAwardStudentSet(exts, currSubTask);
                currSubTask.setCurr(Math.min(studentSet.size(), currSubTask.getTarget()));
            } else {
                currSubTask.setCurr(currSubTask.getCurr() + 1);
            }
            if (Objects.equals(currSubTask.getCurr(), currSubTask.getTarget())) {
                currSubTask.setStatus(finishedStatus);
            }
        } else {
            // 第二个检查作业子任务的特殊逻辑,保存最多完成人数的作业情况
            if (exts != null && currSubTask.getIndex() == 2) {
                Long newSize = MapUtils.getLong(exts, CHECK_STUDENT_SIZE);
                Long oldSize = MapUtils.getLong(currSubTask.getExts(), CHECK_STUDENT_SIZE);
                if (newSize >= oldSize) {
                    currSubTask.setExts(exts);
                }
                if (newSize >= TeacherTaskConstant.ROOKIE_CHECK_STUDENT_SIZE) {
                    currSubTask.setStatus(finishedStatus);
                }
            } else {
                currSubTask.setStatus(finishedStatus);
            }
        }

        // 如果进度完成发奖励
        if (Objects.equals(currSubTask.getStatus(), TeacherRookieTask.Status.FINISHED.name())) {
            sendReward(task.getTeacherId(), currSubTask.getRewardNum(), currSubTask.getIndex());
            currSubTask.setSendReward(true);
        }

        // 如果现在是第三步并且刚完成,判断之前是否有分享行为,有的话把第四步完成
        if (currSubTask.getIndex() == 3 && currSubTask.fetchFinished() && SafeConverter.toBoolean(task.getShare(), false)) {
            TeacherRookieTask.SubTask lastTask = task.getSubTask().get(3);
            lastTask.setStatus(finishedStatus); // get(3) 就是第四个任务
            sendReward(task.getTeacherId(), lastTask.getRewardNum(), lastTask.getIndex());
            lastTask.setSendReward(true);
        }

        boolean allFinished = task.getSubTask().stream().allMatch(TeacherRookieTask.SubTask::fetchFinished);
        if (allFinished) {
            task.setStatus(finishedStatus);
            task.setFinishedDate(new Date());
            // 给老师发 push
            sendNotify(task.getTeacherId());

        }

        teacherRookieTaskDao.upsert(task);
    }

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    private void sendNotify(Long teacherId) {
        String receiePageUrl = "/view/mobile/teacher/activity2018/primary/task_system/novicetask";

        // 站内消息
        AppMessage msg = new AppMessage();
        msg.setUserId(teacherId);
        msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
        msg.setContent("恭喜您完成新手任务!");
        msg.setTitle("新手任务");
        msg.setLinkType(1);
        msg.setLinkUrl(receiePageUrl);
        msg.setCreateTime(new Date().getTime());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

        // 推送
        String messageContent = "恭喜您完成新手任务!";
        Map<String, Object> extInfo = MapUtils.m(
                "s", TeacherMessageType.ACTIVIY.getType(),
                "link", "https://www." + TopLevelDomain.getTopLevelDomain() + "/" + receiePageUrl,
                "t", "h5");

        appMessageServiceClient.sendAppJpushMessageByIds(
                messageContent, AppMessageSource.JUNIOR_TEACHER,
                Collections.singletonList(teacherId), extInfo
        );
    }

    @NotNull
    private Set<Long> commentAwardStudentSet(Map<String, Object> exts, TeacherRookieTask.SubTask itemSubTask) {
        Object studentList = itemSubTask.getExts().get("studentList");
        if (studentList == null) {
            studentList = new ArrayList<>();
        }
        List<Long> studentOldList = (List<Long>) studentList;

        List<Long> extStudentIds = (List<Long>) exts.get("studentIds");
        if (extStudentIds != null) {
            studentOldList.addAll(extStudentIds);
        }
        HashSet<Long> studentSet = new HashSet<>(studentOldList);
        itemSubTask.getExts().put("studentList", studentSet);
        return studentSet;
    }

    private void sendReward(Long teacherId, Integer integralNum, Integer index) {
        Integer sendIntegral = integralNum;
        TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacherId);
        // 小学学豆乘以10
        if (td.isPrimarySchool()) {
            sendIntegral = integralNum * 10;
        }
        IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.TEACHER_ROOKIE_TASK, sendIntegral);
        integralHistory.setComment("新手任务-第" + index + "阶段奖励!");
        MapMessage chgIntegralResult = userIntegralService.changeIntegral(integralHistory);
        if (!chgIntegralResult.isSuccess()) {
            log.error("新手任务奖励发放失败 tid:+" + teacherId + " msg:" + chgIntegralResult.getInfo());
        }

        // 如果有邀请者,还要给邀请者发同样的奖励
        if (DateUtils.dayDiff(new Date(), td.getCreateTime()) <= TeacherTaskConstant.INVI_REWARD_DAY_LIMIT) {
            sendInviteUserReward(teacherId, integralNum, index);
        }
    }

    private void sendInviteUserReward(Long teacherId, Integer integralNum, Integer index) {
        InviteHistory byInviteId2019 = asyncInvitationServiceClient.getAsyncInvitationService().queryByInviteId2019First(teacherId);
        if (byInviteId2019 != null) {
            TeacherDetail td = teacherLoaderClient.loadTeacherDetail(byInviteId2019.getUserId());

            // 没认证 不发
            if (!Objects.equals(td.getAuthenticationState(), AuthenticationState.SUCCESS.getState())) {
                return;
            }

            // 中学的话,除中英中数外也不发
            if (td.isJuniorTeacher()) {
                boolean online = CollectionUtils.isNotEmpty(td.getSubjects()) &&
                        (td.getSubjects().contains(Subject.ENGLISH) || td.getSubjects().contains(Subject.JENGLISH));
                if (!online) {
                    return;
                }
            }

            integralNum = integralNum * 10; // 小学 100 园丁豆 、中学 1000 学豆, 按学豆发, 所以都是传 1000

            inviteRewardHistoryService.incrReward(byInviteId2019.getUserId(), byInviteId2019.getInviteeUserId(), integralNum);

            IntegralHistory integralHistory = new IntegralHistory(td.getId(), IntegralType.TEACHER_DAY_TASK_INVITATION, integralNum);
            integralHistory.setComment("邀请其他老师完成新手任务第" + index + "阶段奖励!");
            MapMessage chgIntegralResult = userIntegralService.changeIntegral(integralHistory);
            if (!chgIntegralResult.isSuccess()) {
                log.error("邀请其他老师完成新手任务奖励发放失败 tid:+" + td.getId() + " msg:" + chgIntegralResult.getInfo());
            }
        }
    }

    private TeacherRookieTask beginTask(Long teacherId) {
        TeacherRookieTask teacherRookieTask = mockTeacherRookieTask(teacherId);

        return teacherRookieTaskDao.upsert(teacherRookieTask);
    }

    @NotNull
    private TeacherRookieTask mockTeacherRookieTask(Long teacherId) {
        Date now = new Date();

        TeacherRookieTask teacherRookieTask = new TeacherRookieTask();
        teacherRookieTask.setShare(false);
        teacherRookieTask.setTeacherId(teacherId);
        teacherRookieTask.setTypeId(TYPE_ID);
        teacherRookieTask.setStatus(TeacherRookieTask.Status.ONGOING.name());
        teacherRookieTask.setReceiveDate(now);

        List<TeacherRookieTask.SubTask> subTask = new ArrayList<>();
        teacherRookieTask.setSubTask(subTask);

        TeacherRookieTask.SubTask subTask1 = new TeacherRookieTask.SubTask(1, "首次布置作业");
        subTask1.setTrigger(ASSIGN_HOMEWORK);
        subTask1.setRewardNum(100);

        TeacherRookieTask.SubTask subTask2 = new TeacherRookieTask.SubTask(2, "检查作业且10名学生完成");
        subTask2.setTrigger(CHECK_HOMEWORK);
        subTask2.setRewardNum(100);
        subTask2.setExts(MapUtils.map(CHECK_STUDENT_SIZE, 0L));

        TeacherRookieTask.SubTask subTask3 = new TeacherRookieTask.SubTask(3, "点评或奖励5名学生");
        subTask3.setTrigger(COMMENT_STUDENT);
        subTask3.setShowProgress(true);
        subTask3.setTarget(ROOKIE_COMMENT_STUDENT_SIZE);
        subTask3.setCurr(0);
        subTask3.setRewardNum(200);

        TeacherRookieTask.SubTask subTask4 = new TeacherRookieTask.SubTask(4, "分享报告给家长");
        subTask4.setTrigger(SHARE_HOMEWORK);
        subTask4.setRewardNum(600);

        subTask.add(subTask1);
        subTask.add(subTask2);
        subTask.add(subTask3);
        subTask.add(subTask4);
        return teacherRookieTask;
    }

    private String genCenterPopCacheKey(Long teacherId) {
        return "BE_ROOKIE_TASK_CENTER_POP_" + teacherId;
    }

    private Date getCenterPopCache(Long teacherId) {
        String cacheKey = genCenterPopCacheKey(teacherId);
        CacheObject<Object> cacheObject = businessCacheSystem.CBS.storage.get(cacheKey);
        if (cacheObject.containsValue()) {
            return (Date) cacheObject.getValue();
        }
        return null;
    }

    private void setCenterPopCache(Long teacherId) {
        String cacheKey = genCenterPopCacheKey(teacherId);
        businessCacheSystem.CBS.storage.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), new Date());
    }
}
