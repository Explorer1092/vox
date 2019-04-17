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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.DPPushService;
import com.voxlearning.utopia.service.vendor.api.entity.Message;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@ExposeService(interfaceClass = DPPushService.class)
public class DPPushServiceImpl implements DPPushService {

    //local variables
    @Inject private AppMessageServiceImpl appMessageService;

    //Logic
    /**
     * push消息
     *
     * @param source   来源，see {@link AppMessageSource}
     * @param messages 消息
     */
    @Override
    public void push(String source, List<Message> messages) {
        messages.forEach(m->
                appMessageService.sendAppJpushMessageByIds(m.getContent(), AppMessageSource.of(source), Lists.newArrayList(m.getUserIds()), m.getExtInfo())
        );
    }

}
