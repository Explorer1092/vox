package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.NamedDaemonThreadFactory;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zhouwei on 2018/10/15
 **/
@Named
@ScheduledJobDefinition(
        jobName = "老师成长体系任务领取提醒",
        jobDescription = "有需要手动领取的任务时，老师属于任务对象但尚未领取的老师",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 13 ? * TUE"
)
public class TeacherTaskReceiveRemind extends ScheduledJobWithJournalSupport {

    private String dateString = "2018-09-01";

    @Inject
    private TeacherTaskLoaderClient teacherTaskLoaderClient;

    @Inject
    private TeacherTaskServiceClient teacherTaskServiceClient;

    @Inject
    private UserLoginServiceClient userLoginServiceClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    private Long currentTeacherId = 0L;
    private Long jobProgress = 0L;
    private Integer threadPoolSize = 10;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            threadPoolSize, threadPoolSize,
            10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(100),
            NamedDaemonThreadFactory.getInstance("TeacherTaskReceiveRemind-Pool"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private synchronized List<Long> pullTaskTeacherId() {
        List<Long> taskTeacherId = teacherTaskLoaderClient.getTaskTeacherId(currentTeacherId); // 一次拉1000

        if (CollectionUtils.isNotEmpty(taskTeacherId)) {
            currentTeacherId = taskTeacherId.get(taskTeacherId.size() - 1); // 把最大的那个设置到进度上
            printLog();
        } else {
            logger.info("老师成长体系任务领取提醒任务即将结束");
        }
        return taskTeacherId;
    }

    /**
     * 3万个老师打印一下进度
     */
    private void printLog() {
        ++jobProgress;
        if (jobProgress % 30 == 0) {
            logger.info("老师成长体系任务领取提醒任务进度：{} / 约6000", jobProgress);
        }
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Runnable runnable = () -> {
            try {
                Date validDate = DateUtils.stringToDate(dateString, DateUtils.FORMAT_SQL_DATE);
                Map<Long, TeacherTaskTpl> tplMap = teacherTaskLoaderClient.getTtLoader().loadTaskTplMap();
                while (true) {
                    List<Long> teacherIdByStatus = pullTaskTeacherId();
                    if (CollectionUtils.isEmpty(teacherIdByStatus)) {
                        break;
                    }
                    for (Long teacherId : teacherIdByStatus) {
                        try {
                            Date userLastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacherId);
                            if (userLastLoginTime == null || userLastLoginTime.getTime() < validDate.getTime()) {
                                continue;
                            }

                            boolean isSend = false;
                            List<TeacherTask> teacherTasks = teacherTaskLoaderClient.getTtLoader().loadAndInitTaskList(teacherId);
                            List<TeacherTask> teacherTaskInit = teacherTasks.stream().filter(t -> Objects.equals(t.getStatus(), TeacherTask.Status.INIT.name())).collect(Collectors.toList());
                            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                            /** 虽然老师有INIT的任务，但是如果很久没有登录，可以已经不具备领取的条件，需要判断 **/
                            for (TeacherTask teacherTask : teacherTaskInit) {
                                TeacherTaskTpl teacherTaskTpl = tplMap.get(teacherTask.getTplId());
                                boolean putOn = teacherTaskServiceClient.isPutOn(teacherDetail, teacherTaskTpl);
                                if (putOn) {
                                    isSend = true;
                                }
                            }

                            if (isSend) {
                                pushAndMessge(teacherDetail);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        };

        currentTeacherId = 0L;
        jobProgress = 0L;

        for (int i = 0; i < threadPoolSize; i++) {
            executor.submit(runnable);
        }
    }

    /**
     * PUSH与消息
     */
    public void pushAndMessge(TeacherDetail teacherDetail) {
        String msgContent = "您有未领取的任务，做任务赚积分赢园丁豆，快来领取吧！";
        String msgTitle = "领取任务提醒";

        //发送App消息
        AppMessage msg = new AppMessage();
        msg.setUserId(teacherDetail.getId());
        msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
        msg.setContent(msgContent);
        msg.setTitle(msgTitle);
        msg.setCreateTime(new Date().getTime());
        msg.setLinkType(1);
        if (teacherDetail.isPrimarySchool()) {
            msg.setLinkUrl("/view/mobile/teacher/activity2018/primary/task_system/index");
        } else {
            msg.setLinkUrl("/view/mobile/teacher/activity2018/junior/task_system/index");
        }
        messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

        // 发送push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("s", TeacherMessageType.ACTIVIY.name());
        jpushExtInfo.put("key", "j");
        jpushExtInfo.put("t", "h5");
        if (teacherDetail.isPrimarySchool()) {
            jpushExtInfo.put("url", "/view/mobile/teacher/activity2018/primary/task_system/index");
        } else {
            jpushExtInfo.put("url", "/view/mobile/teacher/activity2018/junior/task_system/index");
        }
        appMessageServiceClient.sendAppJpushMessageByIds(
                msgContent,
                AppMessageUtils.getMessageSource("17Teacher", teacherDetail),
                Collections.singletonList(teacherDetail.getId()),
                jpushExtInfo);
    }
}
