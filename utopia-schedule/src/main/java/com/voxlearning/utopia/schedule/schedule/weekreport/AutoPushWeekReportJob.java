package com.voxlearning.utopia.schedule.schedule.weekreport;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.newhomework.api.entity.report.WeekPushTeacher;
import com.voxlearning.utopia.service.newhomework.consumer.WeekReportLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.*;
import java.util.stream.Collectors;


@Named
@ScheduledJobDefinition(
        jobName = "周报告，推送消息给老师",
        jobDescription = "周二凌晨6点跑job执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 6 ? * TUE"
)
@ProgressTotalWork(100)
public class AutoPushWeekReportJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    @Inject
    private WeekReportLoaderClient weekReportLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {


        int pageSize = 1000;

        int pageNum = 1;

        Page<WeekPushTeacher> weekPushTeacherPage;


        Long sendTimeEpochMilli = System.currentTimeMillis() + 5 * 30 * 60 * 1000L;

        Date date1 = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal1.setTime(date1);
        int week = cal1.get(Calendar.DAY_OF_WEEK);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (week > 1 && week < 6) {
            cal.add(Calendar.DAY_OF_WEEK, -7);
        }
        Date date = cal.getTime();


        do {
            Pageable pageable = PageableUtils.startFromOne(pageNum++, pageSize);

            weekPushTeacherPage = weekReportLoaderClient.loadWeekPushTeacherByPage(pageable);

            if (weekPushTeacherPage.getTotalElements() == 0)
                break;

            List<WeekPushTeacher> teacherIds = weekPushTeacherPage.getContent();

            if (CollectionUtils.isNotEmpty(teacherIds)) {

                send(teacherIds, sendTimeEpochMilli, date);
            }

        } while (!weekPushTeacherPage.isLast());

        progressMonitor.done();

    }

    private void send(List<WeekPushTeacher> weekPushTeachers, Long sendTimeEpochMilli, Date date) {


        Map<Integer, List<WeekPushTeacher>> teacherMap = weekPushTeachers.stream()
                .filter(o -> o.getCreateTime() != null)
                .filter(o -> o.getCreateTime().getTime() > date.getTime())
                .collect(Collectors.groupingBy(WeekPushTeacher::getSubject_key, Collectors.toList()));

        String link = "/view/mobile/common/weekreport/list";

        for (Integer key : teacherMap.keySet()) {

            if (key == null) {
                continue;
            }

            Subject subject = Subject.fromSubjectId(key);

            if (subject == null) {
                continue;
            }
            String name = subject.getValue();
            if (name != null) {
                List<Long> teacherIds = teacherMap.get(key)
                        .stream()
                        .map(WeekPushTeacher::getTeacherId)
                        .collect(Collectors.toList());

                Map<String, Object> extInfo = MapUtils.m("link", link,  "s", TeacherMessageType.WEEKREPORTNOTICE.getType(), "t", "week_report", "key", "j");

                appMessageServiceClient.sendAppJpushMessageByIds(name + "练习周报已生成，快点击分享给家长吧~", AppMessageSource.PRIMARY_TEACHER, new ArrayList<>(teacherIds), extInfo, sendTimeEpochMilli);//jpush

            }

        }


    }

}
