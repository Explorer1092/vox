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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;

import javax.inject.Named;

/**
 * 成就：完成作业次数
 * 成长：5次/学科/周；1次/学科/天 成长+2
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
@Named("actionEventHandler.finishHomework")
public class FinishHomework extends AbstractActionEventHandler {

    @Override
    public ActionEventType getEventType() {
        return ActionEventType.FinishHomework;
    }

    @Override
    public void handle(ActionEvent event) {
        try {
            addAndGet(event.getUserId(), event.getType(), 1);
            int homeworkScore = SafeConverter.toInt(event.getAttributes().get("homeworkScore"));
            if (homeworkScore >= 90) {
                addAndGet(event.getUserId(), ActionEventType.HomeworkScore90, 1);
            }

            String subjectName = SafeConverter.toString(event.getAttributes().get("homeworkSubject"));
            Subject subject = Subject.safeParse(subjectName);
            if (subject == null)
                return;
            long dc = actionEventDayRangeCounter.increase(event, subject);
            if (dc == 0 || dc > 1) {
                return;
            }
            long wc = actionEventWeekRangeCounter.increase(event, subject);
            if (wc == 0 || wc > 5) {
                return;
            }
            addGrowth(event);
        } catch (Exception ex) {
//            logger.error("FinishHomework action error," + ex.getMessage() + "," + JsonUtils.toJson(event), ex);
            throw ex;
        }
    }
}
