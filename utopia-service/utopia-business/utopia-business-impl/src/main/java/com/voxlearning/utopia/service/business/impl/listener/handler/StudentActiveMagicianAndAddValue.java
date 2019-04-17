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

package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.MagicValueType;
import com.voxlearning.utopia.api.constant.MagicWaterType;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.business.impl.listener.BusinessEventHandler;
import com.voxlearning.utopia.service.business.impl.service.student.StudentMagicCastleServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class StudentActiveMagicianAndAddValue implements BusinessEventHandler {

    @Inject
    private StudentMagicCastleServiceImpl studentMagicCastleService;

    @Override
    public BusinessEventType getEventType() {
        return BusinessEventType.STUDENT_ACTIVE_MAGICIAN_AND_ADD_VALUE;
    }

    @Override
    public void handle(BusinessEvent event) {
        if (event == null) return;
        if (event.getAttributes() == null) return;
        long studentId = SafeConverter.toLong(event.getAttributes().get("studentId"));
        if (studentId == 0) return;
        // 唤醒魔法师
        studentMagicCastleService.activeMagicianSuccess(studentId);
        // 添加魔力值
        studentMagicCastleService.addMagicValue(studentId, MagicValueType.FINISH_HOMEWORK);
        // 添加魔法药水
        studentMagicCastleService.addMagicWater(studentId, MagicWaterType.FINISH_HOMEWORK);
    }
}
