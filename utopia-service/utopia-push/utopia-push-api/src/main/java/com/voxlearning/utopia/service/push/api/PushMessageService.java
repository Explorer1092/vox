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

package com.voxlearning.utopia.service.push.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.push.api.model.Message;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * push 消息服务
 *
 * @author Wenlong Meng
 * @since Mar 13, 2019
 */
@ServiceVersion(version = "20190606")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries=1)
public interface PushMessageService {

    /**
     * push消息
     *
     * @param message 消息
     */
    default void push(Message message){
       this.push(Collections.singletonList(message));
    }

    /**
     * push消息
     *
     * @param messages 消息
     */
    void push(List<Message> messages);

    /**
     * 删除用户与设备绑定关系
     *
     * @param source
     * @param userIds
     */
    void deleteUsers(String source, Set<Long> userIds);

    /**
     * 删除标签与设备绑定关系
     *
     * @param source
     * @param tags
     */
    void deleteTags(String source, Set<String> tags);

}
