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


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.GiftType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 礼物
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/15 15:45
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-adventure")
@DocumentCollection(collection = "vox_adventure_gift")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1}", background = true)
})
public class Gift implements Serializable {
    private static final long serialVersionUID = 2876904758121052462L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;
    private Long userId;                                //用户ID
    private String content;                             //内容
    private Integer beanNum;                            //学豆数量
    private Integer pkVitalityNum;                      //活力数量
    private Boolean grant;                              //是否发放
    private GiftType type;                              //礼物类型
    private AchievementType achievementType;            //成就类型，用于区分成就型礼物
    private Long bookId;                                //教材ID
    private Integer stageOrder;                         //关卡顺序
    private Integer appOrder;                           //小应用顺序

    public static Gift createAchivementGift(Long userId, Long bookId, AchievementType achievementType, Integer count) {
        Integer stageOrder = 0;
        Integer appOrder = 0;
        switch (achievementType) {
            case LOGIN: {
                return createGift(userId, bookId, stageOrder, appOrder, 1, 0, "进入奇幻探险累计达到" + count + "天",
                        GiftType.ACHIEVEMENT, AchievementType.LOGIN);
            }
            case DIAMOND: {
                return createGift(userId, bookId, stageOrder, appOrder, 1, 0, "累计收集水晶钻石" + count + "颗",
                        GiftType.ACHIEVEMENT, AchievementType.DIAMOND);
            }
            case STAGE: {
                return createGift(userId, bookId, stageOrder, appOrder, 1, 1, "累计解锁" + count + "个关卡",
                        GiftType.ACHIEVEMENT, AchievementType.STAGE);
            }
            case SHARED: {
                return createGift(userId, bookId, stageOrder, appOrder, 1, 0, "累计班级空间分享" + count + "次",
                        GiftType.ACHIEVEMENT, AchievementType.SHARED);
            }
            default:
                throw new IllegalArgumentException("No achievementType");
        }
    }

    public static Gift createStageBaseAppDiamondGift(Long userId, Long bookId, Integer stageOrder,
                                                     Integer appOrder, String fileName) {//首次获得关卡基础应用三星
        return createGift(userId, bookId, stageOrder, appOrder, 1, 0,
                "首次获得第" + stageOrder + "关" + fileName + "游戏三个钻石奖励", GiftType.STAGE, null);
    }

    public static Gift createCrownGift(Long userId, Long bookId, Integer stageOrder) {//首次获得关卡皇冠
        return createGift(userId, bookId, stageOrder, 0, 1, 1, "首次获得第" + stageOrder + "关皇冠奖励", GiftType.STAGE, null);
    }

    private static Gift createGift(Long userId, Long bookId, Integer stageOrder, Integer appOrder, Integer beanNum,
                                   Integer pkVitalityNum, String content, GiftType type, AchievementType achievementType) {
        Gift gift = new Gift();
        Date current = new Date();
        gift.setCreateTime(current);
        gift.setUpdateTime(current);
        gift.setUserId(userId);
        gift.setBeanNum(beanNum);
        gift.setPkVitalityNum(pkVitalityNum);
        gift.setContent(content);
        gift.setGrant(false);
        gift.setType(type);
        gift.setAchievementType(achievementType);
        gift.setBookId(bookId);
        gift.setStageOrder(stageOrder);
        gift.setAppOrder(appOrder);
        return gift;
    }

}
