package com.voxlearning.utopia.schedule.schedule.studyplanning;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningLoader;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningService;
import com.voxlearning.galaxy.service.studyplanning.api.constant.StudyPlanningQuantum;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningConfig;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningFinishRecord;
import com.voxlearning.galaxy.service.studyplanning.api.entity.UserDailyStudyPlanningConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author wei.jiang
 * @since 2018/10/29
 */
@Named
@ScheduledJobDefinition(
        jobName = "学习规划发送push",
        jobDescription = "每天17：30发送",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 17 * * ? "
)
@ProgressTotalWork(100)
public class AutoSendStudyPlanningPushJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = StudyPlanningLoader.class)
    private StudyPlanningLoader studyPlanningLoader;
    @ImportService(interfaceClass = StudyPlanningService.class)
    private StudyPlanningService studyPlanningService;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;


    private static Map<Integer, String> map = new HashMap<>();

    static {
        map.put(0, "养成一个习惯只需要7天，再坚持一下就可以看到孩子的进步~");
        map.put(1, "家长们都在用，快来开启学习规划，培养孩子良好学习习惯~");
        map.put(2, "提高孩子学习效率的秘密武器，查看孩子学习任务~");
        map.put(3, "一天一点进步，积累看得见，去完成孩子一天规划吧~");
        map.put(4, "今天还要加油鸭！督促孩子来完成学习任务吧~ ");
        map.put(5, "完成本周最后的学习任务吧，孩子好的习惯即将养成~");
        map.put(6, "坚持打卡，培养持之以恒的品质，给孩子受用一生的礼物~");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        ConcurrentHashSet<Long> allParentIds = new ConcurrentHashSet<>();
        Date current = new Date();
        Map<String, Integer> tabIndexMap = new HashMap<>();
        tabIndexMap.put("index", 2);
        Pageable pageable = new PageRequest(0, 1000);
        Page<StudyPlanningConfig> studyPlanningConfigs = studyPlanningLoader.loadStudyPlanningConfigForJob(pageable, 0L);
        long totalElements = studyPlanningConfigs.getTotalElements();
        if (totalElements == 0) {
            progressMonitor.done();
            return;
        }
        doPush(studyPlanningConfigs.getContent(), current, tabIndexMap, allParentIds);
        progressMonitor.worked(10);
        long everyThreadCount = (totalElements - 1000) / 10;
        List<StudyPlanningConfig> list = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(studyPlanningConfigs.getContent())) {
//            pageable = studyPlanningConfigs.nextPageable();
            Long sid = studyPlanningConfigs.getContent().get(studyPlanningConfigs.getContent().size() - 1).getId();
            studyPlanningConfigs = studyPlanningLoader.loadStudyPlanningConfigForJob(pageable, sid);
            progressMonitor.worked(1);
            if (list.size() < everyThreadCount) {
                list.addAll(studyPlanningConfigs.getContent());
            } else {
                List<StudyPlanningConfig> finalList = list;
                AlpsThreadPool.getInstance().submit(() -> doPush(finalList, current, tabIndexMap, allParentIds));
                list = new ArrayList<>();
            }
        }
//        logger.info("allPushCount---->" + allParentIds.size());
        progressMonitor.done();
    }


    private void doPush(List<StudyPlanningConfig> studyPlanningConfigs, Date current, Map<String, Integer> indexMap, Set<Long> allParentIds) {
        for (StudyPlanningConfig studyPlanningConfig : studyPlanningConfigs) {
            UserDailyStudyPlanningConfig dailyConfig = studyPlanningLoader.loadDailyConfig(UserDailyStudyPlanningConfig.generateId(studyPlanningConfig.getId(), current));
            if (dailyConfig == null || MapUtils.isEmpty(dailyConfig.getItemIdMap())) {
                continue;
            }
            List<StudyPlanningFinishRecord> todayFinishRecord = studyPlanningLoader.loadOneDayFinishRecord(studyPlanningConfig.getId(), current);
            if (CollectionUtils.isEmpty(todayFinishRecord)) {
                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studyPlanningConfig.getId());
                if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                    studentParentRefs.forEach(e -> {
                        if (allParentIds.add(e.getParentId())) {
                            pushMessage(e.getParentId(), indexMap);
                        }
                    });
                }
            }
        }
    }


    private void pushMessage(Long parentId, Map<String, Integer> indexMap) {
        String message = map.get(getCurrentWeekDay());
        appMessageServiceClient.sendAppJpushMessageByIds(message,
                AppMessageSource.PARENT,
                Collections.singletonList(parentId),
                MapUtils.m("s", ParentAppPushType.NATIVE_TAB.name(), "params", JsonUtils.toJson(indexMap)));
    }


    private int getCurrentWeekDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }
}
