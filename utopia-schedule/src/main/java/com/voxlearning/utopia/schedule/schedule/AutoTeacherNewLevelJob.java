package com.voxlearning.utopia.schedule.schedule;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLevelServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

import static com.mongodb.client.model.Filters.lte;

@Named
@ScheduledJobDefinition(
        jobName = "重新计算老师等级",
        jobDescription = "重新计算老师等级, 每天0点执行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        cronExpression = "0 0 0 1/1 * ? "
)
public class AutoTeacherNewLevelJob extends ScheduledJobWithJournalSupport {

    @Inject private TeacherLevelServiceClient teacherLevelServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo-plat")
                .getDatabase("vox-user");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("vox_teacher_ext_attribute");

        // 查询过期exp 重新计算等级 过期时间 180 天
        Date date = DateUtils.addDays(new Date(), -180);
        FindIterable<Document> teacherExtAttributes = mongoCollection.find(lte("initNewExpTime", date.getTime()));
        for (Document document : teacherExtAttributes) {
            Long id = document.getLong("_id");
            if (id != null) {
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(id);
                if (!teacherDetail.isPrimarySchool()) {
                    continue;
                }
            }

            int level = document.getInteger("newLevel", 1);
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            int exp = document.getInteger("exp", 0);
            Integer minExp = levelEnum.getMinExp();
            int requireLevel = level;
            // 普通(1)和初级(2)不降级 TeacherExtAttribute.NewLevel
            if (exp < minExp && level > 2) {
                requireLevel = level - 1;
            }
            Long teacherId = SafeConverter.toLong(document.get("_id"));
            teacherLevelServiceClient.updateNewLevel(teacherId, requireLevel, false);
//            if (mapMessage.isSuccess()) {
//                TeacherExtAttribute.NewLevel newLevel = TeacherExtAttribute.NewLevel.getNewLevelByLevel(requireLevel);
//                // 系统消息
//                String content = String.format("恭喜您！%s 保级成功，再接再厉哦~", newLevel.getValue());
//                if (isDown) {
//                    content = String.format("很遗憾！您的等级变更为%s，快快做任务赚积分升等级吧！", newLevel.getValue());
//                }
//                // pc
//                messageCommandServiceClient.getMessageCommandService().sendUserMessage(teacherId, content);
//                // app
//                AppMessage appMessage = new AppMessage();
//                appMessage.setUserId(teacherId);
//                appMessage.setMessageType(TeacherMessageType.ACTIVIY.getType());
//                appMessage.setTitle("等级变更通知");
//                appMessage.setContent(content);
//                appMessage.setIsTop(false);
//                appMessage.setLinkType(1);
//                appMessage.setLinkUrl("/view/mobile/teacher/activity2018/primary/task_system/index"); // 任务中心
//                appMessage.setTopEndTime(0L);
//                messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
//            }
        }
    }
}
