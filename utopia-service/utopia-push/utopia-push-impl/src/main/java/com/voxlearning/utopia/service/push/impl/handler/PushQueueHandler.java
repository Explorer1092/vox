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

package com.voxlearning.utopia.service.push.impl.handler;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.push.impl.support.PushHandHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * jpush queue handler
 * Created by Shuai Huan on 2016/3/7.
 */
@Named
public class PushQueueHandler extends SpringContainerSupport implements QueueHandLer {

    @Inject private PushHandHelper pushHandHelper;

    @Override
    public void handleMessage(String messageText) {
        Map<String, Object> messageMap = JsonUtils.fromJson(messageText);
        if (messageMap == null) {
            logger.warn("Ignore unrecognized notify message: {}", messageText);
            return;
        }

        pushHandHelper.sendPushNotify(messageMap);
    }

}
