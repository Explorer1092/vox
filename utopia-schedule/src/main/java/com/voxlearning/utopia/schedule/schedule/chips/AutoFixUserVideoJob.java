
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.AiChipsEnglishConfigService;
import com.voxlearning.utopia.service.ai.client.AiChipsEnglishConfigServiceClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

/**
 * @Author songtao
 * TODO 需要等APP版本比较高的时候下线
 */
@Deprecated
@Named
@ScheduledJobDefinition(
        jobName = "薯条英语用户视频数据修复",
        jobDescription = "每一个小时执行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? "
)
@ProgressTotalWork(100)
public class AutoFixUserVideoJob extends ScheduledJobWithJournalSupport {


    @AlpsQueueProducer(queue = "utopia.chips.course.user.video.data.fix.queue")
    private MessageProducer dataFixQueueProducer;

    @Inject
    private AiChipsEnglishConfigServiceClient chipsEnglishConfigServiceClient;

    private static final String CONFIG = "chips_user_video_data_fix";

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Content> contentList = Optional.ofNullable(chipsEnglishConfigServiceClient.loadChipsEnglishConfigByName(CONFIG))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(e -> JsonUtils.fromJsonToList(e.getValue(), Content.class))
                .orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(contentList)) {
            contentList.forEach(e -> {
                Map<String, Object> message = new HashMap<>();
                message.put("B", e.getBook());
                message.put("U", e.getUnit());
                message.put("L", e.getLesson());
                dataFixQueueProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            });
        }
    }

    @Getter
    @Setter
    private static class Content implements Serializable {
        private static final long serialVersionUID = 0L;
        private String book;
        private String unit;
        private String lesson;
    }
}
