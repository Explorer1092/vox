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

package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.impl.dao.TotalAssignmentRecordDao;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.UpdateTotalAssignmentRecordCommand;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Set;

@Named
public class UpdateTotalAssignmentRecordCommandHandler {

    @Inject private TotalAssignmentRecordDao totalAssignmentRecordDao;

    public void handle(UpdateTotalAssignmentRecordCommand command) {
        if (command == null) {
            return;
        }
        Subject subject = command.getSubject();
        Integer clazzGroupSize = command.getClazzGroupSize();
        Map<String, Integer> questionMap = command.getQuestionMap();
        Set<String> packageSet = command.getPackageSet();
        Set<String> paperSet = command.getPaperSet();
        totalAssignmentRecordDao.updateTotalAssignmentRecord(subject, clazzGroupSize, questionMap, packageSet, paperSet);
    }
}
