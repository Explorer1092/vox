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

import com.mongodb.ReadPreference;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.GiftType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.Gift;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 礼物
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/20 16:31
 */
@Named
public class GiftDao extends StaticMongoDao<Gift, String> {

    @Override
    protected void calculateCacheDimensions(Gift source, Collection<String> dimensions) {
    }

    public Gift grantGift(String id) {
        Gift inst = new Gift();
        inst.setGrant(Boolean.TRUE);
        return update(id, inst);
    }

    public List<Gift> getUngrantGifts(Long userId, GiftType type) {
        Filter filter = filterBuilder.where("userId").is(userId)
                .and("grant").is(false)
                .and("type").is(type);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

}
