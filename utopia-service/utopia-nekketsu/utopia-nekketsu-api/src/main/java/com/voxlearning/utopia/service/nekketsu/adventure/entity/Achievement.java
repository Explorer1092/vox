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

package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 成就
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/15 14:51
 */
@Getter
@Setter
public class Achievement implements Serializable {
    private static final long serialVersionUID = -2459433192660449826L;

    private Integer order;//顺序
    private Integer totalCount;//总数量，
    private Integer nextLevelCount;//下次升级需要累积到的数量
    private Integer nextReceiveLevelCount;//未领取的进度
    private Integer beans;    //成就的学豆奖励
    private Integer pkVitality;   //成就的PK活力奖励
    private AchievementType achievementType;//成就类型

    public void increaseTotalCount(Integer count) {
        this.setTotalCount(this.getTotalCount() + count);
    }

    public static Map<AchievementType, Achievement> newAchievements() {
        Map<AchievementType, Achievement> map = new LinkedHashMap<>();
        for (AchievementType type : AchievementType.values()) {
            Achievement achievement = new Achievement();
            achievement.setTotalCount(0);
            achievement.setAchievementType(type);
            achievement.setOrder(type.getOrder());
            achievement.setNextLevelCount(achievement.generateNextLevelCount(0));
            achievement.setNextReceiveLevelCount(achievement.getNextLevelCount());
            achievement.setBeans(type.getBeans());
            achievement.setPkVitality(type.getPkVitality());
            map.put(achievement.getAchievementType(), achievement);
        }
        return map;
    }

    public void calculateNextLevelCount() {
        Integer result = generateNextLevelCount(getTotalCount());
        setNextLevelCount(result);
    }

    private Integer generateNextLevelCount(Integer totalCount) {
        if (null == totalCount || 0 == totalCount) {
            totalCount = getTotalCount();
        }
        switch (getAchievementType()) {
            case LOGIN: {
                if (totalCount < 5) {
                    return 5;
                } else if (totalCount >= 5 && totalCount < 10) {
                    return 10;
                } else if (totalCount >= 10) {
                    Integer times = totalCount / 10;
                    return (times + 1) * 10;
                }
                break;
            }
            case DIAMOND: {
                if (totalCount < 10) {
                    return 10;
                } else if (totalCount >= 10 && totalCount < 30) {
                    return 30;
                } else if (totalCount >= 30 && totalCount < 50) {
                    return 50;
                } else if (totalCount >= 50) {
                    Integer times = totalCount / 50;
                    return (times + 1) * 50;
                }
                break;
            }
            case STAGE: {
                if (totalCount < 5) {
                    return 5;
                } else if (totalCount >= 5 && totalCount < 10) {
                    return 10;
                } else if (totalCount >= 10) {
                    Integer times = totalCount / 10;
                    return (times + 1) * 10;
                }
                break;
            }
            case SHARED: {
                if (totalCount < 5) {
                    return 5;
                } else if (totalCount >= 5) {
                    Integer times = (totalCount - 5) / 10;
                    return (times + 1) * 10 + 5;
                }
                break;
            }
            default: {
                return 0;
            }
        }
        return 0;
    }

    public void calculateNextReceiveLevelCount() {
        if (getNextLevelCount() > getTotalCount() && getTotalCount() >= getNextReceiveLevelCount()) {
            Integer nextReceiveLevelCount = generateNextLevelCount(getNextReceiveLevelCount());
            setNextReceiveLevelCount(nextReceiveLevelCount);
        }
    }

    public boolean canReceive() {
        return getNextLevelCount() > getNextReceiveLevelCount();
    }


}
