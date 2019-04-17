package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.entity.PublicGoodFeed;
import org.jsoup.helper.Validate;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type = PublicGoodFeed.class)
public class PublicGoodFeedDao extends DynamicCacheDimensionDocumentMongoDao<PublicGoodFeed, String> {

    @Override
    protected String calculateDatabase(String template, PublicGoodFeed document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, PublicGoodFeed document) {
        Validate.notNull(document);
        Validate.notNull(document.getId());

        String[] idParts = document.getId().split("-");
        Validate.isTrue(idParts.length >= 2);

        Long userId = SafeConverter.toLong(idParts[0]);
        long routeNum = userId % (RuntimeMode.isUsingTestData() ? 2 : 100);

        return StringUtils.formatMessage(template, routeNum);
    }

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();

        registerBeforeInsertListener(documents ->
                documents.stream()
                        .filter(d -> d.getId() == null)
                        .forEach(PublicGoodFeed::generateId)
        );
    }

    @CacheMethod
    public List<PublicGoodFeed> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = new Query(criteria);
        return executeQuery(getMongoConnection(userId), query);
    }

    public PublicGoodFeed upsertFeed(PublicGoodFeed feed) {
        if (feed == null) {
            return null;
        }
        // 先更新
        PublicGoodFeed modified = super.$upsert(feed);
        // 不让缓存失效，而是直接更新
        String cacheKey = CacheKeyGenerator.generateCacheKey(PublicGoodFeed.class, "UID", feed.getUserId());
        getCache().<List<PublicGoodFeed>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(0)
                .modifier(feedList -> {
                    // 如果已经存在则覆盖，否则是新增
                    int existIndex = feedList.indexOf(modified);
                    if (existIndex >= 0) {
                        feedList.set(existIndex, modified);
                    } else {
                        feedList.add(modified);
                    }
                    return feedList;
                }).execute();

        return modified;
    }

    private MongoConnection getMongoConnection(Long userId) {
        String mockId = userId + "-0000000";
        return createMongoConnection(calculateIdMongoNamespace(mockId));
    }

}
