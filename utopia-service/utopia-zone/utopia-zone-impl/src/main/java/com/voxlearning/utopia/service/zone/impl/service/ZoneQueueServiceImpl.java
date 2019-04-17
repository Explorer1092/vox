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

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.zone.api.ZoneQueueService;
import com.voxlearning.utopia.service.zone.impl.queue.ZoneQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.zone.impl.service.ZoneQueueServiceImpl")
@ExposeService(interfaceClass = ZoneQueueService.class)
public class ZoneQueueServiceImpl implements ZoneQueueService {

    @Inject private ZoneQueueProducer zoneQueueProducer;

    @Override
    public void sendMessage(Message message) {
        if (message != null) {
            zoneQueueProducer.getProducer().produce(message);
        }
    }
}
