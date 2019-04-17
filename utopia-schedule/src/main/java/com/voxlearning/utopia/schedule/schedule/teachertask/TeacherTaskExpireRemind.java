package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhouwei on 2018/10/15
 **/
@Named
@ScheduledJobDefinition(
        jobName = "老师成长体系任务过期提醒",
        jobDescription = "手动领取任务后，老师属于任务对象但尚未领取的老师",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 7 * * ?"
)
public class TeacherTaskExpireRemind extends ScheduledJobWithJournalSupport {

    @Inject
    private TeacherTaskLoaderClient teacherTaskLoaderClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        try {
            //如果有需要领取的任务加入进来，并且分周期性任务，需要添加TPL_IDS
            List<Long> tplIds = Arrays.asList(TeacherTaskTpl.Tpl.PRIMARY_ROOKIE.getTplId(), TeacherTaskTpl.Tpl.PRIMARY_ROOKIE_ENGLISH_CHINESE.getTplId(), TeacherTaskTpl.Tpl.PRIMARY_THREE_HOMEWORK.getTplId());
            List<Long> teacherIdByStatus = teacherTaskLoaderClient.getTeacherIdByInfos(tplIds, TeacherTask.Status.ONGOING.name());
            Map<Long, TeacherTaskTpl> tplMap = teacherTaskLoaderClient.getTtLoader().loadTaskTplMap();
            Date now = new Date();
            for (Long teacherId : teacherIdByStatus) {
                try {
                    TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                    List<TeacherTask> teacherTasks = teacherTaskLoaderClient.getTtLoader().loadAndInitTaskList(teacherId);
                    List<TeacherTask> teacherTasksTplIds = teacherTasks.stream().filter(t -> tplIds.contains(t.getTplId())).collect(Collectors.toList());
                    Map<String, String> messageMap = new HashMap<>();
                    for (TeacherTask teacherTask : teacherTasksTplIds) {
                        if (!Objects.equals(teacherTask.getStatus(), TeacherTask.Status.ONGOING.name())) {//不是进行中的任务，过滤掉。loadAndInitTaskList，这个方法会初始化任务的状态已经正在进行中的任务状态。
                            continue;
                        }
                        if (teacherTask.getExpireDate() == null) {
                            continue;
                        }
                        Long lastTime = teacherTask.getExpireDate().getTime() - now.getTime();
                        if (lastTime <= 0) {
                            continue;
                        }
                        TeacherTaskTpl teacherTaskTpl = tplMap.get(teacherTask.getTplId());
                        if (lastTime <= 24 * 60 * 60 * 1000) {//还有一天过期
                            messageMap.put(teacherTaskTpl.getName(), "今天");
                        } else if (lastTime >= 2 * 24 * 60 * 60 * 1000 && lastTime <= 3 * 24 * 60 * 60 * 1000) {//还有三天内过期
                            messageMap.put(teacherTaskTpl.getName(), "3天后");
                        }
                    }
                    if (!messageMap.isEmpty()) {
                        pushAndMessge(teacherDetail, messageMap);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * PUSH与消息
     */
    public void pushAndMessge(TeacherDetail teacherDetail, Map<String, String> messageMap) {
        String msgTitle = "福利任务有效期提醒";
        String msgContent = "您领取的福利任务";

        for (Map.Entry<String, String> entry : messageMap.entrySet()) {
            msgContent = msgContent + entry.getKey() + "将于" + entry.getValue() + "过期，";
        }
        msgContent = msgContent + "请尽快完成领取福利哦~";

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