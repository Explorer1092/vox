
package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.campaign.api.TeacherCoursewareContestService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;

import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 每天早上4点检查正在审核中的老师课件作品，超过三天的自动释放为待审核
 *
 * @author tao.song
 */
@Named
@ScheduledJobDefinition(
        jobName = "检查正在审核中的老师课件",
        jobDescription = "每天早上4点运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 4 * * ?"
)
@ProgressTotalWork(100)
public class AutoCheckTeacherExamingCoursewareJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = TeacherCoursewareContestService.class)
    private TeacherCoursewareContestService teacherCoursewareContestService;

    @AlpsQueueProducer(queue = "utopia.campaign.teacher.courseware.examining.expire")
    private MessageProducer coursewareProducer;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        List<TeacherCourseware> teacherCoursewareList = teacherCoursewareContestService.loadExaminingCoursewareList();
        if (CollectionUtils.isEmpty(teacherCoursewareList)) {
            return;
        }

        Date now = new Date();
        teacherCoursewareList.stream().filter(e -> DateUtils.dayDiff(now, e.getExamineUpdateTime()) >= 3L)
                .forEach(e -> {
                    Map<String, Object> message = new LinkedHashMap<>();
                    message.put("CID", e.getId());
                    coursewareProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
                });
    }
}
