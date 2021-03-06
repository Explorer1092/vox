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

import com.voxlearning.utopia.schedule.support.AbstractSweeperTask;
import com.voxlearning.utopia.schedule.support.SweeperTask;
import com.voxlearning.utopia.service.user.api.queue.UserCommand;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import java.util.Map;

/**
 * @author RuiBao
 * @since 11/6/2015
 */
@SweeperTask
public class DeleteTeacherLatest extends AbstractSweeperTask {

    @Override
    public void execute(Map<String, Object> beans) {
        UserServiceClient userServiceClient = applicationContext.getBean(UserServiceClient.class);
        userServiceClient.sendCommand(UserCommand.DeleteTeacherLatest);
    }
}
