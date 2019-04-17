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

package com.voxlearning.utopia.service.action.api.document;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The builder of {@link Achievement}.
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
@Slf4j
public class AchievementBuilder {

    public static final EnumMap<ActionEventType, AchievementType> titles;
    private static final EnumMap<ActionEventType, LinkedHashMap<Integer, Integer>> pools;

    static {
        titles = new EnumMap<>(ActionEventType.class);
        titles.put(ActionEventType.WakeupClassmate, AchievementType.HuanXingShi);
        titles.put(ActionEventType.FinishSelfLearning, AchievementType.ZiXueChengCai);
//        titles.put(ActionEventType.ObtainStar, AchievementType.XingGuangCuiCan);
//        titles.put(ActionEventType.CorrectWrongIssue, AchievementType.ShiQueBuYi);
//        titles.put(ActionEventType.WinPk, AchievementType.YongWangZhiQian);
        titles.put(ActionEventType.FinishHomework, AchievementType.QinXueKuLian);
        titles.put(ActionEventType.FinishMental, AchievementType.ShenSuanZi);
        titles.put(ActionEventType.FinishOral, AchievementType.JinHuaTong);
        titles.put(ActionEventType.FinishReading, AchievementType.YueDuDaKa);
        titles.put(ActionEventType.HomeworkScore90, AchievementType.XueYouSuoCheng);
//        titles.put(ActionEventType.CorrectHomework, AchievementType.YouCuoBiJiu);

        pools = new EnumMap<>(ActionEventType.class);
        LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
        map.put(400, 10);
        map.put(300, 9);
        map.put(200, 8);
        map.put(100, 7);
        map.put(70, 6);
        map.put(50, 5);
        map.put(20, 4);
        map.put(10, 3);
        map.put(5, 2);
        map.put(1, 1);
        map.put(0, 0);
        pools.put(ActionEventType.WakeupClassmate, map);

        map = new LinkedHashMap<>();
        map.put(2560, 10);
        map.put(1280, 9);
        map.put(640, 8);
        map.put(320, 7);
        map.put(160, 6);
        map.put(80, 5);
        map.put(40, 4);
        map.put(20, 3);
        map.put(8, 2);
        map.put(2, 1);
        map.put(0, 0);
        pools.put(ActionEventType.FinishSelfLearning, map);

//        map = new LinkedHashMap<>();
//        map.put(4400, 10);
//        map.put(3300, 9);
//        map.put(2200, 8);
//        map.put(1100, 7);
//        map.put(540, 6);
//        map.put(270, 5);
//        map.put(100, 4);
//        map.put(50, 3);
//        map.put(30, 2);
//        map.put(10, 1);
//        map.put(0, 0);
//        pools.put(ActionEventType.ObtainStar, map);
//
//        map = new LinkedHashMap<>();
//        map.put(460, 10);
//        map.put(370, 9);
//        map.put(290, 8);
//        map.put(220, 7);
//        map.put(160, 6);
//        map.put(110, 5);
//        map.put(70, 4);
//        map.put(40, 3);
//        map.put(20, 2);
//        map.put(10, 1);
//        map.put(0, 0);
//        pools.put(ActionEventType.CorrectWrongIssue, map);

        map = new LinkedHashMap<>();
        map.put(10, 10);
        map.put(9, 9);
        map.put(8, 8);
        map.put(7, 7);
        map.put(6, 6);
        map.put(5, 5);
        map.put(4, 4);
        map.put(3, 3);
        map.put(2, 2);
        map.put(1, 1);
        map.put(0, 0);
//        pools.put(ActionEventType.WinPk, map);

        map = new LinkedHashMap<>();
        map.put(800, 10);
        map.put(640, 9);
        map.put(480, 8);
        map.put(320, 7);
        map.put(160, 6);
        map.put(100, 5);
        map.put(60, 4);
        map.put(30, 3);
        map.put(15, 2);
        map.put(5, 1);
        map.put(0, 0);
        pools.put(ActionEventType.FinishHomework, map);

        map = new LinkedHashMap<>();
        map.put(800, 10);
        map.put(640, 9);
        map.put(480, 8);
        map.put(320, 7);
        map.put(160, 6);
        map.put(100, 5);
        map.put(60, 4);
        map.put(30, 3);
        map.put(15, 2);
        map.put(5, 1);
        map.put(0, 0);
        pools.put(ActionEventType.HomeworkScore90, map);

        map = new LinkedHashMap<>();
        map.put(8000, 10);
        map.put(6400, 9);
        map.put(4800, 8);
        map.put(3200, 7);
        map.put(1600, 6);
        map.put(1000, 5);
        map.put(600, 4);
        map.put(300, 3);
        map.put(150, 2);
        map.put(50, 1);
        map.put(0, 0);
        pools.put(ActionEventType.FinishMental, map);

        map = new LinkedHashMap<>();
        map.put(800, 10);
        map.put(640, 9);
        map.put(480, 8);
        map.put(320, 7);
        map.put(160, 6);
        map.put(100, 5);
        map.put(60, 4);
        map.put(30, 3);
        map.put(15, 2);
        map.put(5, 1);
        map.put(0, 0);
        pools.put(ActionEventType.FinishOral, map);

        map = new LinkedHashMap<>();
        map.put(800, 10);
        map.put(640, 9);
        map.put(480, 8);
        map.put(320, 7);
        map.put(160, 6);
        map.put(100, 5);
        map.put(60, 4);
        map.put(30, 3);
        map.put(15, 2);
        map.put(5, 1);
        map.put(0, 0);
        pools.put(ActionEventType.FinishReading, map);

        map = new LinkedHashMap<>();
        map.put(800, 10);
        map.put(640, 9);
        map.put(480, 8);
        map.put(320, 7);
        map.put(160, 6);
        map.put(100, 5);
        map.put(60, 4);
        map.put(30, 3);
        map.put(15, 2);
        map.put(5, 1);
        map.put(0, 0);
        pools.put(ActionEventType.CorrectHomework, map);
    }

    public static Achievement build(UserAchievementRecord record) {
        try {
            ActionEventType aet = ActionEventType.valueOf(record.parse().getAet());
            return build(record, aet);
        } catch (Exception e) {
            log.error("Failed to build user achievement, id:{}", record.getId(), e);
            return null;
        }
    }

    public static Achievement build(UserAchievementRecord record, ActionEventType type) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(type);

        Achievement achievement = new Achievement();
        if (titles.containsKey(type)) {
            achievement.setTitle(titles.get(type).getTitle());
            achievement.setType(titles.get(type));
        } else {
            achievement.setTitle("");
            achievement.setType(null);
        }

        if (record.getScore() == null) {
            return achievement;
        }

        LinkedHashMap<Integer, Integer> pool = pools.get(type);
        if (pool == null) {
            return achievement;
        }

        int value = record.getScore();
        for (Map.Entry<Integer, Integer> entry : pool.entrySet()) {
            if (value >= entry.getKey()) {
                achievement.setRank(entry.getValue());
                break;
            }
        }
        return achievement;
    }

    //获取下一等级需要的成就值
    public static Integer next(ActionEventType type, int currentLevel) {
        LinkedHashMap<Integer, Integer> map = pools.get(type);
        if (null == map) return 0;

        if (!map.containsValue(currentLevel)) return 0;
        if (!map.containsValue(currentLevel + 1)) return 0;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() == currentLevel + 1) {
                return entry.getKey();
            }
        }

        return 0;
    }

    //获取下一等级需要的成就值
    public static Integer next(AchievementType type, int currentLevel) {
        for (EnumMap.Entry<ActionEventType, AchievementType> entry : titles.entrySet()) {
            if (entry.getValue() == type) {
                return next(entry.getKey(), currentLevel);
            }
        }

        return 0;
    }

    //获取成就等级数量
    public static int levelCount(AchievementType type) {
        for (Map.Entry<ActionEventType, AchievementType> entry : titles.entrySet()) {
            if (entry.getValue() == type) {
                if (!pools.containsKey(entry.getKey())) return 0;

                if (MapUtils.isEmpty(pools.get(entry.getKey()))) return 0;

                return pools.get(entry.getKey()).size() - 1;
            }
        }

        return 0;
    }
}
