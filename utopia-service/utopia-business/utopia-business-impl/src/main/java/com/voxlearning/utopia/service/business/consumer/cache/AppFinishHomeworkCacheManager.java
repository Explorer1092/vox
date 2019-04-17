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

package com.voxlearning.utopia.service.business.consumer.cache;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/7/29.
 *
 * @deprecated 当前代码中无调用栈了
 */
@Deprecated
public class AppFinishHomeworkCacheManager extends PojoCacheObject<AppFinishHomeworkCacheManager.TeacherWithClazzId, List<Map<String, Object>>> {
    public AppFinishHomeworkCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean record(Long teacherId, Clazz clazz, String studentName, Long studentId) {
        if (teacherId == null || clazz == null || StringUtils.isBlank(studentName) || studentId == null) {
            return false;
        }
        Map<String, Object> map = MiscUtils.m("studentName", studentName, "studentId", studentId);
        String key = cacheKey(new TeacherWithClazzId(teacherId, clazz.getId()));
        CacheObject<List<Map<String, Object>>> cacheObject = cache.get(key);
        if (cacheObject == null || CollectionUtils.isEmpty(cacheObject.getValue())) {
            List<Map<String, Object>> dataList = new ArrayList<>();
            dataList.add(map);
            cache.set(key, expirationInSeconds(), dataList);
            return true;
        } else {
            List<Map<String, Object>> dataList = cacheObject.getValue().stream()
                    .filter(m -> Objects.equals(ConversionUtils.toLong(m.get("studentId")), studentId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dataList)) {
                cache.cas(key, expirationInSeconds(), cacheObject, currentValue -> {
                    currentValue = new ArrayList<>(currentValue);
                    currentValue.add(map);
                    return currentValue;
                });
                return true;
            } else {
                return false;
            }
        }
    }

    public Map<Clazz, List<Map<String, Object>>> loadByTeacherIdAndClazzIds(Long teacherId, List<Clazz> clazzs) {
        if (teacherId == null || CollectionUtils.isEmpty(clazzs)) {
            return Collections.emptyMap();
        }
        Map<Clazz, List<Map<String, Object>>> data = new HashMap<>();
        for (Clazz clazz : clazzs) {
            String key = cacheKey(new TeacherWithClazzId(teacherId, clazz.getId()));
            CacheObject<List<Map<String, Object>>> cacheObject = cache.get(key);
            if (cacheObject != null && cacheObject.getValue() != null) {
                data.put(clazz, cacheObject.getValue());
            }
        }
        return data;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherWithClazzId {
        public Long teacherId;
        public Long clazzId;

        @Override
        public String toString() {
            return "T=" + teacherId + ",C=" + clazzId;
        }
    }

    @Override
    public int expirationInSeconds() {
        return (int) (DateUtils.stringToDate("2015-09-20 23:59:59").getTime() / 1000);
    }
}