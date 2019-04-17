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

package com.voxlearning.utopia.service.nekketsu.adventure.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.Achievement;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.UserAdventure;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 16:13
 */
@Named
@UtopiaCacheSupport(UserAdventure.class)
public class UserAdventureDao extends StaticMongoDao<UserAdventure, Long> {
    @Override
    protected void calculateCacheDimensions(UserAdventure source, Collection<String> dimensions) {
        dimensions.add(UserAdventure.ck_id(source.getId()));
    }

    public UserAdventure changeBookStagesId(Long userId, final String bookStagesId, Long newBookId) {
        Update update = updateBuilder.update("bookStagesId", bookStagesId);
        if (null != newBookId) {
            update = update.addToSet("bookIds", newBookId);
        }
        return update(userId, update);
    }

    public void increaseBeanAndPkVitalityCount(Long userId, final Integer beans, final Integer pkVitality) {
        Update update = updateBuilder.build()
                .inc("totalBeans", beans)
                .inc("totalPkVitality", pkVitality);
        update(userId, update);
    }

    public void increaseCrown(Long userId, final Integer crownCount) {
        Update update = updateBuilder.build()
                .inc("currentCrown", crownCount);
        update(userId, update);
    }

    public UserAdventure decreaseCurrentDiamond(Long userId, final Integer diamondCount) {
        Update update = updateBuilder.build()
                .inc("currentDiamond", -diamondCount);
        return update(userId, update);
    }

    public void updateAchievement(Long userId, final Achievement achievement) {
        List<Achievement> achievements = new ArrayList<>();
        achievements.add(achievement);
        updateAchievement(userId, false, 0, achievements);
    }

    public UserAdventure updateAchievement(Long userId, boolean incrTrialStageCount, final int realObtainDiamond,
                                           final List<Achievement> achievements) {
        Update update = updateBuilder.build();
        if (realObtainDiamond > 0) {
            update = update.inc("currentDiamond", realObtainDiamond);
        }
        for (Achievement achievement : achievements) {
            update = update.set("achievements." + achievement.getAchievementType().name() +
                    ".totalCount", achievement.getTotalCount());
            update = update.set("achievements." + achievement.getAchievementType().name() +
                    ".nextLevelCount", achievement.getNextLevelCount());
        }
        if (incrTrialStageCount) {
            update = update.inc("trialCount", 1);
        }
        if (update.toBsonDocument().isEmpty()) {
            return null;
        }
        return update(userId, update);
    }

    public void updateNextReceiveLevelCount(Long userId, Integer nextReceiveLevelCount,
                                            AchievementType achievementType) {
        Update update = updateBuilder.build();
        update = update.set("achievements." + achievementType.name() + ".nextReceiveLevelCount", nextReceiveLevelCount);
        update(userId, update);
    }

    public void updateFinalReceiveFbTime(Long userId, Date date) {
        Update update = updateBuilder.build();
        update.set("finalReceiveFbTime", date);
        update(userId, update);
    }
}
