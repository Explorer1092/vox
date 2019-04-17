package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class TQR_RecordWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<TermQuizResultContext> {

    @Inject private AfentiQueueProducer afentiQueueProducer;
    @Inject private QuestionLoaderClient questionLoaderClient;

    @Override
    public void execute(TermQuizResultContext context) {
        if (Boolean.TRUE.equals(context.getMaster())) return;

        Long studentId = context.getStudent().getId();
        Subject subject = context.getSubject();
        String eid = context.getQuestionId();
        NewQuestion question = questionLoaderClient.loadQuestionByDocId(eid);
        if (question == null) return;
        Date current = new Date();

        WrongQuestionLibrary wrongQuestionLibrary = new WrongQuestionLibrary();
        wrongQuestionLibrary.setId(WrongQuestionLibrary.generateId(studentId, subject, question.getId()));
        wrongQuestionLibrary.setState(AfentiState.INCORRECT);
        wrongQuestionLibrary.setCreateAt(current);
        wrongQuestionLibrary.setUpdateAt(current);
        wrongQuestionLibrary.setDisabled(false);
        wrongQuestionLibrary.setSource(AfentiType.学习城堡.name());
        wrongQuestionLibrary.setUserId(studentId);
        wrongQuestionLibrary.setEid(question.getId());
        wrongQuestionLibrary.setSubject(subject);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("T", AfentiQueueMessageType.WRONG_QUESTION_LIBRARY);
        message.put("WQL", wrongQuestionLibrary);
        afentiQueueProducer.getProducer().produce(Message.newMessage().withStringBody(JsonUtils.toJson(message)));
    }
}
