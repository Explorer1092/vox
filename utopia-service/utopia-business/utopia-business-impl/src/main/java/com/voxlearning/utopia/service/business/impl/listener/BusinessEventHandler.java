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

package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;

/**
 * The handler abstraction of {@link BusinessEvent}.
 *
 * @author Xiaohai Zhang
 * @since Aug 2, 2016
 */
public interface BusinessEventHandler {

    BusinessEventType getEventType();

    void handle(BusinessEvent event);

    BusinessEventHandler NOP = new BusinessEventHandler() {
        @Override
        public BusinessEventType getEventType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(BusinessEvent event) {
        }
    };
}
