package com.voxlearning.utopia.schedule.schedule.studyplanning;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningLoader;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningService;
import com.voxlearning.galaxy.service.studyplanning.api.entity.UserDailyStudyPlanningConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroupStudentRef;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/12/06
 */
@Named
@ScheduledJobDefinition(
        jobName = "修复学习规划训练营相关错误数据",
        jobDescription = "每天03：00运行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class FixStudyPlanningDailyConfigJob extends ScheduledJobWithJournalSupport {
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @ImportService(interfaceClass = StudyPlanningLoader.class)
    private StudyPlanningLoader studyPlanningLoader;
    @ImportService(interfaceClass = StudyPlanningService.class)
    private StudyPlanningService studyPlanningService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String lessonId = "14010";
        PageRequest pageRequest = new PageRequest(0, 2000);
        int activeCount = 0;
        int needHandleCount = 0;
        Page<StudyGroupStudentRef> studyGroupStudentRefs = studyTogetherServiceClient.loadActivateRefsPage(lessonId, pageRequest);
        if (studyGroupStudentRefs != null && CollectionUtils.isNotEmpty(studyGroupStudentRefs.getContent())) {
            Set<Long> studentIds = studyGroupStudentRefs.getContent().stream()
                    .map(StudyGroupStudentRef::getStudentId)
                    .collect(Collectors.toSet());
            activeCount = studentIds.size();
            for (int i = 3; i <= 7; i++) {
                for (Long studentId : studentIds) {
                    String dailyId = studentId + "-2018120" + i;
                    UserDailyStudyPlanningConfig dailyConfig = studyPlanningLoader.loadDailyConfig(dailyId);
                    if (dailyConfig != null && MapUtils.isNotEmpty(dailyConfig.getItemIdMap())) {
                        needHandleCount++;
                        dailyConfig.getItemIdMap().remove("393973199-STUDY_TOGETHER-14010");
                        studyPlanningService.saveDailyConfig(dailyConfig);
                    }
                }
            }
        }
        logger.info("FixStudyPlanningDailyConfigJob activeCount=" + activeCount + ",needHandleCount=" + needHandleCount);
        progressMonitor.done();
    }
}
