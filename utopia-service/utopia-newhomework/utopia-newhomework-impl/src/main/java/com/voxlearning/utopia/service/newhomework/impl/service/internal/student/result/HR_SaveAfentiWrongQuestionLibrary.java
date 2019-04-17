/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/4/29
 */
@Named
public class HR_SaveAfentiWrongQuestionLibrary extends SpringContainerSupport implements HomeworkResultTask {
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;

    @Inject private AfentiQueueProducer afentiQueueProducer;

    @Override
    public void execute(HomeworkResultContext context) {
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.READING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType())
				|| ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.ORAL_COMMUNICATION.equals(context.getObjectiveConfigType())
				|| ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(context.getObjectiveConfigType())
				|| ObjectiveConfigType.OCR_DICTATION.equals(context.getObjectiveConfigType())
                ) {
            return;
        }
        Map<String, NewHomeworkProcessResult> resultMap = context.getProcessResult();
        for (NewHomeworkProcessResult result : resultMap.values()) {
            if (result.getGrasp()) return; // 如果做对了，return

            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(result.getQuestionId());
            if (question == null) return;

            // TODO: 2016/4/29 如果题型不支持 return
            List<Integer> subContentTypeIds = question.findSubContentTypeIds();
            if (questionContentTypeLoaderClient.isSubjective(subContentTypeIds) || questionContentTypeLoaderClient.isOral(subContentTypeIds)) {
                return;
            }

            Date currentDate = new Date();
            String id = WrongQuestionLibrary.generateId(context.getUserId(), context.getSubject(), result.getQuestionId());

            WrongQuestionLibrary wrongQuestionLibrary = new WrongQuestionLibrary();
            wrongQuestionLibrary.setId(id);
            wrongQuestionLibrary.setDisabled(false);
            wrongQuestionLibrary.setState(INCORRECT);
            wrongQuestionLibrary.setCreateAt(currentDate);
            wrongQuestionLibrary.setUpdateAt(currentDate);
            wrongQuestionLibrary.setUserId(context.getUserId());
            wrongQuestionLibrary.setEid(result.getQuestionId());
            wrongQuestionLibrary.setSource(context.getLearningType().name());
            wrongQuestionLibrary.setSubject(context.getSubject());

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("T", AfentiQueueMessageType.WRONG_QUESTION_LIBRARY);
            message.put("WQL", wrongQuestionLibrary);
            afentiQueueProducer.getProducer().produce(Message.newMessage().withStringBody(JsonUtils.toJson(message)));
        }
    }
}
