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

package com.voxlearning.utopia.service.push.impl;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.service.push.api.PushMessageService;
import com.voxlearning.utopia.service.push.api.model.Message;
import com.voxlearning.utopia.service.push.api.model.TagMessage;
import com.voxlearning.utopia.service.push.api.model.UserMessage;

import javax.inject.Named;
import java.util.List;
import java.util.Set;

import static com.voxlearning.utopia.service.push.impl.jiguang.JGConstants.PUSH_COUNT;

/**
 * push服务实现
 *
 * @author Wenlong Meng
 * @since Mar 13, 2019
 */
@Named
@ExposeService(interfaceClass = PushMessageService.class)
public class PushMessageServiceImpl implements PushMessageService {

    //local variables

    //Logic
    /**
     * push消息
     *
     * @param messages 消息
     */
    @Override
    public void push(List<Message> messages) {
        messages.forEach(m->{
                if(m instanceof UserMessage){
                    send((UserMessage) m);
                }else if(m instanceof TagMessage){
                    send((TagMessage) m);
                }
        });
    }

    /**
     * 发送标签消息
     *
     * @param message
     */
    private void send(TagMessage message) {
        message.getTags().forEach(m->
            MQUtils.send("utopia.push.message.queue", MapUtils.m("source", message.getSource(),
                "data", m,
                "command", "tagMessage")));

    }

    /**
     * 发送用户消息
     *
     * @param message
     */
    private void send(UserMessage message) {
        Lists.partition(Lists.newArrayList(message.getUserIds()), PUSH_COUNT).stream().forEach(ids->{
            MQUtils.send("utopia.push.message.queue", MapUtils.m("source", message.getSource(),
                "data", ids,
                "command", "userMessage"));
        });
    }

    /**
     * 删除用户与设备绑定关系
     *
     * @param source
     * @param userIds
     */
    @Override
    public void deleteUsers(String source, Set<Long> userIds) {
        userIds.forEach( id->
            MQUtils.send("utopia.push.device.queue", MapUtils.m("source", source,
                    "data", id,
                    "command", "deleteUsers"))
        );

    }

    /**
     * 删除标签与设备绑定关系
     *
     * @param source
     * @param tags
     */
    @Override
    public void deleteTags(String source, Set<String> tags) {
        tags.forEach(tag->
                MQUtils.send("utopia.push.device.queue", MapUtils.m("source", source,
                        "data", tag,
                        "command", "deleteTags"))
        );
    }
}
