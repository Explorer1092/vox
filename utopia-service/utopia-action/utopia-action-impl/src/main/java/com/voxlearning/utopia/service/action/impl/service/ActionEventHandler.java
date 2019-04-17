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

package com.voxlearning.utopia.service.action.impl.service;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;

/**
 * The handler of {@link ActionEvent}.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
public interface ActionEventHandler {

    ActionEventType getEventType();

    void handle(ActionEvent event);

    ActionEventHandler NOP = new ActionEventHandler() {
        @Override
        public ActionEventType getEventType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(ActionEvent event) {
            throw new IllegalStateException("Illegal event type,"+ JsonUtils.toJson(event));
        }
    };
}
