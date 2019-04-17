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

package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UseEqualsValidateCache;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 用户大冒险信息
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/15 16:03
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-adventure")
@DocumentCollection(collection = "vox_adventure_useradventure")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151103")
@EqualsAndHashCode(of = "id")
@UseEqualsValidateCache
public class UserAdventure implements Serializable {
    private static final long serialVersionUID = 1953905165720066319L;

    @DocumentId private Long id;                            //userId
    private String image;                                   //头像
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;
    private Integer currentDiamond;                         //当前钻石数
    private Integer currentCrown;                           //当前皇冠数
    private Integer totalBeans;                             //总共得到的学豆
    private Integer totalPkVitality;                        //总共得到的PK活力值
    private String bookStagesId;                            //用户当前教材关卡ID
    private Integer trialCount;                             //用户试用关卡数量
    private List<Long> bookIds;                             //用户所有教材ID
    private Map<AchievementType, Achievement> achievements; //成就
    private Date finalReceiveFbTime; //最后领取免费学习豆时间

    @JsonIgnore
    public Long getBookId() {
        return Long.valueOf(bookStagesId.split("_")[1]);
    }

    /**
     * 成就信息，用于给前端返回数据
     * 注意: 这是前端flash需要的数据
     */
    @DocumentFieldIgnore
    public List<Achievement> getAchievementList() {
        if (getAchievements() == null) {
            return new LinkedList<>();
        } else {
            return new LinkedList<>(getAchievements().values());
        }
    }

    public void increaseCurrentDiamond(Integer count) {
        this.setCurrentDiamond(this.getCurrentDiamond() + count);
    }

    public void increaseCurrentCrown(Integer count) {
        this.setCurrentCrown(this.getCurrentCrown() + count);
    }

    public static UserAdventure newInstance(Long userId, Long bookId, String bookStagesId) {
        UserAdventure userAdventure = new UserAdventure();
        userAdventure.setId(userId);
        Date current = new Date();
        userAdventure.setCreateTime(current);
        userAdventure.setUpdateTime(current);
        userAdventure.setCurrentCrown(0);
        userAdventure.setCurrentDiamond(0);
        userAdventure.setTotalBeans(0);
        userAdventure.setTotalPkVitality(0);
        userAdventure.setBookStagesId(bookStagesId);
        userAdventure.setAchievements(Achievement.newAchievements());
        List<Long> bookIds = new LinkedList<>();
        bookIds.add(bookId);
        userAdventure.setBookIds(bookIds);
        userAdventure.setTrialCount(0);
        userAdventure.setFinalReceiveFbTime(null);
        return userAdventure;
    }

    public static List<String> bookStagesIdList(List<UserAdventure> userAdventures) {
        List<String> list = new LinkedList<>();
        for (UserAdventure adventure : userAdventures) {
            list.add(adventure.getBookStagesId());
        }
        return list;
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(UserAdventure.class, id);
    }
}
