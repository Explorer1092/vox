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

package com.voxlearning.utopia.service.surl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.surl.entity.ShortUrlRef;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Named;
import java.util.Collection;
import java.util.Objects;

/**
 * @author xin.xin
 * @since 9/28/15
 */
@Named
@CacheBean(type = ShortUrlRef.class)
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class ShortUrlRefPersistence extends AlpsStaticJdbcDao<ShortUrlRef, Long> {

    public ShortUrlRefPersistence() {
        registerBeforeInsertListener(documents -> documents.stream()
                .filter(e -> e.getDisabled() == null)
                .forEach(e -> e.setDisabled(false)));
    }

    @Override
    protected void calculateCacheDimensions(ShortUrlRef document, Collection<String> dimensions) {
        dimensions.add(CacheKeyGenerator.generateCacheKey(ShortUrlRef.class, "shortUrl", document.getShortUrl()));
        dimensions.add(generateCacheKey(document.getLongUrl()));
    }

    @CacheMethod
    public ShortUrlRef getByShortUrl(@CacheParameter("shortUrl") String shortUrl) {
        Criteria criteria = Criteria.where("SHORT_URL").is(shortUrl).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    public ShortUrlRef getByLongUrl(String longUrl) {
        String key = generateCacheKey(longUrl);
        ShortUrlRef cached = getCache().load(key);
        if (cached != null) {
            return cached;
        }
        //sign字段里存longUrl的md5值,cache key也是longUrl的md5值,这里直接用cache key了
        Criteria criteria = Criteria.where("SIGN").is(key).and("DISABLED").is(false);
        cached = query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
        if (cached != null) {
            getCache().add(key, getDefaultCacheExpirationInSeconds(), cached);
        }
        return cached;
    }

    private String generateCacheKey(String longUrl) {
        Objects.requireNonNull(longUrl);
        return DigestUtils.md5Hex(longUrl);
    }
}
