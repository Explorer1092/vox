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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.vendor.api.entity.Message;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * push 消息服务
 *
 * @author Wenlong Meng
 * @since Feb 26, 2019
 */
@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries=1)
public interface DPPushService {

    /**
     * push消息
     *
     * @param source 来源，see {@link AppMessageSource}
     * @param messages 消息
     */
    void push(String source, List<Message> messages);

}
