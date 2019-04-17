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

package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;

/**
 * 成就：唤醒
 * 成长：无
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
@Named("actionEventHandler.wakeupClassmate")
public class WakeupClassmate extends AbstractActionEventHandler {
    @Override
    public ActionEventType getEventType() {
        return ActionEventType.WakeupClassmate;
    }

    @Override
    public void handle(ActionEvent event) {
        // 每成功唤醒同学一次，成就+1
        addAndGet(event.getUserId(), event.getType(), 1);
    }

}
