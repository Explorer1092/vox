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

package com.voxlearning.utopia.service.nekketsu.elf.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/27.
 * 播放开场动画、有新可合成植物、有新礼物、有新成就
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-bubble")
@DocumentCollection(collection = "elf_bubble")

public class ElfBubble implements Serializable {
    private static final long serialVersionUID = 8740061599648733404L;

    @DocumentId
    private Long userId;

    private Boolean playAnimate;

    /**
     * key:SAVE_PRINCE,SAVE_QUEEN,SAVE_KING
     */
    private Map<String, Boolean> newPlant;

    private Boolean newGift;

    private Boolean newAchv;

    public ElfBubble() {

    }

    public static ElfBubble getDefault(long userId) {
        ElfBubble istc = new ElfBubble();
        istc.userId = userId;
        istc.playAnimate = true;
        istc.newPlant = new HashMap<>();
        istc.newPlant.put("SAVE_PRINCE", false);
        istc.newPlant.put("SAVE_QUEEN", false);
        istc.newPlant.put("SAVE_KING", false);
        istc.newGift = false;
        istc.newAchv = false;
        return istc;
    }

    public String cacheKeyFromCondition(ElfBubble source) {
        return CacheKeyGenerator.generateCacheKey(ElfBubble.class, source.getUserId());
    }

    public boolean isPlayAnimate() {
        return playAnimate != null && playAnimate;
    }

    public boolean isNewGift() {
        return newGift != null && newGift;
    }
}
