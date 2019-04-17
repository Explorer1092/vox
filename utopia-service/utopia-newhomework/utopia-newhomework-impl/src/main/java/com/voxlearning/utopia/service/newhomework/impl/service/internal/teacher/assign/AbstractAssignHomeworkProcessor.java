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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
abstract public class AbstractAssignHomeworkProcessor extends NewHomeworkSpringBean {

    final public AssignHomeworkContext process(AssignHomeworkContext context) {
        if (context == null) {
            return null;
        }
        FlightRecorder.dot(getClass().getSimpleName());
        try {
            doProcess(context);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            context.errorResponse("布置作业失败");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
        }
        return context;
    }

    abstract protected void doProcess(AssignHomeworkContext context);
}
