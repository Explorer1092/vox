package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/12/22
 */
@Named
@ScheduledJobDefinition(
        jobName = "发送成长世界报告",
        jobDescription = "每周一11：00",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 11 ? * MON",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoSendParentGrowthReportJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        //拼url
        long mondayTime = WeekRange.current().previous().getStartDate().getTime();
        String url = "/view/mobile/parent/study_report/detail.vpage?is_week_report=true" + "&week_monday=" + mondayTime;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            url = "https://www.test.17zuoye.net" + url;
        } else if (RuntimeMode.isStaging()) {
            url = "https://www.staging.17zuoye.net" + url;
        } else {
            url = "https://www.17zuoye.com" + url;
        }
        //push消息
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("url", url);
        extInfo.put("ext_tab_message_type", ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getType());
        extInfo.put("studentId", "");
        extInfo.put("tag", ParentMessageTag.通知.name());
        extInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
        extInfo.put("shareContent", "");
        extInfo.put("shareUrl", "");
        extInfo.put("s", ParentAppPushType.NOTICE.name());
        Integer durationTime = 30;
        List<String> tags = new ArrayList<>();
        tags.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
        tags.add(JpushUserTag.PRIMARY_SCHOOL.tag);
        String pushContent = "孩子上周的成长世界自学报告生成啦，请查收！";
        //系统消息
        AppGlobalMessage appGlobalMessage = new AppGlobalMessage();
        appGlobalMessage.setMessageSource(AppMessageSource.PARENT.name()); //消息来源
        appGlobalMessage.setMessageType(ParentMessageType.REMINDER.getType()); //消息类型
        appGlobalMessage.setTitle("成长世界周报");//消息title
        String startDate = DateUtils.dateToString(WeekRange.current().previous().getStartDate(), "MM.dd");
        String endDate = DateUtils.dateToString(WeekRange.current().previous().getEndDate(), "MM.dd");
        String globalMessageText = "孩子的成长世界自学周报（" + startDate + "-" + endDate + "）生成啦，请查收！";
        appGlobalMessage.setContent(globalMessageText);//消息概要
        Map<String, Object> messageExtInfo = new HashMap<>();
        messageExtInfo.put("studentId", "");
        messageExtInfo.put("senderName", "");
        messageExtInfo.put("tag", ParentMessageTag.通知.name());
        messageExtInfo.put("type", ParentMessageType.REMINDER.name());
        messageExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
        messageExtInfo.put("shareContent", "");
        messageExtInfo.put("shareUrl", "");
        appGlobalMessage.setExtInfo(messageExtInfo);//扩展信息
        appGlobalMessage.setLinkUrl(url);
        if (RuntimeMode.le(Mode.TEST)) {
            appGlobalMessage.setImageUrl("img-File-20180102-5a4b0742777487b1040aea02.png");
        } else {
            appGlobalMessage.setImageUrl("img-File-20180102-5a4b05a88555ab57518d1ea6.png");
        }
        appGlobalMessage.setLinkType(0);//绝对地址
        appGlobalMessage.setIsTop(true); //是否置顶
        appGlobalMessage.setTopEndTime(0L);
        appGlobalMessage.setNoneBlackTag(JpushUserTag.NON_ANY_BLACK_LIST.tag);
        appGlobalMessage.setKtwelve(JpushUserTag.PRIMARY_SCHOOL.tag);
        messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(appGlobalMessage);
        appMessageServiceClient.sendAppJpushMessageByTags(pushContent, AppMessageSource.PARENT, tags, null, extInfo, durationTime);
    }
}
