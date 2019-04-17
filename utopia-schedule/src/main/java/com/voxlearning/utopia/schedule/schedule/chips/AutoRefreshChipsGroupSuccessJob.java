
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条拼团自动成行",
        jobDescription = "每一个小时执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? "
)
@ProgressTotalWork(100)
public class AutoRefreshChipsGroupSuccessJob extends ScheduledJobWithJournalSupport {

    @AlpsQueueProducer(queue = "utopia.chips.group.to.success.queue")
    private MessageProducer refreshGroupToSuccessProducer;

    @AlpsQueueProducer(queue = "utopia.chips.create.group.queue")
    private MessageProducer createGroupProducer;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_chipsenglish");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Date now = new Date();
        int time = Optional.ofNullable(parameters)
                .map(e -> SafeConverter.toInt(e.get("time")))
                .filter(e -> e > 0)
                .orElse(1 * 60);
        String date = DateUtils.dateToString(DateUtils.addMinutes(now, -time));
        String sql = "SELECT CODE FROM VOX_CHIPS_GROUP_SHOPPING WHERE CREATETIME <= '"+ date +"' AND NUMBER = 1";
        List<String> codeList = new ArrayList<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSql.withSql(sql).queryAll(((rs, rowNum) -> {
                    String code = rs.getString("CODE");
                    codeList.add(code);
                    return null;
                })))
                .execute();
        for(String code : codeList) {
            Map<String, Object> message = new HashMap<>();
            message.put("C", code);
            refreshGroupToSuccessProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        }

        try{
            Thread.sleep(2000);
            Map<String, Object> message = new HashMap<>();
            Long userId = Optional.ofNullable(parameters)
                    .map(e -> parameters.get("user"))
                    .map(SafeConverter::toLong)
                    .orElse(0L);
            message.put("U", userId);
            int number = Optional.ofNullable(parameters)
                    .map(e -> parameters.get("number"))
                    .map(SafeConverter::toInt)
                    .orElse(2);
            message.put("N", number);
            createGroupProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        } catch (Exception e) {

        }
    }
}
