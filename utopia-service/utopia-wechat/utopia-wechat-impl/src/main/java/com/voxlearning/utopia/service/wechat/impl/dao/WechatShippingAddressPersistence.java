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
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.wechat.api.entities.WechatShippingAddress;

import javax.inject.Named;

/**
 * DAO implementation of {@link WechatShippingAddress}.
 *
 * @author Shuai Huan
 * @since Dec 8, 2014
 */
@Named
@UtopiaCacheSupport(WechatShippingAddress.class)
public class WechatShippingAddressPersistence extends StaticCacheDimensionDocumentJdbcDao<WechatShippingAddress, Long> {

    @UtopiaCacheable
    public WechatShippingAddress findByUserId(@UtopiaCacheKey(name = "userId") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }
}
