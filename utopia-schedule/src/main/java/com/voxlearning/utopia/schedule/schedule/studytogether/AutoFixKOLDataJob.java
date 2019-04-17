package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.*;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.BigMonitor;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.MonitorRecruit;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.MonitorRecruitV2;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorStatusRecord;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.ClassInfoInter;
import com.voxlearning.utopia.service.parent.constant.MonitorLevelType;
import com.voxlearning.utopia.service.parent.constant.MonitorRecruitType;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/7/30
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
public class AutoFixKOLDataJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = CrmMonitorRecruitService.class)
    private CrmMonitorRecruitService crmMonitorRecruitService;

    @ImportService(interfaceClass = MonitorRecruitV2Service.class)
    private MonitorRecruitV2Service monitorRecruitV2Service;

    @ImportService(interfaceClass = MonitorRecruitService.class)
    private MonitorRecruitService monitorRecruitService;
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @ImportService(interfaceClass = StudyTogetherService.class)
    private StudyTogetherService studyTogetherService;

    @SuppressWarnings("unchecked")
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<String> parentIds = (List<String>) parameters.get("fix_parents");
        List<MonitorRecruit> monitorRecruits = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parentIds)) {
            for (String parentId : parentIds) {
                long id = SafeConverter.toLong(parentId);
                MonitorRecruit monitorRecruit = monitorRecruitService.loadMonitorRecruitByParentId(id);
                if (monitorRecruit == null) {
                    continue;
                }
                monitorRecruits.add(monitorRecruit);
            }
        } else {
            monitorRecruits = monitorRecruitService.fixDataForJob();
        }
        if (CollectionUtils.isEmpty(monitorRecruits)) {
            return;
        }
        progressMonitor.worked(10);
        ISimpleProgressMonitor progress = progressMonitor.subTask(90, monitorRecruits.size());
        for (MonitorRecruit monitorRecruit : monitorRecruits) {
            progress.worked(1);
            MonitorRecruitV2 recruitForJobByParentId = crmMonitorRecruitService.getRecruitForJobByParentId(monitorRecruit.getParentId());
            if (recruitForJobByParentId != null) {
                continue;
            }
            //待审核
            if (monitorRecruit.getStatus() == 0) {
                MonitorRecruitV2 monitorRecruitV2 = new MonitorRecruitV2();
                monitorRecruitV2.setBirthday(monitorRecruit.getBirthday());
                monitorRecruitV2.setInputWechatId(monitorRecruit.getInputWechatId());
                monitorRecruitV2.setProvince(monitorRecruit.getProvince());
                monitorRecruitV2.setCity(monitorRecruit.getCity());
                monitorRecruitV2.setProfession(monitorRecruit.getProfession());
                monitorRecruitV2.setTime(monitorRecruit.getTime());
                monitorRecruitV2.setEducation(monitorRecruit.getEducation());
                monitorRecruitV2.setAdvantage(monitorRecruit.getAdvantage());
                monitorRecruitV2.setCharacter(monitorRecruit.getCharacter());
                monitorRecruitV2.setIdea(monitorRecruit.getIdea());
                monitorRecruitV2.setLessonId(monitorRecruit.getLessonId());
                monitorRecruitV2.setStudentId(monitorRecruit.getStudentId());
                StudyGroup studyGroup = studyTogetherService.loadStudentGroupByLessonId(monitorRecruit.getStudentId(), monitorRecruit.getLessonId());
                if (studyGroup != null) {
                    monitorRecruitV2.setGroupId(studyGroup.getId());
                }
                monitorRecruitV2.setParentId(monitorRecruit.getParentId());
                monitorRecruitV2.setStatus(MonitorRecruitType.IN_RECRUIT.getTypeId());
                monitorRecruitV2Service.saveApplyRecord(monitorRecruitV2);
            }

            //审核通过
            if (monitorRecruit.getStatus() == 1 || (parentIds.contains(SafeConverter.toString(monitorRecruit.getParentId())) && monitorRecruit.getStatus() == 4)) {
                BigMonitor bigMonitorInfo = crmMonitorRecruitService.getBigMonitorInfo(monitorRecruit.getParentId());
                if (bigMonitorInfo == null) {
                    logger.info("异常数据---->parentId:" + monitorRecruit.getParentId());
                    continue;
                }
                KolMonitorStatusRecord kolMonitorStatusRecord = new KolMonitorStatusRecord();
                kolMonitorStatusRecord.setParentId(monitorRecruit.getParentId());
                kolMonitorStatusRecord.setRecruitStatus(MonitorRecruitType.PASS.getTypeId());
                if (bigMonitorInfo.getStatus() == 0) {
                    kolMonitorStatusRecord.setLevel(MonitorLevelType.MONITOR.getLevelId());
                }
                if (bigMonitorInfo.getStatus() == 1) {
                    kolMonitorStatusRecord.setLevel(MonitorLevelType.JUNIOR_COUNSELLOR.getLevelId());
                }
                if (CollectionUtils.isNotEmpty(bigMonitorInfo.getClassInfoList())) {
                    List<String> groupIds = bigMonitorInfo.getClassInfoList().stream().map(ClassInfoInter::getClassId).collect(Collectors.toList());
                    Set<String> lessonIds = crmStudyTogetherService.$getStudyGroupByIds(groupIds).values().stream().map(StudyGroup::getLessonId).collect(Collectors.toSet());
                    kolMonitorStatusRecord.setGroupIds(groupIds);
                    kolMonitorStatusRecord.setLessonIds(lessonIds);
                }
                MonitorRecruitV2 monitorRecruitV2 = new MonitorRecruitV2();
                monitorRecruitV2.setBirthday(monitorRecruit.getBirthday());
                monitorRecruitV2.setInputWechatId(monitorRecruit.getInputWechatId());
                monitorRecruitV2.setProvince(monitorRecruit.getProvince());
                monitorRecruitV2.setCity(monitorRecruit.getCity());
                monitorRecruitV2.setProfession(monitorRecruit.getProfession());
                monitorRecruitV2.setTime(monitorRecruit.getTime());
                monitorRecruitV2.setEducation(monitorRecruit.getEducation());
                monitorRecruitV2.setAdvantage(monitorRecruit.getAdvantage());
                monitorRecruitV2.setCharacter(monitorRecruit.getCharacter());
                monitorRecruitV2.setIdea(monitorRecruit.getIdea());
                monitorRecruitV2.setLessonId(monitorRecruit.getLessonId());
                monitorRecruitV2.setStudentId(monitorRecruit.getStudentId());
                monitorRecruitV2.setParentId(monitorRecruit.getParentId());
                monitorRecruitV2.setStatus(MonitorRecruitType.PASS.getTypeId());
                StudyGroup studyGroup = studyTogetherService.loadStudentGroupByLessonId(monitorRecruit.getStudentId(), monitorRecruit.getLessonId());
                if (studyGroup != null) {
                    monitorRecruitV2.setGroupId(studyGroup.getId());
                }
                crmMonitorRecruitService.saveMonitorInfoRecord(kolMonitorStatusRecord);
                crmMonitorRecruitService.upsertRecruitInfoForJob(monitorRecruitV2);
            }
        }
        progressMonitor.done();
    }
}
