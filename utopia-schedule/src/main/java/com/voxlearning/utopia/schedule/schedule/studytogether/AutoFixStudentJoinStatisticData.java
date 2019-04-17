package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.CrmMonitorRecruitService;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudentJoinStatistics;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/8/2
 */
@Named
@ScheduledJobDefinition(
        jobName = "17学kol班长招募数据修复",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 0 2 * * ? "
)
@ProgressTotalWork(100)
public class AutoFixStudentJoinStatisticData extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = CrmMonitorRecruitService.class)
    private CrmMonitorRecruitService crmMonitorRecruitService;
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Pageable pageRequest = new PageRequest(0, 100);
        Page<StudentJoinStatistics> studentJoinStatistics = crmMonitorRecruitService.$queryAll(pageRequest);
//        handleGroupArea(studentJoinStatistics.getContent());
        handleDelData(studentJoinStatistics.getContent());
        progressMonitor.worked(10);
        while (studentJoinStatistics.hasNext()) {
            pageRequest = studentJoinStatistics.nextPageable();
            studentJoinStatistics = crmMonitorRecruitService.$queryAll(pageRequest);
//            handleGroupArea(studentJoinStatistics.getContent());
            handleDelData(studentJoinStatistics.getContent());
            progressMonitor.worked(10);
        }
        progressMonitor.done();
    }

    private void handleGroupArea(List<StudentJoinStatistics> studentJoinStatistics) {
        if (CollectionUtils.isEmpty(studentJoinStatistics)) {
            return;
        }
        for (StudentJoinStatistics statistics : studentJoinStatistics) {
            if (statistics == null) {
                continue;
            }
            String groupId = statistics.getGroupId();
            if (StringUtils.isBlank(groupId)) {
                continue;
            }
            if (StringUtils.isNotBlank(statistics.getGroupAreaId())) {
                continue;
            }
            Map<String, StudyGroup> groupMap = crmStudyTogetherService.$getStudyGroupByIds(Collections.singleton(groupId));
            if (MapUtils.isEmpty(groupMap) || groupMap.get(groupId) == null) {
                continue;
            }
            StudyGroup studyGroup = groupMap.get(groupId);
            if (StringUtils.isNotBlank(studyGroup.getGroupAreaId())) {
                statistics.setGroupAreaId(studyGroup.getGroupAreaId());
                crmMonitorRecruitService.updateGroupAreaId(statistics);
            }
        }

    }


    private void handleData(List<StudentJoinStatistics> studentJoinStatistics) {
        if (CollectionUtils.isEmpty(studentJoinStatistics)) {
            return;
        }
        for (StudentJoinStatistics statistics : studentJoinStatistics) {
            if (statistics == null) {
                continue;
            }
            String groupId = statistics.getGroupId();
            if (StringUtils.isBlank(groupId)) {
                continue;
            }
            if (StringUtils.isBlank(statistics.getGroupAreaId())) {
                continue;
            }
            Map<String, StudyGroup> groupMap = crmStudyTogetherService.$getStudyGroupByIds(Collections.singleton(groupId));
            if (MapUtils.isEmpty(groupMap) || groupMap.get(groupId) == null) {
                continue;
            }
            StudyGroup studyGroup = groupMap.get(groupId);
            if (StringUtils.isNotBlank(studyGroup.getGroupAreaId())) {
                String generateId = StudentJoinStatistics.generateId(studyGroup.getId(), statistics.getDayRange(), studyGroup.getGroupAreaId());
                StudentJoinStatistics statistics1 = crmMonitorRecruitService.loadStatistics(generateId);
                if (statistics1 != null) {
                    continue;
                }
                statistics.setId(generateId);
                crmMonitorRecruitService.incrStatisticData(statistics, statistics.getStudentJoinCount());
//                crmMonitorRecruitService.updateGroupAreaId(statistics);
            }
        }
    }

    private void handleDelData(List<StudentJoinStatistics> studentJoinStatistics) {
        if (CollectionUtils.isEmpty(studentJoinStatistics)) {
            return;
        }
        for (StudentJoinStatistics statistics : studentJoinStatistics) {
            if (statistics == null) {
                continue;
            }
            String groupId = statistics.getGroupId();
            if (StringUtils.isBlank(groupId)) {
                continue;
            }
            if (StringUtils.isBlank(statistics.getGroupAreaId())) {
                continue;
            }
            if (StringUtils.isNotBlank(statistics.getGroupAreaId())) {
                if (statistics.getId().contains(DayRange.current().toString() + "_")) {
                    if (!statistics.getId().contains("_" + statistics.getGroupAreaId() + "_")) {
                        crmMonitorRecruitService.removeStatistics(statistics.getId());
                    }
                } else {
                    if (statistics.getId().contains("_" + statistics.getGroupAreaId() + "_")) {
                        crmMonitorRecruitService.removeStatistics(statistics.getId());
                    }
                }

            }
        }
    }
}
