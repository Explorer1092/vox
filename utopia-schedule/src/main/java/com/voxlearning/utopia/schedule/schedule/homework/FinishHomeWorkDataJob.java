package com.voxlearning.utopia.schedule.schedule.homework;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.client.PlainEmailCreator;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/9/3
 * \* Time: 下午5:38
 * \* Description:定时任务：今日截至当前时刻，完成作业数量
 * \
 */
@Named
@ScheduledJobDefinition(
        jobName = "定时任务：今日截至当前时刻，完成作业数量",
        jobDescription = "定时任务：今日截至当前时刻，完成作业数量",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 13,17 * * ?"
)
public class FinishHomeWorkDataJob extends ScheduledJobWithJournalSupport {

    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getUtopiaSql("homework");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        Date todayBegin = DayRange.newInstance(new Date().getTime()).getStartDate();
        Date todayEnd = new Date();
        Date yesBegin = DateUtils.nextDay(todayBegin, -1);
        Date yesEnd = DateUtils.nextDay(todayEnd, -1);
        Date weekBegin = DateUtils.nextDay(todayBegin, -7);
        Date weekEnd = DateUtils.nextDay(todayEnd, -7);
        List<Map<String, Object>> result = getHomeWorkAccomplishmentCount(todayBegin, todayEnd, yesBegin, yesEnd, weekBegin, weekEnd);
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        //send email
        PlainEmailCreator plainEmailCreator = emailServiceClient.createPlainEmail();
        plainEmailCreator.subject("定时任务：今日截至[" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_TIME) + "]当前时刻，完成作业数量");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("统计情况如下：\n\n");
        for (Map<String, Object> item : result) {
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                stringBuilder.append(" | ").append(entry.getValue());
            }
            stringBuilder.append("\n\n\n");
        }
        plainEmailCreator.body(stringBuilder.toString());
        plainEmailCreator.to("xuhong.liu@17zuoye.com;" +
                "xiaohai.zhang@17zuoye.com;weiyi.shen@17zuoye.com;" +
                "xuesong.zhang@17zuoye.com;yizhou.zhang@17zuoye.com;zhilong.hu@17zuoye.com;" +
                "guohong.tan@17zuoye.com;huichao.liu.a@17zuoye.com;" +
                "guoqiang.li@17zuoye.com;dun.xiao@17zuoye.com;andy.liu@17zuoye.com;");
        plainEmailCreator.send();
    }

    private List<Map<String, Object>> getHomeWorkAccomplishmentCount(Date todayBegin, Date todayEnd, Date yesBegin, Date yesEnd, Date weekBegin, Date weekEnd) {
        String sql = " SELECT '今天' AS '标识', COUNT(DISTINCT STUDENT_ID) AS '数量' " +
                " FROM VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT " +
                " WHERE ACCOMPLISH_TIME BETWEEN ? AND ? " +
                " UNION" +
                " SELECT '昨天' AS '时间' ,  COUNT(DISTINCT STUDENT_ID) AS '数量' " +
                " FROM VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT " +
                " WHERE ACCOMPLISH_TIME BETWEEN ? AND ?" +
                " UNION " +
                " SELECT  '上周' AS  '时间', COUNT(DISTINCT STUDENT_ID) AS '数量' " +
                " FROM VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT " +
                " WHERE ACCOMPLISH_TIME BETWEEN ? AND ? ";
        List<Map<String, Object>> resultList = new ArrayList<>();
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> resultList.addAll(utopiaSql.withSql(sql).useParamsArgs(todayBegin, todayEnd, yesBegin, yesEnd, weekBegin, weekEnd).queryAll()))
                .execute();
        return resultList;
    }

}
