package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
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
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@ScheduledJobDefinition(
        jobName = "薯条英语修复用户课程数据",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class FixChipsUserCourseDataJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;


    @AlpsQueueProducer(queue = "utopia.chips.user.course.fix.data.queue")
    private MessageProducer chipsUserCourseFixDataMessageProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<ChipsEnglishClass> chipsEnglishClassList = chipsEnglishClazzService.selectAllChipsEnglishClass();
        if (CollectionUtils.isEmpty(chipsEnglishClassList)) {
            return;
        }

        for(ChipsEnglishClass clazz : chipsEnglishClassList) {
            List<Long> userList = chipsEnglishClazzService.selectAllUserByClazzId(clazz.getId());
            if (CollectionUtils.isEmpty(userList)) {
                continue;
            }
            userList.stream().collect(Collectors.toSet()).forEach(user ->
                    chipsUserCourseFixDataMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(new UserCourseMap(user, clazz.getProductId())))));
        }
    }

    private static class UserCourseMap extends HashMap<String, Object> {
        UserCourseMap(Long userId, String productId) {
            this.put("U", userId);
            this.put("P", productId);
        }
    }
}
