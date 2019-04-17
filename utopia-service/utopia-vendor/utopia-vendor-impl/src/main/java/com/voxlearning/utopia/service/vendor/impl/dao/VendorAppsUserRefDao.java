package com.voxlearning.utopia.service.vendor.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Summer on 2017/2/10.
 */
@Named
@CacheBean(type = VendorAppsUserRef.class)
public class VendorAppsUserRefDao extends AlpsDynamicMongoDao<VendorAppsUserRef, String> {

    @Override
    protected String calculateDatabase(String template, VendorAppsUserRef document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, VendorAppsUserRef document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());

        String[] ids = StringUtils.split(document.getId(), "-");
        if (ids.length != 2) throw new IllegalArgumentException();

        long userId = SafeConverter.toLong(ids[0]);
        if (0 == userId) throw new IllegalArgumentException();

        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            long mod = userId % 2;
            return StringUtils.formatMessage(template, mod);
        } else {
            long mod = userId % 50;
            return StringUtils.formatMessage(template, mod);
        }
    }

    @Override
    protected void calculateCacheDimensions(VendorAppsUserRef document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @UtopiaCacheable
    public List<VendorAppsUserRef> findVendorAppsUserRefList(@UtopiaCacheKey(name = "UID") Long userId) {
        if (userId == null || userId == 0) {
            return Collections.emptyList();
        }
        Pattern pattern = Pattern.compile("^" + userId + "-");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return executeQuery(calculateMongoConnection(userId), Query.query(criteria));
    }

    private MongoConnection calculateMongoConnection(Long userId) {
        String mockId = userId + "-000000000000000000000000";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(namespace);
    }

    public long updateSessionKey(String appKey, Long userId, String sessionKey) {
        if (appKey == null || userId == null || sessionKey == null) {
            return 0;
        }

        VendorAppsUserRef ref = findVendorAppsUserRefList(userId).stream()
                .filter(p -> Objects.equals(p.getAppKey(), appKey)).findFirst().orElse(null);
        if (ref == null) {
            return 0;
        }

        Criteria criteria = Criteria.where("_id").is(ref.getId());
        Update update = Update.update("sessionKey", sessionKey).currentDate("updateTime");
        long rows = executeUpdateMany(calculateMongoConnection(userId), criteria, update);
        if (rows > 0) {
            // 极其低频的操作，直接清缓存
            getCache().delete(Arrays.asList(ref.generateCacheDimensions()));
        }
        return rows;
    }

    @Override
    public void insert(VendorAppsUserRef vendorAppsUserRef) {
        if (vendorAppsUserRef == null) {
            return;
        }
        if (StringUtils.isBlank(vendorAppsUserRef.getId())) {
            vendorAppsUserRef.setId(VendorAppsUserRef.generateId(vendorAppsUserRef.getUserId()));
        }
        $insert(vendorAppsUserRef);

        vendorAppsUserRef.setCreateTime(new Date());
        vendorAppsUserRef.setUpdateTime(new Date());

        String key = vendorAppsUserRef.newCacheKey("UID", vendorAppsUserRef.getUserId());
        ChangeCacheObject<List<VendorAppsUserRef>> modifier = currentValue -> {
            currentValue = new LinkedList<>(currentValue);
            currentValue.add(vendorAppsUserRef);
            return currentValue;
        };
        CacheValueModifierExecutor<List<VendorAppsUserRef>> executor = getCache().createCacheValueModifier();
        executor.key(key)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(modifier)
                .execute();
    }


}
