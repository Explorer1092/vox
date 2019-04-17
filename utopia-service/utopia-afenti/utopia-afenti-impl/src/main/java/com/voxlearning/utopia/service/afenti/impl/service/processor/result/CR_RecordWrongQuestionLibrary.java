package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
public class CR_RecordWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {

    @Inject private AfentiQueueProducer afentiQueueProducer;

    @Override
    public void execute(CastleResultContext context) {
        if (Boolean.TRUE.equals(context.getMaster())) return;

        Date current = new Date();
        String id = WrongQuestionLibrary.generateId(context.getStudent().getId(), context.getSubject(), context.getQuestionId());

        WrongQuestionLibrary wrongQuestionLibrary = new WrongQuestionLibrary();
        wrongQuestionLibrary.setId(id);
        wrongQuestionLibrary.setState(AfentiState.INCORRECT);
        wrongQuestionLibrary.setCreateAt(current);
        wrongQuestionLibrary.setUpdateAt(current);
        wrongQuestionLibrary.setDisabled(false);
        wrongQuestionLibrary.setSource(context.getLearningType().name());
        wrongQuestionLibrary.setUserId(context.getStudent().getId());
        wrongQuestionLibrary.setEid(context.getQuestionId());
        wrongQuestionLibrary.setSubject(context.getSubject());

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("T", AfentiQueueMessageType.WRONG_QUESTION_LIBRARY);
        message.put("WQL", wrongQuestionLibrary);
        afentiQueueProducer.getProducer().produce(Message.newMessage().withStringBody(JsonUtils.toJson(message)));
    }
}
