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
import com.voxlearning.galaxy.service.coin.api.CoinTypeBufferLoaderClient;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.entity.CoinType;
import com.voxlearning.galaxy.service.coin.api.support.CoinHistoryBuilder;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.CrmMonitorRecruitService;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitLoader;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.MonitorRecruitV2;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorStatusRecord;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/08/03
 */
@Named
@ScheduledJobDefinition(
        jobName = "KOL分级发放学习币",
        jobDescription = "每天中午十二点根据昨天的等级情况发放学习币",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 12 * * ?"
)
@ProgressTotalWork(100)
public class AutoSendKolMonitorCoinJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = MonitorRecruitLoader.class)
    private MonitorRecruitLoader monitorRecruitLoader;
    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @ImportService(interfaceClass = CrmMonitorRecruitService.class)
    private CrmMonitorRecruitService crmMonitorRecruitService;
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private CoinTypeBufferLoaderClient coinTypeBufferLoaderClient;
    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    private static Map<Integer, Integer> monitorCoinTypeMap;

    static {
        monitorCoinTypeMap = new HashMap<>();
        if (RuntimeMode.le(Mode.TEST)) {
            monitorCoinTypeMap.put(1, 1);
            monitorCoinTypeMap.put(2, 51);
            monitorCoinTypeMap.put(3, 52);
            monitorCoinTypeMap.put(4, 53);
            monitorCoinTypeMap.put(5, 54);
            monitorCoinTypeMap.put(6, 55);
        } else {
            monitorCoinTypeMap.put(1, 1);
            monitorCoinTypeMap.put(2, 34);
            monitorCoinTypeMap.put(3, 41);
            monitorCoinTypeMap.put(4, 42);
            monitorCoinTypeMap.put(5, 43);
            monitorCoinTypeMap.put(6, 44);
        }
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<MonitorRecruitV2> passVerifyMonitors = monitorRecruitLoader.loadPassVerifyMonitors();
        if (CollectionUtils.isEmpty(passVerifyMonitors)) {
            return;
        }
        Set<Long> parentIds = passVerifyMonitors.stream().map(MonitorRecruitV2::getParentId).collect(Collectors.toSet());
        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, parentIds.size());
        Map<String, StudyLesson> lessonMap = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .collect(Collectors.toMap(t -> SafeConverter.toString(t.getLessonId()), Function.identity()));
        Map<Integer, CoinType> coinTypeMap = coinTypeBufferLoaderClient.getCoinTypes().stream().collect(Collectors.toMap(CoinType::getId, Function.identity()));

        DayRange dayRange;
        if (RuntimeMode.le(Mode.TEST)) {
            dayRange = DayRange.current();
        } else {
            dayRange = DayRange.current().previous();
        }
        parentIds.forEach(parentId -> {
            List<KolMonitorStatusRecord> statusRecords = crmMonitorRecruitService.getStatusListByParentId(parentId);
            KolMonitorStatusRecord sendRecord = getCoinStatusRecord(statusRecords, dayRange);
            if (sendRecord != null) {
                if (CollectionUtils.isNotEmpty(sendRecord.getLessonIds())) {
                    //家长管理的所有正在上的课
                    Set<String> lessonIds = sendRecord.getLessonIds().stream()
                            .filter(lessonId -> {
                                StudyLesson studyLesson = lessonMap.get(lessonId);
                                return studyLesson != null && studyLesson.getCloseDate().after(dayRange.getStartDate()) && studyLesson.getOpenDate().before(dayRange.getEndDate());
                            })
                            .collect(Collectors.toSet());
                    //家长管理的所有课下面孩子的班级
                    List<String> groupIds = sendRecord.getGroupIds();
                    List<StudyGroup> studyGroups = new ArrayList<>(crmStudyTogetherService.$getStudyGroupByIds(groupIds).values());

                    if (CollectionUtils.isNotEmpty(lessonIds) && CollectionUtils.isNotEmpty(studyGroups)) {
                        Integer monitorLevel = sendRecord.getLevel();
                        Integer coinTypeId = monitorCoinTypeMap.get(monitorLevel);
                        CoinType coinType = coinTypeMap.get(coinTypeId);
                        if (coinType != null) {
                            List<Long> studentIds = parentLoaderClient.loadParentStudentRefs(parentId)
                                    .stream()
                                    .map(StudentParentRef::getStudentId)
                                    .collect(Collectors.toList());
                            Collections.shuffle(studentIds);
                            //已经发过奖励的班级id
                            Set<String> hasSendGroupIds = new HashSet<>();
                            for (Long studentId : studentIds) {
                                //当前孩子激活的课程
                                List<String> studentLessonIds = studyTogetherServiceClient.loadStudentGroupByLessonId(studentId, lessonIds).values()
                                        .stream()
                                        .map(StudyGroup::getLessonId)
                                        .collect(Collectors.toList());
                                List<String> studentGroupIds = studyGroups.stream()
                                        .filter(studyGroup -> studentLessonIds.contains(studyGroup.getLessonId()))
                                        .filter(studyGroup -> !hasSendGroupIds.contains(studyGroup.getId()))
                                        .map(StudyGroup::getId)
                                        .collect(Collectors.toList());

                                int totalCount = studentGroupIds.size() * coinType.getCount();
                                if (totalCount > 0) {
                                    CoinHistory coinHistory = new CoinHistoryBuilder().withUserId(studentId)
                                            .withType(coinTypeId)
                                            .withCount(totalCount)
                                            .build();
                                    MapMessage mapMessage = dpCoinService.changeCoin(coinHistory);
                                    if (!mapMessage.isSuccess()) {
                                        logger.error("AutoSendKolMonitorCoinJob send kol coin error:studentId:{}, monitorLevel:{}, coinType:{}, count:{}", studentId, monitorLevel, coinTypeId, totalCount);
                                    }
                                    hasSendGroupIds.addAll(studentGroupIds);
                                }
                            }
                        } else {
                            logger.error("AutoSendKolMonitorCoinJob get kol coinType error:parentId:{}, monitorLevel:{}, coinType:{}", sendRecord.getParentId(), monitorLevel, coinTypeId);
                        }

                    }

                }
            }
            monitor.worked(1);
        });
        progressMonitor.done();
    }

    //获取给用户发放学币的KOL状态依据记录
    private KolMonitorStatusRecord getCoinStatusRecord(List<KolMonitorStatusRecord> statusRecords, DayRange dayRange) {
        //取昨天之前的最后一条
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
