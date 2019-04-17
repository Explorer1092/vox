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

package com.voxlearning.utopia.business.api.mapper;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class ActivitySchoolLevelMap implements Serializable {
    private static final long serialVersionUID = 4466401362424036917L;

    public static final String schoolLevelMapDir = "/activity/{}/school_level_map";
    public static final String schoolLevelBlackListDir = "/activity/{}/school_level_black_list";

    private Long activityId;
    private String defaultLevel;
    private Map<Long, String> schoolLevelMap;
    private List<Long> blackList;

    public ActivitySchoolLevelMap(Long activityId, String defaultLevel) {
        this.activityId = activityId;
        this.defaultLevel = defaultLevel;
        schoolLevelMap = new HashMap<>();
        blackList = new LinkedList<>();
    }

    public String genMapFile() {
        return StringUtils.formatMessage(schoolLevelMapDir, activityId);
    }

    public String genBalckListFile() {
        return StringUtils.formatMessage(schoolLevelBlackListDir, activityId);
    }

    public void appendSchoolMap(Long schoolId, String level) {
        schoolLevelMap.put(schoolId, level);
    }

    public void appendBlackList(Long schoolId) {
        blackList.add(schoolId);
    }

    public boolean checkBlackList(Long schoolId) {
        return blackList.contains(schoolId);
    }

    public String getSchoolLevel(Long schoolId) {
        return schoolLevelMap.getOrDefault(schoolId, defaultLevel);
    }
}
