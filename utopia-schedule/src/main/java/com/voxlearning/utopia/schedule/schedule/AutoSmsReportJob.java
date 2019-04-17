package com.voxlearning.utopia.schedule.schedule;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BsonField;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;

/**
 * @author chongfeng.qi
 * @since 2018/06/25
 */
@Named
@ScheduledJobDefinition(
        jobName = "短信发送月度统计汇总",
        jobDescription = "月度统计汇总邮件,每月1号凌晨3点跑任务，跑上月的数据",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 1 * ?"
)

public class AutoSmsReportJob extends ScheduledJobWithJournalSupport {

    @Inject
    private EmailServiceClient emailServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Date preMonthDate = DateUtils.addMonths(new Date(), -1);
        Date startDate = DateUtils.getFirstDayOfMonth(preMonthDate);
        Date endDate = DateUtils.getLastDayOfMonth(preMonthDate);
        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo-crm")
                .getDatabase("vox-crm");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("user_sms_message");
        AggregateIterable<Document> aggregates = mongoCollection.aggregate(Arrays.asList(
                match(and(eq("status", "1"), gte("createTime", startDate), lte("createTime", endDate))),
                group("$smsChannel", sum("smsCount", "$smsCount"), sum("submitCount", 1))
        ));
        AggregateIterable<Document> aggregatesSuccess = mongoCollection.aggregate(Arrays.asList(
                match(and(eq("status", "1"), exists("receiveTime"), gte("createTime", startDate), lte("createTime", endDate))),
                group("$smsChannel", sum("successCount", 1))
        ));
        MongoCursor<Document> iterator = aggregates.iterator();
        StringBuffer calcThirdProductRes = new StringBuffer("<style>table,table tr th, table tr td { border:1px solid #000; padding:8px} table{text-align: center; border-collapse: collapse; padding:10px; width:100%}</style>");
        calcThirdProductRes.append("<h2>短信月度统计:").append(DateUtils.dateToString(startDate, "yyyy-MM-dd") + " ~ " + DateUtils.dateToString(endDate, "yyyy-MM-dd")).append("</h2>")
                .append("<table border='1' cellpadding='0' cellspacing='0'><tr><th> 通道 </th><th> 提交数 </th><th> 成功数 </th><th> 提交短信数 </th></tr>");
        Map<String, Document> aggregateMap = new HashMap<>();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            aggregateMap.put(document.getString("_id"), document);
        }
        MongoCursor<Document> iteratorSuccess = aggregatesSuccess.iterator();
        while (iteratorSuccess.hasNext()) {
            Document document = iteratorSuccess.next();
            if (aggregateMap.containsKey(document.getString("_id"))) {
                aggregateMap.get(document.getString("_id")).put("successCount", document.getInteger("successCount"));
            }
        }
        aggregateMap.forEach((k, v) -> {
            calcThirdProductRes
                    .append("<tr>")
                    .append("<td>").append(k).append("</td>")
                    .append("<td>").append(v.getInteger("submitCount")).append("</td>")
                    .append("<td>").append(v.getInteger("successCount") == null ? 0 : v.getInteger("successCount")).append("</td>")
                    .append("<td>").append(v.getInteger("smsCount")).append("</td>")
                    .append("</tr>");
        });
        calcThirdProductRes.append("</table>");
        String toEmail;
        if (!RuntimeMode.isProduction()) {
            toEmail = "chongfeng.qi@17zuoye.com";
        } else {
            toEmail = "zhilong.hu@17zuoye.com; cui.zhao@17zuoye.com; ziyu.yang@17zuoye.com";
        }
        Map<String, Object> content = new HashMap<>();
        content.put("info", calcThirdProductRes.toString());
        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                .to(toEmail)
                .cc("chongfeng.qi@17zuoye.com")
                .subject("短信月度统计:" + DateUtils.dateToString(startDate, "yyyy-MM-dd") + " ~ " +  DateUtils.dateToString(endDate, "yyyy-MM-dd"))
                .content(content).send();
    }
}
