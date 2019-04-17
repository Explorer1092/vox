package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ScheduledJobDefinition(
        jobName = "趣味配置平台活动开始发送消息",
        jobDescription = "趣味配置平台活动开始发送,每5分钟执行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        cronExpression = "0 0/5 * * * ? *"
)
public class AutoActivitySendNotice extends ScheduledJobWithJournalSupport {
    private static String DF = "yyyy年MM月dd日";
    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Object params = parameters.get("id");

        // 为了手动补偿漏发消息的活动
        if (params != null) {
            String ids = SafeConverter.toString(params);
            String[] split = ids.split(",");
            for (String id : split) {
                ActivityConfig activityConfig = activityConfigServiceClient.loadById(id);
                sendGlobalMessage(activityConfig);
            }
        } else {
            List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadAgreeStartingNoNotice();
            if (CollectionUtils.isNotEmpty(activityConfigs)) {
                for (ActivityConfig activityConfig : activityConfigs) {
                    sendGlobalMessage(activityConfig);
                }
            }
        }
    }

    private void sendGlobalMessage(ActivityConfig activityConfig) {
        if (activityConfig == null || activityConfig.getType() == null) {
            return;
        }
        // 设置已通知状态
        activityConfigServiceClient.getActivityConfigService().editNoticeStatus(activityConfig.getId(), true);
        String content = DateUtils.dateToString(activityConfig.getStartTime(), DF) + (DateUtils.dayDiff(activityConfig.getEndTime(), activityConfig.getStartTime()) > 0 ? (" - " + DateUtils.dateToString(activityConfig.getEndTime(), DF)) : "");
        if (CollectionUtils.isNotEmpty(activityConfig.getClazzIds())) {
            // 班级的直接发送到人AppMessage，新加入班级的通过卡片进入
            Set<Long> studentIds = studentLoaderClient.loadClazzStudentIds(activityConfig.getClazzIds()).values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
            for (Long studentId : studentIds) {
                AppMessage message = new AppMessage();
                message.setUserId(studentId);
                message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
                message.setTitle(activityConfig.getTitle());
                message.setContent(content);
                message.setLinkUrl(activityConfig.getType().getUrl() + activityConfig.getId());
                message.setLinkType(1);
                message.setImageUrl(activityConfig.getType().getImage());
                messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            }
        } else {
            // 发AppGlobalMessage
            AppGlobalMessage appGlobalMessage = new AppGlobalMessage();
            appGlobalMessage.setMessageSource(AppMessageSource.STUDENT.name()); // 消息来源
            appGlobalMessage.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType()); // 消息类型
            appGlobalMessage.setTitle(activityConfig.getTitle()); // 设置标题
            appGlobalMessage.setContent(content);
            appGlobalMessage.setIsTop(true);
            appGlobalMessage.setTopEndTime(activityConfig.getEndTime().getTime()); // 设置置顶时间
            appGlobalMessage.setExpiredTime(activityConfig.getEndTime().getTime()); // 设置过期时间
            appGlobalMessage.setLinkUrl(activityConfig.getType().getUrl() + activityConfig.getId());
            appGlobalMessage.setLinkType(1);
            appGlobalMessage.setImageUrl(activityConfig.getType().getImage());
            //@see com/voxlearning/washington/controller/open/AbstractApiController.java:985
            if (!activityConfig.fetchSubjects().contains(Subject.UNKNOWN)) {
                List<String> tagList = new ArrayList<>();
                for (Subject subject : activityConfig.fetchSubjects()) {
                    tagList.add(JpushUserTag.SUBJECT.tag + subject.name());
                }
                appGlobalMessage.withSubjectConstraint(tagList);
            }
            if(CollectionUtils.isNotEmpty(activityConfig.getSchoolIds()) && CollectionUtils.isNotEmpty(activityConfig.getClazzLevels())) {
                appGlobalMessage.withSchoolConstraint(activityConfig.getSchoolIds().stream().map(m -> "school_" + m).collect(Collectors.toList()))
                        .withClazzLevelConstraint(activityConfig.getClazzLevels().stream().map(m -> "clazz_level_" + m).collect(Collectors.toList()));
                messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(appGlobalMessage);
            } else if (CollectionUtils.isNotEmpty(activityConfig.getAreaIds()) && CollectionUtils.isNotEmpty(activityConfig.getClazzLevels())) {
                appGlobalMessage.withSchoolConstraint(activityConfig.getAreaIds().stream().map(m -> "region_" + m).collect(Collectors.toList()))
                        .withClazzLevelConstraint(activityConfig.getClazzLevels().stream().map(m -> "clazz_level_" + m).collect(Collectors.toList()));
                messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(appGlobalMessage);
            }
        }
    }
}
