package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 补发老师代收奖励的消息
 * Created by haitaian.gan on 2017/9/25.
 */
@Named
@ScheduledJobDefinition(
        jobName = "SupplyTeacherCollectReward",
        jobDescription = "补发老师代收奖励的消息",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 20 23 1 1 ?",
        ENABLED = false
)
public class SupplyTeacherCollectReward extends ScheduledJobWithJournalSupport {

    private UtopiaSql utopiaSqlReward;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.utopiaSqlReward = UtopiaSqlFactory.instance().getUtopiaSql("hs_reward");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        List<RewardLogistics> logisticList = utopiaSqlReward.withSql(
                "SELECT * FROM VOX_REWARD_LOGISTICS " +
                " WHERE CREATE_DATETIME >= '2017-11-01' " +
                " AND LOGISTIC_NO != '' " +
                " AND DELIVERED_TIME IS NOT NULL " +
                " AND TYPE = 'STUDENT'")
                .queryAll(BeanPropertyRowMapper.newInstance(RewardLogistics.class));

        for(RewardLogistics logistics : logisticList){
            if(logistics.getType() != RewardLogistics.Type.STUDENT)
                continue;

            Long receiverId = logistics.getReceiverId();
            Long logisticId = logistics.getId();
            TeacherDetail td = teacherLoaderClient.loadTeacherDetail(receiverId);

            String msgNoticePart = "恭喜您成为幸运老师，帮学生收取奖品~感谢您的辛勤付出，";

            String pcReceiveUrl = "https://www." + TopLevelDomain.getTopLevelDomain() + "/reward/getcollectreward.vpage?logisticId=" + logisticId.toString();
            String pcMsgContent = msgNoticePart + "<a style=\"color:#35a4fa;\" href=\""+ pcReceiveUrl +"\">点击可领取奖励！</a>";

            // 发pc端信息
            teacherLoaderClient.sendTeacherMessage(receiverId, pcMsgContent);

            String appMsgContent = "恭喜您成为幸运老师，帮学生收取奖品~感谢您的辛勤付出，点击可领取奖励！";

            // 发弹窗
            userPopupServiceClient.createPopup(receiverId)
                    .content(msgNoticePart)
                    .type(PopupType.DEFAULT_AD)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();

            // 发app消息
            AppMessage msg = new AppMessage();
            msg.setUserId(receiverId);
            msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
            msg.setContent(appMsgContent);
            msg.setTitle("帮学生收取奖品奖励");
            msg.setCreateTime(new Date().getTime());
            // 点击跳领取奖励页面
            msg.setLinkUrl("/view/mobile/teacher/collectreward?logisticId=" + logisticId.toString());
            msg.setLinkType(1);

            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(receiverId);
            if (mainTeacherId != null && mainTeacherId != 0) {
                msg.setUserId(mainTeacherId);
            }

            messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

            // 发push
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("teacherId", "");
            jpushExtInfo.put("url", "");
            jpushExtInfo.put("tag", TeacherMessageType.ACTIVIY.name());
            jpushExtInfo.put("shareContent", "");
            jpushExtInfo.put("shareUrl", "");

            String jpushContent = "恭喜您成为幸运老师，帮学生收取奖品~感谢您的辛勤付出。";

            appMessageServiceClient.sendAppJpushMessageByIds(
                    jpushContent,
                    AppMessageUtils.getMessageSource("17Teacher", td),
                    Collections.singletonList(logistics.getReceiverId()),
                    jpushExtInfo);
        }
    }

}
