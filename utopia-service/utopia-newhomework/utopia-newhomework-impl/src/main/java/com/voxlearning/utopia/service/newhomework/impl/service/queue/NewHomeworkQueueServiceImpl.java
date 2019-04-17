package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.newhomework.api.context.AssignmentRecordContext;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.ViewHintReq;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkQueueService;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.InterventionPublisher;
import com.voxlearning.utopia.service.newhomework.impl.queue.AvengerQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2016/10/31
 */
@Named
@Service(interfaceClass = NewHomeworkQueueService.class)
@ExposeService(interfaceClass = NewHomeworkQueueService.class)
public class NewHomeworkQueueServiceImpl extends SpringContainerSupport implements NewHomeworkQueueService {

    @Inject private NewHomeworkQueueProducer newHomeworkQueueProducer;
    @Inject private AvengerQueueProducer avengerQueueProducer;
    @Inject private InterventionPublisher interventionPublisher;

    @Override
    public void sendUpdateTotalAssignmentRecordMessage(Subject subject, List<NewHomeworkPracticeContent> practices, Integer clazzGroupSize) {
        AssignmentRecordContext assignmentRecordContext = new AssignmentRecordContext(practices);

        UpdateTotalAssignmentRecordCommand command = new UpdateTotalAssignmentRecordCommand();
        command.setSubject(subject);
        command.setClazzGroupSize(clazzGroupSize);
        command.setQuestionMap(assignmentRecordContext.getQuestionMap());
        command.setPackageSet(assignmentRecordContext.getPackageSet());
        command.setPaperSet(assignmentRecordContext.getPaperSet());

        Message message = Message.newMessage().writeObject(command);
        newHomeworkQueueProducer.getProducer().produce(message);
    }

    @Override
    public void saveJournalNewHomeworkProcessResults(List<JournalNewHomeworkProcessResult> results) {
        results = CollectionUtils.toLinkedList(results);
        if (results.isEmpty()) {
            return;
        }

//        SaveJournalNewHomeworkProcessResultCommand command = new SaveJournalNewHomeworkProcessResultCommand();
//        command.setResults(results);
//
//        Message message = Message.newMessage().writeObject(command);
//        newHomeworkQueueProducer.getProducer().produce(message);

        // 上面是老的上报，下面是新的上报，直接去kafka了
        for (JournalNewHomeworkProcessResult processResult : results) {
            if (processResult != null) {
                processResult.setEnv(RuntimeMode.getCurrentStage());
                Message messageAvenger = Message.newMessage().withPlainTextBody(JsonUtils.toJson(processResult));
                avengerQueueProducer.getJournalHomeworkProcessResultProducer().produce(messageAvenger);
            }
        }
    }

    @Override
    public void saveJournalStudentHomework(JournalStudentHomework journalStudentHomework) {
        Message message = Message.newMessage().writeObject(journalStudentHomework);
        newHomeworkQueueProducer.getJournalProducer().produce(message);
    }

    @Override
    public void saveHomeworkSyllable(List<NewHomeworkSyllable> results) {
        results = CollectionUtils.toLinkedList(results);
        if (results.isEmpty()) {
            return;
        }

        SaveHomeworkSyllableCommand command = new SaveHomeworkSyllableCommand();
        command.setResults(results);

        Message message = Message.newMessage().writeObject(command);
        newHomeworkQueueProducer.getProducer().produce(message);
    }

    @Override
    public void saveSelfStudyWordsIncreaseHomework(Long clazzGroupId, Long studentId, Map<String, Map<String, List<String>>> bookToKpMap) {
        if (clazzGroupId == null || studentId == null || MapUtils.isEmpty(bookToKpMap)) {
            return;
        }

        SaveSelfStudyWordIncreaseHomeworkCommand command = new SaveSelfStudyWordIncreaseHomeworkCommand();
        command.setClazzGroupId(clazzGroupId);
        command.setStudentId(studentId);
        command.setBookToKpMap(bookToKpMap);

        Message message = Message.newMessage().writeObject(command);
        newHomeworkQueueProducer.getProducer().produce(message);
    }

    @Override
    public void interventionViewhintProducer(Long userId, ViewHintReq request) {
        InterventionCommand command = new InterventionCommand();
        command.setActor(userId);
        command.setObject(request.getQuestionId() + "#" + request.getHintId());
        command.setVerb(request.getVerb());
        InterventionCommand.Context context = new InterventionCommand.Context(request.getHomeworkId(), request.getObjectiveConfigType());
        context.setHintTag(request.getHintTag());
        command.setContext(context);
        if (request.getTimestamp() != null) {
            command.setTimestamp(DateUtils.dateToString(request.getTimestamp()));
        }
        command.setDuration(request.getDuration());
        command.setResult(request.getResult());
        interventionPublisher.getInterventionProducer().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(command)));
    }
}
