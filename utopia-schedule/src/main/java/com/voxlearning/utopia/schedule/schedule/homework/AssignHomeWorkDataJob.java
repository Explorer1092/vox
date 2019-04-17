package com.voxlearning.utopia.schedule.schedule.homework;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.client.PlainEmailCreator;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/9/3
 * \* Time: 下午4:38
 * \* Description:今日截至当前时刻，布置作业数量
 * \
 */
@Named
@ScheduledJobDefinition(
        jobName = "今日截至当前时刻，布置作业数量统计",
        jobDescription = "今日截至当前时刻，布置作业数量",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 13,17 * * ?"
)
@ProgressTotalWork(100)
public class AssignHomeWorkDataJob extends ScheduledJobWithJournalSupport {

    @Inject
    private NewHomeworkLoader newHomeworkLoader;
    @Inject
    private EmailServiceClient emailServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Date todayBegin = DayRange.newInstance(new Date().getTime()).getStartDate();
        Date todayEnd = new Date();
        Date yesBegin = DateUtils.nextDay(todayBegin, -1);
        Date yesEnd = DateUtils.nextDay(todayEnd, -1);
        Date weekBegin = DateUtils.nextDay(todayBegin, -7);
        Date weekEnd = DateUtils.nextDay(todayEnd, -7);
        Map<String, Long> queryResult = newHomeworkLoader.getAssignHomeWorkCount(todayBegin, todayEnd, yesBegin, yesEnd, weekBegin, weekEnd);
        if (MapUtils.isEmpty(queryResult)) {
            return;
        }
        //send email
        PlainEmailCreator plainEmailCreator = emailServiceClient.createPlainEmail();
        plainEmailCreator.subject("定时任务：今日截至[" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_TIME) + "]当前时刻，布置作业数量");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("统计情况如下：\n\n");
        for (Map.Entry<String, Long> entry : queryResult.entrySet()) {
            stringBuilder.append("|").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" |\n");
            stringBuilder.append("\n\n");
        }
        plainEmailCreator.body(stringBuilder.toString());
        plainEmailCreator.to("xuhong.liu@17zuoye.com;" +
                "xiaohai.zhang@17zuoye.com;weiyi.shen@17zuoye.com;" +
                "xuesong.zhang@17zuoye.com;yizhou.zhang@17zuoye.com;zhilong.hu@17zuoye.com;" +
                "guohong.tan@17zuoye.com;huichao.liu.a@17zuoye.com;" +
                "guoqiang.li@17zuoye.com;dun.xiao@17zuoye.com;andy.liu@17zuoye.com;");
        plainEmailCreator.send();
    }
}
