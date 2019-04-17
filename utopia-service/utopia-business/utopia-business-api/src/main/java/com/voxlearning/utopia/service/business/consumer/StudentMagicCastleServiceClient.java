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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.MagicValueType;
import com.voxlearning.utopia.api.constant.MagicWaterType;
import com.voxlearning.utopia.business.api.StudentMagicCastleService;
import com.voxlearning.utopia.entity.activity.StudentMagicCastleRecord;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.business.cache.BusinessCache;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2015/12/2.
 */
public class StudentMagicCastleServiceClient implements StudentMagicCastleService {

    @ImportService(interfaceClass = StudentMagicCastleService.class)
    private StudentMagicCastleService remoteReference;

    @Override
    public StudentMagicLevel loadStudentMagicLevel(Long studentId) {
        if (studentId == null) {
            return null;
        }
        String key = CacheKeyGenerator.generateCacheKey(StudentMagicLevel.class, "magicianId", studentId);
        StudentMagicLevel magicLevel = BusinessCache.getBusinessCache().load(key);
        if (magicLevel != null) {
            return magicLevel;
        } else {
            return remoteReference.loadStudentMagicLevel(studentId);
        }
    }

    @Override
    public boolean hasBindParentApp(Long studentId) {
        return remoteReference.hasBindParentApp(studentId);
    }

    @Override
    public List<Map<String, Object>> loadCurrentWeekMagicValueRank(StudentDetail detail) {
        return remoteReference.loadCurrentWeekMagicValueRank(detail);
    }

    @Override
    public List<Map<String, Object>> loadCurrentWeekActiveRank(StudentDetail detail) {
        return remoteReference.loadCurrentWeekActiveRank(detail);
    }

    @Override
    public List<ActivateInfoMapper> loadClazzSleepMagicianList(Long studentId, Long clazzId) {
        return remoteReference.loadClazzSleepMagicianList(studentId, clazzId);
    }

    @Override
    public Map<String, Object> loadStudentActiveDetailInfo(Long studentId) {
        return remoteReference.loadStudentActiveDetailInfo(studentId);
    }

    @Override
    public MapMessage activeMagician(Long magicianId, Long activeId, Integer activeLevel, StudentMagicCastleRecord.Source source) {
        return remoteReference.activeMagician(magicianId, activeId, activeLevel, source);
    }

    @Override
    public Integer loadStudentMagicWater(Long studentId) {
        return remoteReference.loadStudentMagicWater(studentId);
    }

    @Override
    public void activeMagicianSuccess(Long activeId) {
        remoteReference.activeMagicianSuccess(activeId);
    }

    @Override
    public void addMagicValue(Long magicianId, MagicValueType magicValueType) {
        remoteReference.addMagicValue(magicianId, magicValueType);
    }

    @Override
    public void addMagicWater(Long studentId, MagicWaterType magicWaterType) {
        remoteReference.addMagicWater(studentId, magicWaterType);
    }

    @Override
    public List<ActivateInfoMapper> loadSuperMagician(StudentDetail detail, Integer level) {
        return remoteReference.loadSuperMagician(detail, level);
    }

    @Override
    public Map<String, Object> loadMaxMagician(StudentDetail detail) {
        return remoteReference.loadMaxMagician(detail);
    }

    @Override
    public List<Map<String, Object>> loadTotalMagicValueRank(StudentDetail detail) {
        return remoteReference.loadTotalMagicValueRank(detail);
    }

}
