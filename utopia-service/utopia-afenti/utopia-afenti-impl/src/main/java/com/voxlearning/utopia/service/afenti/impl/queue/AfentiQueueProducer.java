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

package com.voxlearning.utopia.service.afenti.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer")
public class AfentiQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.afenti.queue")
    private MessageProducer producer;

    @Getter
    @AlpsQueueProducer(queue = "utopia.afenti.login.queue")
    private MessageProducer loginProducer;

    @Getter
    @AlpsQueueProducer(queue = "utopia.afenti2.queue")
    private MessageProducer producer2;
}