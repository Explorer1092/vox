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

package com.voxlearning.utopia.business.api.constant;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory.PrimaryChineseStudent;
import static com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory.PrimaryEnglishStudent;


public enum ActivitySchoolLevel implements IActivityLevel {

    PrimaryEnglishLevelA(PrimaryEnglishStudent.getId(), "A", 20, 30, 60),
    PrimaryEnglishLevelB(PrimaryEnglishStudent.getId(), "B", 30, 60, 90),
    PrimaryEnglishLevelC(PrimaryEnglishStudent.getId(), "C", 60, 120, Integer.MAX_VALUE),
    PrimaryEnglishLevelD(PrimaryEnglishStudent.getId(), "D", 60, Integer.MAX_VALUE, Integer.MAX_VALUE),

    PrimaryChineseLevelA(PrimaryChineseStudent.getId(), "A", 15, 30, 45),
    PrimaryChineseLevelB(PrimaryChineseStudent.getId(), "B", 20, 40, 60),
    PrimaryChineseLevelC(PrimaryChineseStudent.getId(), "C", 30, 60, 90),;

    @Getter private Long activityId;            // 活动ID
    @Getter private final String level;         // S/A/B/C/D
    @Getter private final int rewardLevel1;     // 奖励档1人数
    @Getter private final int rewardLevel2;     // 奖励档2人数
    @Getter private final int rewardLevel3;     // 奖励档3人数

    ActivitySchoolLevel(Long activityId, String level, int rewardLevel1, int rewardLevel2, int rewardLevel3) {
        this.activityId = activityId;
        this.level = level;
        this.rewardLevel1 = rewardLevel1;
        this.rewardLevel2 = rewardLevel2;
        this.rewardLevel3 = rewardLevel3;
    }

    @Override
    public int rewardLevel1() {
        return rewardLevel1;
    }

    @Override
    public int rewardLevel2() {
        return rewardLevel2;
    }

    @Override
    public int rewardLevel3() {
        return rewardLevel3;
    }

    @Override
    public String fetchLevel() {
        return level;
    }


    private static Map<Long, List<ActivitySchoolLevel>> activityLevelMap;

    static {
        activityLevelMap = Stream.of(values())
                .collect(Collectors.groupingBy(ActivitySchoolLevel::getActivityId));
    }

    public static List<ActivitySchoolLevel> findLevelByActivity(Long activityId) {
        return activityLevelMap.getOrDefault(activityId, Collections.emptyList());
    }

    public static ActivitySchoolLevel loadActivitySchoolLevel(Long activityId, String level) {
        return activityLevelMap.getOrDefault(activityId, Collections.emptyList())
                .stream()
                .filter(config -> StringUtils.equals(config.getLevel(), level))
                .findFirst()
                .orElse(null);
    }
}
