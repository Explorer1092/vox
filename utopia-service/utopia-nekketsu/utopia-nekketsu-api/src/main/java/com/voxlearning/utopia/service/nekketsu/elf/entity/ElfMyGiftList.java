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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/26.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-gift")
@DocumentCollection(collection = "elf_gift")
public class ElfMyGiftList implements Serializable {
    private static final long serialVersionUID = 5832900939697167087L;
    @DocumentId
    private Long userId;
    private Map<String, ElfMyGift> giftMap;

    public ElfMyGiftList() {

    }

    public static ElfMyGiftList getDefault(long userId) {
        ElfMyGiftList instance = new ElfMyGiftList();
        instance.userId = userId;
        instance.giftMap = new LinkedHashMap<>();
        return instance;
    }

    public String cacheKeyFromCondition(ElfMyGiftList source) {
        return CacheKeyGenerator.generateCacheKey(ElfMyGiftList.class, userId);
    }
}
