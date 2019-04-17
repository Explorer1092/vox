package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;
import com.voxlearning.utopia.service.newhomework.api.service.AvengerQueueService;
import com.voxlearning.utopia.service.newhomework.impl.queue.AvengerQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/6/15
 */
@Named
@ExposeService(interfaceClass = AvengerQueueService.class)
public class AvengerQueueServiceImpl extends SpringContainerSupport implements AvengerQueueService {

    @Inject private AvengerQueueProducer avengerQueueProducer;

    @Override
    public void sendHomeworkProcessResultList(List<JournalNewHomeworkProcessResult> results) {

    }

    @Override
    public void sendHomework(AvengerHomework avengerHomework) {
        if (avengerHomework == null) {
            return;
        }

        Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(avengerHomework));
        try {
            avengerQueueProducer.getHomeworkProducer().produce(message);
        } catch (Exception e) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", avengerHomework.getHomeworkId(),
                    "mod2", message.getBody().length,
                    "op", "AvengerHomework"
            ));
        }
    }

    public void sendJournalStudentHomework(JournalStudentHomework journalStudentHomework) {
        Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(journalStudentHomework));
        avengerQueueProducer.getStudentHomeworkFinishProducer().produce(message);
    }
}
