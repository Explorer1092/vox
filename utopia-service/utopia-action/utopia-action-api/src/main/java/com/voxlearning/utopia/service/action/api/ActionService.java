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

package com.voxlearning.utopia.service.action.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;

import java.util.concurrent.TimeUnit;

/**
 * Action service abstraction.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@ServiceVersion(version = "2016.12.15")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ActionService extends IPingable {

    @NoResponseWait
    void sendEvent(ActionEvent event);

    @NoResponseWait
    void handleActionEvent(ActionEvent event);

    @Async
    AlpsFuture<Long> increaseFinishSelfLearningCount(Long userId);

    @Async
    AlpsFuture<Long> decreaseDayRangeCount(Long userId, ActionEventType type, String day);
}
