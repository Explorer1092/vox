package com.voxlearning.utopia.schedule.schedule;


import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "学校重建索引",
        jobDescription = "手动调用执行",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.UNIT_TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 3 1 * ?"
)
public class SchoolRebuildJob extends ScheduledJobWithJournalSupport {

    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @AlpsQueueProducer(queue = "utopia.school.update.queue")
    private MessageProducer schoolInfoUpdatedPublisher;
    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        Long id = 0L;
        int esSize = 0;

        String ck = "school_es_20180920_1";

        String ckValue = CacheSystem.CBS.getCache("flushable").load(ck);
        if (StringUtils.isNoneBlank(ckValue)) {
            id = SafeConverter.toLong(ckValue);
        }

        while (true) {
            String sql =  String.format("select ID from VOX_SCHOOL where DISABLED = false and ID > %s order by ID ASC limit 1000", id);
            List<Map<String, Object>> results = utopiaSql.withSql(sql).queryAll();
            if (results.isEmpty()) {
                break;
            }

            for (Map map : results) {
                Map<String, Object> message = new LinkedHashMap<>();
                message.put("event", "school_updated");
                message.put("schoolId", map.get("ID"));
                Message msg = Message.newMessage().withStringBody(JsonUtils.toJson(message));
                schoolInfoUpdatedPublisher.produce(msg);
                id = SafeConverter.toLong(map.get("ID"));

                if (esSize % 10 == 0) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            esSize += results.size();
            logger.info("学校创建索引: {}, current school id:{}", results.size(), id);

            CacheSystem.CBS.getCache("flushable").set(ck, 72*3600, SafeConverter.toString(id));

        }

        logger.info("学校创建完成: {}", esSize);
    }
}
