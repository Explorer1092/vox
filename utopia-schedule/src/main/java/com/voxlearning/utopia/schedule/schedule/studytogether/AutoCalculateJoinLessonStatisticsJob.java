package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.parent.api.CrmMonitorRecruitService;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitLoader;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.GroupArea;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudentJoinStatistics;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2018-7-14
 */
@Named
@ScheduledJobDefinition(
        jobName = "一起学计算参课率排名",
        jobDescription = "每半小时执行一次",
        disabled = {Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0/30 * * * ? "
)
@ProgressTotalWork(100)
public class AutoCalculateJoinLessonStatisticsJob extends ProgressedScheduleJob {

    @ImportService(interfaceClass = MonitorRecruitService.class)
    private MonitorRecruitService monitorRecruitService;
    @ImportService(interfaceClass = MonitorRecruitLoader.class)
    private MonitorRecruitLoader monitorRecruitLoader;
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @ImportService(interfaceClass = CrmMonitorRecruitService.class)
    private CrmMonitorRecruitService crmMonitorRecruitService;
    

    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return;
        }
        List<GroupArea> groupAreas = crmStudyTogetherService.$getAllForJob();
        if (CollectionUtils.isEmpty(groupAreas)) {
            return;
        }
        for (GroupArea groupArea : groupAreas) {
            ISimpleProgressMonitor monitor = progressMonitor.subTask(10, 100);
            //用班级区id取数据
            Map<String, List<StudyGroup>> studyGroupMaps = crmStudyTogetherService.getStudyGroupByAreaId(groupArea.getId());
            if (MapUtils.isEmpty(studyGroupMaps)) {
                continue;
            }
            //所有班级按照课程分组
            Map<String, List<StudyGroup>> lessonGroupMaps = studyGroupMaps.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(p -> StringUtils.isNotBlank(p.getLessonId()))
                    .collect(Collectors.groupingBy(StudyGroup::getLessonId));
            //一个班级区内上报的所有班级数据
            List<StudentJoinStatistics> studentJoinStatistics = crmMonitorRecruitService.loadTodayStatisticsForJobByGroupAreaId(groupArea.getId(), DayRange.current());
            if (CollectionUtils.isEmpty(studentJoinStatistics)) {
                continue;
            }
            for (String lessonId : lessonGroupMaps.keySet()) {
                List<StudyGroup> list = lessonGroupMaps.get(lessonId);
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                //一个课程下的所有班级ID
                Set<String> groupIds = list.stream().map(StudyGroup::getId).collect(Collectors.toSet());
                //一个课程下的所有班级的统计数据
                List<StudentJoinStatistics> joinStatisticsList = studentJoinStatistics.stream().filter(p -> groupIds.contains(p.getGroupId())).collect(Collectors.toList());
                Map<String, StudentJoinStatistics> joinStatisticsMap = joinStatisticsList.stream().collect(Collectors.toMap(StudentJoinStatistics::getGroupId, Function.identity()));
                Integer classTotalCount = list.size();
                //班级人数不足20人的班级个数
                int needRemoveClassCount = 0;
                for (String groupId : groupIds) {
                    StudentJoinStatistics statistics = joinStatisticsMap.get(groupId);
                    if (statistics == null) {
                        //没有上报。生成一个新的。便于之后直接Upsert进数据库
                        statistics = new StudentJoinStatistics();
                        statistics.setDayRange(DayRange.current().toString());
                        statistics.setClassArea(groupArea.getGroupAreaName());
                        statistics.setGroupId(groupId);
                        statistics.setGroupAreaId(groupArea.getId());
                        statistics.setStudentJoinCount(0);
                        statistics.setLessonId(lessonId);
                        joinStatisticsList.add(statistics);
                    }
                    Long studentTotalCount = monitorRecruitLoader.loadStudentCountForJob(groupId);
                    //班级人数
                    statistics.setStudentTotalCount(SafeConverter.toInt(studentTotalCount));
                    if (studentTotalCount == null || studentTotalCount == 0) {
                        statistics.setStudentJoinRate(0d);
                    } else if (statistics.getStudentJoinCount() == null) {
                        statistics.setStudentJoinRate(0d);
                    } else if (statistics.getStudentJoinCount() > studentTotalCount) {
                        statistics.setStudentJoinRate(100d);
                    } else {
                        //参课率
                        double rate = new BigDecimal(statistics.getStudentJoinCount() * 100).divide(new BigDecimal(studentTotalCount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        statistics.setStudentJoinRate(rate);
                    }
                    //班级人数少于20
                    //TODO 临时修改成4个
                    if (statistics.getStudentTotalCount() < 2) {
                        needRemoveClassCount++;
                    }
                }
                //参课率排序
                joinStatisticsList.sort((o1, o2) -> o2.getStudentJoinRate().compareTo(o1.getStudentJoinRate()));
                //移除班级人数少于20后的班级
                classTotalCount = classTotalCount - needRemoveClassCount;
                Double lastRate = null;
                int rank = 0;
                for (StudentJoinStatistics statistics : joinStatisticsList) {
                    statistics.setTotalClassCount(classTotalCount);
                    //班级人数少于20.不参与排名。直接保存
                    //TODO 临时修改成4个
                    if (statistics.getStudentTotalCount() < 2) {
                        statistics.setRank(0);
                    } else {
                        //处理排名
                        if (!Objects.equals(lastRate, statistics.getStudentJoinRate())) {
                            lastRate = statistics.getStudentJoinRate();
                            rank++;
                        }
                        statistics.setRank(rank);
                    }
                    //保存排名
                    monitorRecruitService.saveStatistics(statistics);
                }
            }
            monitor.done();
        }
    }
}
