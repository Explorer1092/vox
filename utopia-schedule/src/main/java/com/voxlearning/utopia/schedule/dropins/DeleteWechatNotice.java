/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.dropins;

import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.schedule.support.AbstractSweeperTask;
import com.voxlearning.utopia.schedule.support.SweeperTask;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeUpdateAction;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeUpdater;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import java.util.Map;

/**
 * 删除1周之前的微信消息数据
 * Created by Shuai Huan on 2014/12/2.
 */
@SweeperTask
public class DeleteWechatNotice extends AbstractSweeperTask {

    @Override
    public void execute(Map<String, Object> beans) {
        WechatServiceClient wechatServiceClient;
        try {
            wechatServiceClient = applicationContext.getBean(WechatServiceClient.class);
        } catch (Exception ex) {
            wechatServiceClient = null;
        }

        if (wechatServiceClient != null) {
            WechatNoticeUpdater message = new WechatNoticeUpdater();
            message.setAction(WechatNoticeUpdateAction.SCHEDULE__DELETE_WECHAT_NOTICE);
            String json = message.serialize();
            Message msg = Message.newMessage().withStringBody(json);
            wechatServiceClient.getWechatService().sendMessage(msg);
        }
    }

}
