/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newexam.impl.queue;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/3/8
 */
@Named("com.voxlearning.utopia.service.newexam.impl.queue.NewExamQueueProducer")
public class NewExamQueueProducer extends SpringContainerSupport {

    @Getter
    @AlpsQueueProducer(queue = "utopia.newexam.queue")
    private MessageProducer producer;

    public void sendSaveResultMessage(NewExamProcessResult newExamProcessResult) {
        String messageText = JsonUtils.toJson(newExamProcessResult);
        Message message = Message.newMessage().withStringBody(messageText);
        producer.produce(message);
    }

    @Getter
    @AlpsQueueProducer(queue = "utopia.newexam.student.viewreport.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer studentViewReport;
}
