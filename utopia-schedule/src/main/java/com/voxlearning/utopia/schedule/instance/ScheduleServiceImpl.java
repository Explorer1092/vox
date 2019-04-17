/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.instance;

import com.alibaba.dubbo.config.annotation.Service;

import javax.inject.Named;

/**
 * Default {@link ScheduleService} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 5, 2015
 */
@Named
@Service(interfaceClass = ScheduleService.class)
public class ScheduleServiceImpl implements ScheduleService {
    @Override
    public String ping() {
        return STATUS_OK;
    }
}
