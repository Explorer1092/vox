package com.voxlearning.utopia.schedule.schedule.platform;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.TeacherSummaryEsService;
import com.voxlearning.utopia.service.user.api.entities.SyahUserMobile;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSummaryEsServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "手动修复老师ES数据任务",
        jobDescription = "手动修复老师ES数据任务",
        cronExpression = "0 */5 * * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
@Slf4j
public class AutoUpdateTeacherSummaryEsinfoJob extends ScheduledJobWithJournalSupport {

    @Inject TeacherSummaryEsServiceClient teacherSummaryEsServiceClient;
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
        // 获取需要处理的region codes
        List<Integer> targetRegions = getTargetRegions(parameters);
        if (CollectionUtils.isEmpty(targetRegions)) {
            log.error("parameter regions is required.");
            return;
        }

        log.info("Update teacher summary esinfo with regions {}", JsonUtils.toJson(targetRegions));

        for (Integer region : targetRegions) {
            log.info("Update teacher summary esinfo in region {}", region);

            String querySql = "SELECT DISTINCT t2.USER_ID FROM VOX_SCHOOL t1, VOX_USER_SCHOOL_REF t2 WHERE t1.ID = t2.SCHOOL_ID and t1.DISABLED = FALSE and t2.DISABLED = FALSE and t1.REGION_CODE = ?";
            List<Long> userIds = utopiaSql.withSql(querySql).useParamsArgs(region).queryColumnValues(Long.class);
            for (Long uid : userIds) {

                teacherSummaryEsServiceClient.getTeacherSummaryEsService().upsert(uid);

                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    // 获取开始时间
    private List<Integer> getTargetRegions(Map<String, Object> parameters) {
        String regions = SafeConverter.toString(parameters.get("regions"));
        if (StringUtils.isBlank(regions)) {
            return Collections.emptyList();
        }

        return StringUtils.toIntegerList(regions);
    }

    public static void main(String[] args) {
        System.out.println(StringUtils.toIntegerList("1,2,3,4"));
    }

}
