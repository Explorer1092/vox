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
 * 成就：自学天数
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
@Named("actionEventHandler.finishSelfLearning")
public class FinishSelfLearning extends AbstractActionEventHandler {
    @Override
    public ActionEventType getEventType() {
        return ActionEventType.FinishSelfLearning;
    }

    @Override
    public void handle(ActionEvent event) {
        // how many FinishSelfLearning within today?
        long dc = actionEventDayRangeCounter.increase(event);
        if (dc == 1) {
            addAndGet(event.getUserId(), event.getType(), 1);
        }

        //只用作自学天数。成长值需细分
//        if (dc == 0 || dc > 1) {
//            return;
//        }
//        // how many FinishSelfLearning within current week?
//        long wc = actionEventWeekRangeCounter.increase(event);
//        if (wc == 0 || wc > 7) {
//            return;
//        }
//        int delta = 5;
//        userGrowthDao.addAndGet(event.getUserId(), delta);
//
//        UserGrowthLog log = new UserGrowthLog();
//        log.setUserId(event.getUserId());
//        log.setActionTime(new Date(event.getTimestamp()));
//        log.setType(event.getType());
//        log.setDelta(delta);
//        ensureId(log);
//        userGrowthLogDao.insert(log);
    }
}
