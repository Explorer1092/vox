package com.voxlearning.utopia.schedule.schedule;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;

@Named
@ScheduledJobDefinition(
        jobName = "积分即将过期通知",
        jobDescription = "积分即将过期通知, 每天1点执行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        ENABLED = false,
        cronExpression = "0 0 1 1/1 * ? "
)
public class AutoTeacherExpNoticeJob extends ScheduledJobWithJournalSupport {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo-plat")
                .getDatabase("vox-user");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("vox_teacher_ext_attribute");

        Date now = new Date();
        // 过期时间
        Date expDate = DateUtils.addDays(now, -180);
        Date startTime = DateUtils.addDays(expDate, 0);
        Date endTime = DateUtils.addDays(expDate, 31);
        FindIterable<Document> teacherExtAttributes = mongoCollection.find(and(gte("initNewExpTime", startTime.getTime()), lte("initNewExpTime", endTime.getTime())));
        for (Document document : teacherExtAttributes) {
            long diffDay = ((document.getLong("initNewExpTime") - expDate.getTime()) / 86400000);
            String dateDesc;
            if (diffDay == 30) {
                dateDesc = "30天后";
            } else if (diffDay == 15) {
                dateDesc = "15天后";
            } else if (diffDay == 7) {
                dateDesc = "7天后";
            } else if (diffDay == 1) {
                dateDesc = "今天";
            } else {
                continue;
            }

            Long teacherId = document.getLong("_id");
            // 系统消息
            String content = String.format("您的等级将于 %s 到期，到期将重新评定等级，清零等级积分及特权，请及时关注。做任务赚积分升等级赢特权哦~", dateDesc);
            // pc
            messageCommandServiceClient.getMessageCommandService().sendUserMessage(teacherId, content);
            // app
            AppMessage appMessage = new AppMessage();
            appMessage.setUserId(teacherId);
            appMessage.setMessageType(TeacherMessageType.ACTIVIY.getType());
            appMessage.setTitle("等级有效期提醒");
            appMessage.setContent(content);
            appMessage.setLinkType(1);
            appMessage.setLinkUrl("/view/mobile/teacher/activity2018/primary/task_system/index"); // 任务中心
            appMessage.setIsTop(false);
            appMessage.setTopEndTime(0L);
            messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
        }
    }
}
