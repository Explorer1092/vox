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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.MagicValueType;
import com.voxlearning.utopia.api.constant.MagicWaterType;
import com.voxlearning.utopia.entity.activity.StudentMagicCastleRecord;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer Yang on 2015/12/2.
 */
@ServiceVersion(version = "3.0.STABLE")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface StudentMagicCastleService extends IPingable {
    // 获取魔法师等级
    StudentMagicLevel loadStudentMagicLevel(Long studentId);

    boolean hasBindParentApp(Long studentId);

    List<Map<String, Object>> loadCurrentWeekMagicValueRank(StudentDetail detail);

    List<Map<String, Object>> loadCurrentWeekActiveRank(StudentDetail detail);

    List<ActivateInfoMapper> loadClazzSleepMagicianList(Long studentId, Long clazzId);

    Map<String, Object> loadStudentActiveDetailInfo(Long studentId);

    MapMessage activeMagician(Long magicianId, Long activeId, Integer activeLevel, StudentMagicCastleRecord.Source source);

    Integer loadStudentMagicWater(Long studentId);

    void activeMagicianSuccess(Long activeId);

    void addMagicValue(Long magicianId, MagicValueType magicValueType);

    void addMagicWater(Long studentId, MagicWaterType magicWaterType);

    List<ActivateInfoMapper> loadSuperMagician(StudentDetail detail, Integer level);

    Map<String, Object> loadMaxMagician(StudentDetail detail);

    List<Map<String, Object>> loadTotalMagicValueRank(StudentDetail detail);
}
