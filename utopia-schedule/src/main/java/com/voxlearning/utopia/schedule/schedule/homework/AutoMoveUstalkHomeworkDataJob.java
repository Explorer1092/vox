package com.voxlearning.utopia.schedule.schedule.homework;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkLivecastService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkProcessResultLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/8/3
 */
@Named
@ScheduledJobDefinition(
        jobName = "迁移Ustalk的作业数据",
        jobDescription = "迁移数据用",
        disabled = {Mode.TEST, Mode.UNIT_TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 4 ? * FRI"
)
@ProgressTotalWork(100)
public class AutoMoveUstalkHomeworkDataJob extends ScheduledJobWithJournalSupport {

    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject private NewHomeworkProcessResultLoaderClient newHomeworkProcessResultLoaderClient;
    @Inject private EmailServiceClient emailServiceClient;

    @ImportService(interfaceClass = NewHomeworkLivecastService.class)
    private NewHomeworkLivecastService livecastService;

    @ImportService(interfaceClass = NewHomeworkLivecastLoader.class)
    private NewHomeworkLivecastLoader newHomeworkLivecastLoader;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

//        List<UsTalkHomeworkData> ustalkHomeworkDataSet = new ArrayList<>();
//        UsTalkHomeworkData data = new UsTalkHomeworkData();
//        data.setHomeworkId("201610_5810602377748751e918a205");
//        data.setStudentId(333890305L);
//        ustalkHomeworkDataSet.add(data);

        List<UsTalkHomeworkData> ustalkHomeworkDataSet = newHomeworkLivecastLoader.findAllUsTalkHomeworkData();
        ustalkHomeworkDataSet.forEach(o -> o.setHomeworkId(StringUtils.replace(o.getHomeworkId(), "\r", "")));

//        Set<String> homeworkIdSet = ustalkHomeworkDataSet.stream()
//                .filter(o -> StringUtils.isNotBlank(o.getHomeworkId()))
//                .map(UsTalkHomeworkData::getHomeworkId)
//                .collect(Collectors.toSet());

//        Map<String, NewHomework> homeworkMap = newHomeworkLoaderClient.loads(homeworkIdSet);
//        Map<String, NewHomeworkBook> homeworkBookMap = newHomeworkLoaderClient.loadNewHomeworkBooks(homeworkIdSet);

        int success = 0;
        int failure = 0;
        List<String> errorDataList = new ArrayList<>();
        for (UsTalkHomeworkData homeworkData : ustalkHomeworkDataSet) {
            String homeworkId = homeworkData.getHomeworkId();
            NewHomework homework = newHomeworkLoaderClient.load(homeworkId);
            NewHomeworkBook homeworkBook = newHomeworkLoaderClient.loadNewHomeworkBook(homeworkId);
            if (homework != null && homeworkBook != null) {
                if (internalMove(homework, homeworkBook, homeworkData.getStudentId())) {
                    success++;
                } else {
                    failure++;
                    String errorData = homeworkData.getId() + "," + homeworkData.getStudentId() + "," + homeworkData.getHomeworkId();
                    errorDataList.add(errorData);
                }
            }
        }
        progressMonitor.done();
        long endTimeStamp = System.currentTimeMillis();
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> content = new HashMap<>();

        String info = RuntimeMode.getCurrentStage() + "环境，任务开始时间：" + fdf.format(startTimestamp)
                + "，任务结束时间：" + fdf.format(endTimeStamp)
                + "，任务运行时长：" + NewHomeworkUtils.formatDuring(endTimeStamp - startTimestamp) + "<br />成功" + success + "，失败" + failure + "<br />"
                + StringUtils.join(errorDataList, "<br />");

        content.put("info", info);
        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                .to("xuesong.zhang@17zuoye.com;longlong.yu@17zuoye.com;wentao.chen@17zuoye.com")
                .subject("作业迁移通知")
                .content(content)
                .send();
    }

    private boolean internalMove(NewHomework homework, NewHomeworkBook homeworkBook, Long studentId) {
        boolean allSuccess = false;
        String homeworkId = homework.getId();
        NewHomeworkResult homeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(homework.toLocation(), studentId, true);
        if (homeworkResult == null) {
            // 如果result是空，说明学生还没有做作业，只拷贝book和homework
            boolean isSuccessBook = processHomeworkBook(homeworkBook);
            if (isSuccessBook) {
                allSuccess = processHomework(homework);
            }
            return allSuccess;
        }

        Set<ObjectiveConfigType> typeSet = homework.getPractices().stream()
                .filter(o -> o.getType() != null)
                .map(NewHomeworkPracticeContent::getType)
                .collect(Collectors.toSet());

        Set<String> processIdSet = new HashSet<>();
        for (ObjectiveConfigType type : typeSet) {
            processIdSet.addAll(homeworkResult.findHomeworkProcessIdsByObjectiveConfigType(type));
        }

        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoaderClient.loads(homeworkId, processIdSet);

        // copy数据需要反着撸

        boolean isSuccessProcess = processHomeworkProcessResult(processResultMap);
        if (isSuccessProcess) {
            boolean isSuccessResult = processHomeworkResult(homework.toLocation(), studentId, homeworkResult);
            if (isSuccessResult) {
                boolean isSuccessBook = processHomeworkBook(homeworkBook);
                if (isSuccessBook) {
                    allSuccess = processHomework(homework);
                }
            }
        }
        return allSuccess;
    }

    private boolean processHomework(NewHomework homework) {
        if (homework == null) {
            return true;
        }
        try {
            LiveCastHomework target = new LiveCastHomework();
            PropertiesUtils.copyProperties(target, homework);
            livecastService.insertsLiveCastHomeworkWithoutCache(Collections.singleton(target));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean processHomeworkBook(NewHomeworkBook homeworkBook) {
        if (homeworkBook == null) {
            return true;
        }
        try {
            LiveCastHomeworkBook target = new LiveCastHomeworkBook();
            PropertiesUtils.copyProperties(target, homeworkBook);
            livecastService.insertsLiveCastHomeworkBookWithoutCache(Collections.singleton(target));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean processHomeworkResult(NewHomework.Location location, Long studentId, NewHomeworkResult homeworkResult) {
        if (homeworkResult == null) {
            return true;
        }
        try {
            LiveCastHomeworkResult target = new LiveCastHomeworkResult();
            PropertiesUtils.copyProperties(target, homeworkResult);
            // 这里需要处理一下id
            String month = MonthRange.newInstance(location.getCreateTime()).toString();
            LiveCastHomeworkResult.ID id = new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), studentId);
            target.setId(id.toString());
            livecastService.insertsLiveCastHomeworkResultWithoutCache(Collections.singleton(target));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean processHomeworkProcessResult(Map<String, NewHomeworkProcessResult> processResultMap) {
        if (MapUtils.isEmpty(processResultMap)) {
            return true;
        }
        try {
            Set<LiveCastHomeworkProcessResult> processResultSet = new HashSet<>();
            // source target
            processResultMap
                    .values()
                    .forEach(o -> {
                        LiveCastHomeworkProcessResult target = new LiveCastHomeworkProcessResult();
                        PropertiesUtils.copyProperties(target, o);
                        processResultSet.add(target);
                    });
            livecastService.insertsLiveCastHomeworkProcessResultWithoutCache(processResultSet);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
