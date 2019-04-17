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

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFollower;

import javax.inject.Named;

/**
 * DAO implementation of {@link WechatFollower}.
 *
 * @author Shuai Huan
 * @since Dec 18, 2014
 */
@Named
@UtopiaCacheSupport(WechatFollower.class)
public class WechatFollowerPersistence extends StaticCacheDimensionDocumentJdbcDao<WechatFollower, Long> {

    @UtopiaCacheable
    public WechatFollower findByOpenId(@UtopiaCacheKey(name = "openId") String openId) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    public int unFollow(String openId) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId).and("DISABLED").is(false);
        Update update = Update.update("DISABLED", true);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            WechatFollower document = new WechatFollower();
            document.setOpenId(openId);
            evictDocumentCache(document);
        }
        return rows;
    }
}
