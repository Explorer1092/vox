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

package com.voxlearning.utopia.service.nekketsu.parkour.entity;


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sadi.Wan on 2014/8/15.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-parkour")
@DocumentCollection(collection = "vox_nekketsu_parkour_role")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151027")
public class ParkourRole implements Serializable {
    private static final long serialVersionUID = 6846547164085683250L;

    @DocumentId private Long roleId;                    // 角色Id（目前即userid）
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;
    private Integer exp;
    private Integer level;
    private Integer coinCount;                  // 跑酷金币数
    private Integer openStage;                  // 已开启关卡
    private Integer passedStage;                // 已通过关卡
    private Integer coinToExchange;             // 未付费用户积攒的游戏金币
    private List<ParkourWord> wordToExchange;   // 未付费用户积累的单词。用于付费后自动兑换成学豆
    private Date spDate;

    public ParkourRole initializeIfNecessary() {
        if (getExp() == null) setExp(0);
        if (getLevel() == null) setLevel(0);
        if (getCoinCount() == null) setCoinCount(0);
        if (getOpenStage() == null) setOpenStage(0);
        if (getPassedStage() == null) setPassedStage(0);
        if (getCoinToExchange() == null) setCoinToExchange(0);
        if (getWordToExchange() == null) setWordToExchange(new LinkedList<>());
        return this;
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ParkourRole.class, id);
    }

}
