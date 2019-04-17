package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.MizarCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AutoMizarSettlementJob
 *
 * @author song.wang
 * @date 2017/6/27
 */
@Named
@ScheduledJobDefinition(
        jobName = "Mizar机构收入结算",
        jobDescription = "Mizar机构收入结算（每月1号执行）",
        disabled = {Mode.STAGING},  //
        cronExpression = "0 0 11 1 * ?")
@ProgressTotalWork(100)
public class AutoMizarSettlementJob extends ScheduledJobWithJournalSupport {

    @Inject
    private MizarCommandQueueProducer mizarCommandQueueProducer;


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        /*
           job参数：
               date : 日期 20170628  不传的情况下默认为前一天
               schoolIds ： 要生成结算数据的学校ID列表， 可不传， 不传的情况下默认为All, 会生成机构下所有学校的结算数据
         */
        Date runningDate = DateUtils.addDays(new Date(), -1);
        if (parameters.containsKey("date")) {
            runningDate = DateUtils.stringToDate(String.valueOf(parameters.get("date")), DateUtils.FORMAT_SQL_DATE);
            if (runningDate == null) {
                logger.error("invalid running date format: {}", parameters.get("date"));
                return;
            }
        }

        // 运行日期
        Integer runningDay = SafeConverter.toInt(DateUtils.dateToString(runningDate, "yyyyMMdd"));

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "user_settlement");
        command.put("date", runningDay);
        command.put("schoolIds", parameters.get("schoolIds"));
        mizarCommandQueueProducer.getProducer().produce(Message.newMessage().writeObject(command));

        progressMonitor.done();
    }
}
