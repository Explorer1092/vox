
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.ChipsActiveService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.entity.ChipsActiveServiceRecord;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语筛选一对一点评视频",
        jobDescription = "每天02:00执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 2 * * ?"
)
@ProgressTotalWork(100)
public class ChipsActiveServiceRemarkVideoJob extends ScheduledJobWithJournalSupport {

    @AlpsQueueProducer(queue = "utopia.chips.active.remark.video.message.queue")
    private MessageProducer remarkVideoMessageProducer;

    @ImportService(interfaceClass = ChipsActiveService.class)
    private ChipsActiveService chipsActiveService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        long start = System.currentTimeMillis();
        Date startDate = DayRange.current().previous().getStartDate();
        List<ChipsActiveServiceRecord> recordList = chipsActiveService.loadChipsActiveServiceRecord(ChipsActiveServiceType.SERVICE, startDate);
        long t2 = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(recordList)) {
            logger.info("ChipsActiveServiceRecord size : " + (recordList == null ? 0 : recordList.size()));
            return;
        }
        recordList.forEach(r -> {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", r.getUserId());
            message.put("unitId", r.getUnitId());
            remarkVideoMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        });
        logger.info("ChipsActiveServiceRemarkVideoJob cost: " + (System.currentTimeMillis() - start) + "; query cost: " + (t2 - start) + " ;size : " + recordList.size());
    }

}
