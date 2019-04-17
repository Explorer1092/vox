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
import com.voxlearning.utopia.service.reward.entity.PublicGoodCollect;
import org.jsoup.helper.Validate;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PublicGoodCollect.class)
public class PublicGoodCollectDao extends DynamicCacheDimensionDocumentMongoDao<PublicGoodCollect,String> {

    @Override
    protected String calculateDatabase(String template, PublicGoodCollect document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, PublicGoodCollect document) {
        Validate.notNull(document);
        Validate.notNull(document.getId());

        String[] idParts = document.getId().split("-");
        Validate.isTrue(idParts.length >= 2);

        Long userId = SafeConverter.toLong(idParts[0]);
        long routeNum = userId % (RuntimeMode.isUsingTestData() ? 2 : 100);

        return StringUtils.formatMessage(template, routeNum);
    }

    @Override
    protected void calculateCacheDimensions(PublicGoodCollect document, Collection<String> dimensions) {

    }

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();

        registerBeforeInsertListener(documents ->
                documents.stream()
                        .filter(d -> d.getId() == null)
                        .forEach(PublicGoodCollect::generateId)
        );
    }

    @CacheMethod
    public List<PublicGoodCollect> loadByUserId(@CacheParameter("USER_ID") Long userId){
        Criteria criteria = Criteria.where("userId").is(userId);
        return executeQuery(getMongoConnection(userId), Query.query(criteria));
    }

    public PublicGoodCollect upsertCollect(PublicGoodCollect collect){
        if(collect == null)
            return null;

        // 先更新
        PublicGoodCollect modified = super.$upsert(collect);
        // 不让缓存失效，而是直接更新
        String cacheKey = CacheKeyGenerator.generateCacheKey(PublicGoodCollect.class,"USER_ID",collect.getUserId());
        getCache().<List<PublicGoodCollect>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(0)
                .modifier(collectList -> {
                    // 如果已经存在则覆盖，否则是新增
                    int existIndex = collectList.indexOf(modified);
                    if(existIndex >= 0){
                        collectList.set(existIndex,modified);
                    }else
                        collectList.add(modified);

                    return collectList;
                })
                .execute();

        return modified;
    }

    private MongoConnection getMongoConnection(Long userId) {
        String mockId = userId + "-0000000";
        return createMongoConnection(calculateIdMongoNamespace(mockId));
    }
}
