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

package com.voxlearning.utopia.service.nekketsu.elf.dao;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfMyGift;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfMyGiftList;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
public class ElfMyGiftDao extends StaticMongoDao<ElfMyGiftList, Long> {

    public ElfMyGiftList load(long userId) {
        ElfMyGiftList fromDb = super.load(userId);
        if (null != fromDb) {
            return fromDb;
        }
        ElfMyGiftList newInstance = ElfMyGiftList.getDefault(userId);
        if (null != insert(newInstance)) {
            return newInstance;
        }
        return null;
    }

    public ElfMyGiftList removeGift(long userId, String giftId) {
        String upField = "giftMap." + giftId;
        return update(userId, updateBuilder.build().unset(upField));
    }

    public ElfMyGiftList pushGift(long userId, ElfMyGift elfMyGift) {
        if (null == elfMyGift) {
            return null;
        }
        if (StringUtils.isBlank(elfMyGift.getGiftId())) {
            elfMyGift.setGiftId(RandomUtils.nextObjectId());
        }
        Update update = updateBuilder.build().set("giftMap." + elfMyGift.getGiftId(), elfMyGift);
        return update(userId, update);
    }

    @Override
    protected void calculateCacheDimensions(ElfMyGiftList source, Collection<String> dimensions) {
        dimensions.add(source.cacheKeyFromCondition(source));
    }
}
