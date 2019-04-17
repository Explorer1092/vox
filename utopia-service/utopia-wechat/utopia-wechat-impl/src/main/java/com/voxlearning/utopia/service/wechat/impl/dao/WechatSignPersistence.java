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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.wechat.api.entities.WechatSign;

import javax.inject.Named;

/**
 * Persistence implementation of entity {@link WechatSign}.
 *
 * @author Shuai Huan
 * @since Dec 2, 2014
 */
@Named
@UtopiaCacheSupport(WechatSign.class)
public class WechatSignPersistence extends StaticCacheDimensionDocumentJdbcDao<WechatSign, Long> {

    @UtopiaCacheable
    public WechatSign findByUserIdAndSignDate(@UtopiaCacheKey(name = "userId") Long userId,
                                              @UtopiaCacheKey(name = "signDate") String signDate) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("SIGN_DATE").is(signDate)
                .and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort).limit(1)).stream().findFirst().orElse(null);
    }
}
