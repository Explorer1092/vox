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

package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.impl.push.HomeworkMessageProcessorManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author xinxin
 * @since 28/7/2016
 */
@Named
public class JpushHomeworkQueueHandler extends SpringContainerSupport {
    @Inject
    private HomeworkMessageProcessorManager homeworkMessageProcessorManager;

    public void handleMessage(String messageText) {
        if (StringUtils.isBlank(messageText)) {
            return;
        }

        Map<String, Object> messageMap = JsonUtils.fromJson(messageText);

        process(messageMap);
    }

    private void process(Map<String, Object> messageMap) {
        String type = (String) messageMap.get("type"); //type值是约定的,要改两边都改


        AppMessageSource source = AppMessageSource.of(type);
        if (source == AppMessageSource.UNKNOWN) {
            return;
        }

        homeworkMessageProcessorManager.get(source).process(messageMap);
    }
}
