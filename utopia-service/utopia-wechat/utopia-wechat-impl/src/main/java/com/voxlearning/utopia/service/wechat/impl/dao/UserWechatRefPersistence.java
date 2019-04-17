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

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persistence implementation of entity {@code UserWechatRef}.
 *
 * @author xin.xin
 * @since 2014-04-16 10:10AM
 */
@Named
@UtopiaCacheSupport(UserWechatRef.class)
public class UserWechatRefPersistence extends StaticCacheDimensionDocumentJdbcDao<UserWechatRef, Long> {

    @UtopiaCacheable
    public List<UserWechatRef> findByUserId(@UtopiaCacheKey(name = "userId") Long userId,
                                            @UtopiaCacheKey(name = "type") Integer type) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("TYPE").is(type)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable
    public UserWechatRef findByOpenId(@UtopiaCacheKey(name = "openId") String openId) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    @UtopiaCacheable
    public UserWechatRef findByOpenIdAndUserId(@UtopiaCacheKey(name = "openId") String openId,
                                               @UtopiaCacheKey(name = "userId") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("OPEN_ID").is(openId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    /**
     * 用于判定是否第一次绑定微信账号
     */
    @UtopiaCacheable
    public List<UserWechatRef> findByUserIdInAnyCase(@UtopiaCacheKey(name = "USER_ID") Long userId,
                                                     @UtopiaCacheKey(name = "TYPE") Integer type) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("TYPE").is(type);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<UserWechatRef>> findByUserIds(@CacheParameter(value = "userId", multiple = true)
                                                        final Collection<Long> userIds,
                                                        @CacheParameter("type") final Integer type) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds)
                .and("TYPE").is(type)
                .and("DISABLED").is(false);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(UserWechatRef::getUserId));
    }

    @CacheMethod
    public Map<Long, List<UserWechatRef>> findByUserIdsFromCache(@CacheParameter(value = "userId", multiple = true)
                                                                 final Collection<Long> userIds) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(UserWechatRef::getUserId));
    }

    @CacheMethod
    public UserWechatRef findByUnionId(@CacheParameter(value = "UNION_ID") String unionId) {
        if (StringUtils.isBlank(unionId)) {
            return null;
        }
        Criteria criteria = Criteria.where("UNION_ID").is(unionId).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }
}
