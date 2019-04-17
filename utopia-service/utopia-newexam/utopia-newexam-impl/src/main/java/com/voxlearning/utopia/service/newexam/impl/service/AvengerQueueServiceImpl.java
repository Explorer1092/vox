package com.voxlearning.utopia.service.newexam.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.newexam.api.entity.AvengerNewExam;
import com.voxlearning.utopia.service.newexam.api.entity.JournalNewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.JournalStudentNewExam;
import com.voxlearning.utopia.service.newexam.api.service.AvengerQueueService;
import com.voxlearning.utopia.service.newexam.impl.queue.AvengerQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @Description: 数据上报
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:44
 */
@Named
@ExposeService(interfaceClass = AvengerQueueService.class)
public class AvengerQueueServiceImpl extends SpringContainerSupport implements AvengerQueueService{
    @Inject
    private AvengerQueueProducer avengerQueueProducer;

    @Override
    public void sendExam(AvengerNewExam avengerNewExam) {
        if (avengerNewExam == null) {
            return;
        }

        Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(avengerNewExam));
        try {
            avengerQueueProducer.getNewExamProducer().produce(message);
        } catch (Exception e) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", avengerNewExam.getHomeworkId(),
                    "mod2", message.getBody().length,
                    "op", "AvengerHomework"
            ));
        }
    }

    @Override
    public void sendExamProcessResultList(List<JournalNewExamProcessResult> results) {

    }

    @Override
    public void sendJournalStudentExam(JournalStudentNewExam journalStudentNewExam) {
        Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(journalStudentNewExam));
        avengerQueueProducer.getStudentNewExamFinishProducer().produce(message);
    }

    @Override
    public void sendJournalNewExamProcessResultProducer(List<JournalNewExamProcessResult> results) {
        results = CollectionUtils.toLinkedList(results);
        if (results.isEmpty()) {
            return;
        }

        // 直接去kafka
        for (JournalNewExamProcessResult processResult : results) {
            if (processResult != null) {
                processResult.setEnv(RuntimeMode.getCurrentStage());
                Message messageAvenger = Message.newMessage().withPlainTextBody(JsonUtils.toJson(processResult));
                avengerQueueProducer.getJournalNewExamProcessResultProducer().produce(messageAvenger);
            }
        }
    }
}
