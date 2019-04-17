package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.support.CoinHistoryBuilder;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.CrmMonitorRecruitService;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitLoader;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.GroupArea;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.MonitorRecruitV2;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudentJoinStatistics;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorStatusRecord;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/08/06
 */
@Named
@ScheduledJobDefinition(
        jobName = "KOL排名发放学习币",
        jobDescription = "上课率在所在区排名前20%（包括20%）奖励学习币",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 12 ? * TUE-SAT"
)
@ProgressTotalWork(100)
public class AutoSendKolRankCoinJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @ImportService(interfaceClass = CrmMonitorRecruitService.class)
    private CrmMonitorRecruitService crmMonitorRecruitService;
    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;
    @ImportService(interfaceClass = MonitorRecruitLoader.class)
    private MonitorRecruitLoader monitorRecruitLoader;
    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<MonitorRecruitV2> passVerifyMonitors = monitorRecruitLoader.loadPassVerifyMonitors();
        if (CollectionUtils.isEmpty(passVerifyMonitors)) {
            return;
        }
        DayRange dayRange;
        if (RuntimeMode.le(Mode.TEST)) {
            dayRange = DayRange.current();
        } else {
            dayRange = DayRange.current().previous();
        }

        //所有kol昨天的状态记录
        List<KolMonitorStatusRecord> statusRecords = new ArrayList<>();
        passVerifyMonitors.forEach(monitorRecruitV2 -> {
            Long parentId = monitorRecruitV2.getParentId();
            KolMonitorStatusRecord statusRecord = getStatusRecord(parentId, dayRange);
            if (statusRecord != null) {
                statusRecords.add(statusRecord);
            }
        });

        List<GroupArea> groupAreas = crmStudyTogetherService.$getAllForJob();

        Integer coinTypeId = RuntimeMode.le(Mode.TEST) ? 56 : 45;
        List<String> areaIds = groupAreas.stream().map(GroupArea::getId).collect(Collectors.toList());

        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, areaIds.size());

        for (String areaId : areaIds) {
            List<StudentJoinStatistics> joinStatistics = getJoinStatisticsList(areaId, dayRange);
            if (CollectionUtils.isEmpty(joinStatistics)) {
                continue;
            }
            for (StudentJoinStatistics statistics : joinStatistics) {
                String groupId = statistics.getGroupId();
                Set<Long> parentIds = getKolParents(statusRecords, groupId);
                for (Long parentId : parentIds) {
                    List<Long> studentIds = parentLoaderClient.loadParentStudentRefs(parentId)
                            .stream()
                            .map(StudentParentRef::getStudentId)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(studentIds)) {
                        //家长有多个孩子，随机取一个孩子发放奖励
                        Collections.shuffle(studentIds);
                        Long studentId = studentIds.get(0);
                        CoinHistory coinHistory = new CoinHistoryBuilder().withUserId(studentId)
                                .withType(coinTypeId)
                                .build();
                        MapMessage mapMessage = dpCoinService.changeCoin(coinHistory);
                        if (!mapMessage.isSuccess()) {
                            logger.error("AutoSendKolRankCoinJob send coin error:studentId:{}", studentId);
                        }
                    }
                }
            }
            monitor.worked(1);
        }
        progressMonitor.done();
    }

    //获取参课率排名前20%的班级
    private List<StudentJoinStatistics> getJoinStatisticsList(String areaId, DayRange dayRange) {
        List<StudentJoinStatistics> studentJoinStatistics = crmMonitorRecruitService.loadTodayStatisticsForJobByGroupAreaId(areaId, dayRange);
        if (CollectionUtils.isNotEmpty(studentJoinStatistics)) {
            StudentJoinStatistics statistics = studentJoinStatistics.get(0);
            //取班级总数的百分之二十
            int totalClassCount = SafeConverter.toInt(statistics.getTotalClassCount());
            int coinRank = new BigDecimal(totalClassCount).divide(new BigDecimal(5), 0, BigDecimal.ROUND_HALF_UP).intValue();
            return studentJoinStatistics.stream().filter(e -> e.getRank() > 0 && e.getRank() <= coinRank).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Set<Long> getKolParents(List<KolMonitorStatusRecord> statusRecords, String groupId) {
        Set<Long> parentIds = new HashSet<>();
        statusRecords.forEach(statusRecord -> {
            if (CollectionUtils.isNotEmpty(statusRecord.getGroupIds()) && statusRecord.getGroupIds().contains(groupId)) {
                parentIds.add(statusRecord.getParentId());
            }
        });
        return parentIds;
    }


    //判断家长是否是kol
    private KolMonitorStatusRecord getStatusRecord(Long parentId, DayRange dayRange) {
        List<KolMonitorStatusRecord> statusRecords = crmMonitorRecruitService.getStatusListByParentId(parentId);
        //取昨天之前的最后一条(昨天之前最后的等级即为昨天最早的等级)
        KolMonitorStatusRecord coinRecord = statusRecords.stream()
                .filter(record -> record.getCreateDate().before(dayRange.getStartDate()))
                .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()))
                .findFirst()
                .orElse(null);
        if (coinRecord == null) {
            coinRecord = statusRecords.stream()
                    .filter(record -> record.getCreateDate().after(dayRange.getStartDate()) && record.getCreateDate().before(dayRange.getEndDate()))
                    .min(Comparator.comparing(KolMonitorStatusRecord::getCreateDate))
                    .orElse(null);
        }
        if (coinRecord != null) {
            Integer status = SafeConverter.toInt(coinRecord.getRecruitStatus());
            Integer level = SafeConverter.toInt(coinRecord.getLevel());
            if (status == 5 || level == 0) {
                coinRecord = null;
            }
        }
        return coinRecord;
    }

}
