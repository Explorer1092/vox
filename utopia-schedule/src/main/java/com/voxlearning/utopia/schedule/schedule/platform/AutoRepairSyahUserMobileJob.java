package com.voxlearning.utopia.schedule.schedule.platform;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SyahUserMobile;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "自动修复SyahUserMobile映射任务",
        jobDescription = "自动修复SyahUserMobile映射任务",
        cronExpression = "0 */5 * * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
@Slf4j
public class AutoRepairSyahUserMobileJob extends ScheduledJobWithJournalSupport {


    @Inject UserLoaderClient userLoaderClient;
    @Inject UserServiceClient userServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;


    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        // 获取开始时间
        Date startTime = getStartTime(parameters);
        if (startTime == null) {
            log.error("parameter start time is required.");
            return;
        }

        log.info("Repair syah user mobile data from {}", DateUtils.dateToString(startTime));

        int tableSize = getTableSize();

        for (int i = 0; i < tableSize; i++) {
            log.info("checking user data in table SYAH_USER_{}", i);

            String querySql = "SELECT ID from SYAH_USER_" + i + " WHERE USER_TYPE in (1,2,3) and CREATETIME >= ?";
            List<Long> userIds = utopiaSql.withSql(querySql).useParamsArgs(startTime).queryColumnValues(Long.class);
            for (Long uid : userIds) {
                String userMobile = sensitiveUserDataServiceClient.loadUserMobile(uid);
                if (StringUtils.isNoneBlank(userMobile)) {
                    SyahUserMobile syahUserMobile = userLoaderClient.loadSyahUserMobile(userMobile);
                    if (syahUserMobile == null || !syahUserMobile.getUserIds().contains(uid)) {
                        userServiceClient.updateEmailMobile(uid, null, userMobile);
                    }
                }
            }
        }
    }

    // 获取开始时间
    private Date getStartTime(Map<String, Object> parameters) {
        String startTimeStr = SafeConverter.toString(parameters.get("startTime"));
        if (StringUtils.isBlank(startTimeStr)) {
            return null;
        }

        return DateUtils.stringToDate(startTimeStr, DateUtils.FORMAT_SQL_DATETIME);
    }

    // 获取查询表的范围，暂时线上数据只到10
    private int getTableSize() {
        return RuntimeMode.isProduction() || RuntimeMode.isStaging() ? 11 : 2;
    }

}
