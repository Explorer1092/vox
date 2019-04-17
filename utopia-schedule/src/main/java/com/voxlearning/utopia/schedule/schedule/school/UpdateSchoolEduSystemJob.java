package com.voxlearning.utopia.schedule.schedule.school;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmSchoolExtInfoCheckServiceClient;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Task #55198 【学校学制清洗】
 * Created by yuechen.wang
 * <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=35323927"></a>
 */
@Named
@ScheduledJobDefinition(
        jobName = "学校学制清洗任务",
        jobDescription = "批量修改学校学制",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 10 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class UpdateSchoolEduSystemJob extends ScheduledJobWithJournalSupport {

    @Inject private CrmSchoolExtInfoCheckServiceClient crmSchoolExtInfoCheckServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        logger.info("开始清洗学校学制信息..............");
        List<SchoolInfo> schoolInfoList = new LinkedList<>();

        List<SchoolInfo> part1SchoolList = parseSchoolInfo("/schooledusystem/part1");
        logger.info("Part1 学校数据量：" + part1SchoolList.size() + "..........");
        schoolInfoList.addAll(part1SchoolList);

        List<SchoolInfo> part2SchoolList = parseSchoolInfo("/schooledusystem/part2");
        logger.info("Part2 学校数据量：" + part2SchoolList.size() + "..........");
        schoolInfoList.addAll(part2SchoolList);

        List<SchoolInfo> part3SchoolList = parseSchoolInfo("/schooledusystem/part3");
        logger.info("Part3 学校数据量：" + part3SchoolList.size() + "..........");
        schoolInfoList.addAll(part3SchoolList);

        progressMonitor.worked(5);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, schoolInfoList.size());
        for (SchoolInfo schoolInfo : schoolInfoList) {
            try {
                // 可以staging 环境看看效果
                if (RuntimeMode.isProduction()) {
                    crmSchoolExtInfoCheckServiceClient.updateSchoolExtInfoEduSystem(
                            schoolInfo.getSchoolId(),
                            schoolInfo.getEduSystem(),
                            "自动清洗任务更新",
                            "SYSTEM"
                    );
                }
                Thread.sleep(10);
            } catch (Exception ex) {
                logger.error("Failed batch update school edu system, please do it later. schoolId={}", schoolInfo.getSchoolId(), ex);
            } finally {
                monitor.worked(1);
            }
        }

        logger.info("学校学制和班级学制修改完毕..............");
        progressMonitor.done();
    }

    @SneakyThrows(IOException.class)
    private List<SchoolInfo> parseSchoolInfo(String file) {
        List<SchoolInfo> result = new LinkedList<>();
        if (StringUtils.isBlank(file)) {
            return result;
        }
        InputStream resource = null;
        BufferedReader reader = null;
        try {
            resource = this.getClass().getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(resource, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                SchoolInfo schoolInfo = new SchoolInfo(line);
                if (schoolInfo.introspect()) {
                    result.add(schoolInfo);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (resource != null) {
                resource.close();
            }
        }
        return result;
    }

    @Getter
    private class SchoolInfo {
        private Long schoolId;
        private SchoolLevel schoolLevel;
        private EduSystemType eduSystem;

        SchoolInfo(String line) {
            if (StringUtils.isNotBlank(line)) {
                String[] info = line.split(",");
                if (info.length == 3) {
                    schoolId = SafeConverter.toLong(info[0]);
                    schoolLevel = SchoolLevel.safeParse(SafeConverter.toInt(info[1]));
                    eduSystem = EduSystemType.of(info[2]);
                } else {
                    logger.warn("UpdateSchoolEduSystemJob has one line invalid, please check it. line={}", line);
                }
            }
        }

        private boolean introspect() {
            return schoolId > 0L && schoolLevel != null && eduSystem != null;
        }

        public String toString() {
            return StringUtils.formatMessage("[schoolId={}, schoolLevel={}, eduSystem={}]", schoolId, schoolLevel, eduSystem);
        }
    }

}
