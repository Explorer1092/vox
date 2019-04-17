package com.voxlearning.utopia.service.reward.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.entity.PublicGoodUserActivity;
import org.jsoup.helper.Validate;

import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Named
@CacheBean(type = PublicGoodUserActivity.class)
public class PublicGoodUserActivityDao extends DynamicCacheDimensionDocumentMongoDao<PublicGoodUserActivity, String> {

    @Override
    protected String calculateDatabase(String template, PublicGoodUserActivity document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, PublicGoodUserActivity document) {
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
                        .forEach(PublicGoodUserActivity::generateId)
        );
    }

    @CacheMethod
    public List<PublicGoodUserActivity> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = new Query(criteria);
        return executeQuery(getMongoConnection(userId), query);
    }

    public PublicGoodUserActivity addLike(PublicGoodUserActivity userActivity) {
        return addLike(userActivity, 1L);
    }

    public PublicGoodUserActivity addLike(PublicGoodUserActivity userActivity, Long likeCount) {
        if (userActivity == null) {
            return null;
        }
        Criteria criteria = Criteria.where("userId").is(userActivity.getUserId()).and("_id").is(userActivity.getId());
        Update update = new Update();
        update.inc("likeNum", likeCount);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        PublicGoodUserActivity modified = executeFindOneAndUpdate(getMongoConnection(userActivity.getUserId()), criteria, update, options);

        String cacheKey = CacheKeyGenerator.generateCacheKey(PublicGoodUserActivity.class, "UID", userActivity.getUserId());
        getCache().<List<PublicGoodUserActivity>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(0)
                .modifier(cacheUserActivity -> {
                    for (PublicGoodUserActivity activity : cacheUserActivity) {
                        if (Objects.equals(activity.getActivityId(), modified.getActivityId())) {
                            activity.setLikeNum(modified.getLikeNum());
                        }
                    }
                    return cacheUserActivity;
                }).execute();
        return modified;
    }

    public PublicGoodUserActivity addMoney(PublicGoodUserActivity userActivity, Long money) {
        if (userActivity == null || money == null) {
            return null;
        }
        Criteria criteria = Criteria.where("userId").is(userActivity.getUserId()).and("_id").is(userActivity.getId());
        Update update = new Update();
        update.inc("moneyNum", money);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        PublicGoodUserActivity modified = executeFindOneAndUpdate(getMongoConnection(userActivity.getUserId()), criteria, update, options);

        String cacheKey = CacheKeyGenerator.generateCacheKey(PublicGoodUserActivity.class, "UID", userActivity.getUserId());
        getCache().<List<PublicGoodUserActivity>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(0)
                .modifier(cacheUserActivity -> {
                    for (PublicGoodUserActivity activity : cacheUserActivity) {
                        if (Objects.equals(activity.getActivityId(), modified.getActivityId())) {
                            activity.setMoneyNum(modified.getMoneyNum());
                        }
                    }
                    return cacheUserActivity;
                }).execute();
        return modified;
    }

    public void addLikedUser(PublicGoodUserActivity userActivity, Set<Long> userId) {
        if (userActivity == null || userId == null) {
            return;
        }
        Set<Long> likedUser = new HashSet<>();
        String cacheKey = CacheKeyGenerator.generateCacheKey(PublicGoodUserActivity.class, "UID", userActivity.getUserId());
        getCache().<List<PublicGoodUserActivity>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(0)
                .modifier(cacheUserActivity -> {
                    for (PublicGoodUserActivity item : cacheUserActivity) {
                        if (Objects.equals(item.getActivityId(), userActivity.getActivityId())) {
                            Set<Long> likedId = item.getLikedUser();
                            if (likedId != null) {
                                likedUser.addAll(likedId);
                            }
                            likedUser.addAll(userId);
                            item.setLikedUser(likedUser);
                        }
                    }
                    return cacheUserActivity;
                }).execute();

        Criteria criteria = Criteria.where("userId").is(userActivity.getUserId()).and("_id").is(userActivity.getId());
        Update update = new Update();
        update.set("likedUser", likedUser);
        executeUpdateOne(getMongoConnection(userActivity.getUserId()), criteria, update);
    }

    public PublicGoodUserActivity upsertUserActivity(PublicGoodUserActivity userActivity) {
        if (userActivity == null) {
            return null;
        }

        PublicGoodUserActivity modified = super.$upsert(userActivity);

        String cacheKey = CacheKeyGenerator.generateCacheKey(PublicGoodUserActivity.class, "UID", userActivity.getUserId());
        getCache().<List<PublicGoodUserActivity>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(0)
                .modifier(cacheUserActivity -> {
                    // 如果已经存在则覆盖，否则是新增
                    int existIndex = cacheUserActivity.indexOf(modified);
                    if (existIndex >= 0) {
                        cacheUserActivity.set(existIndex, modified);
                    } else {
                        cacheUserActivity.add(modified);
                    }
                    return cacheUserActivity;
                }).execute();

        return modified;
    }

    private MongoConnection getMongoConnection(Long userId) {
        String mockId = userId + "-0000000";
        return createMongoConnection(calculateIdMongoNamespace(mockId));
    }

    public List<PublicGoodUserActivity> findShareTable(Long tableName) {
        return executeQuery(getMongoConnection(tableName), new Query(new Criteria()));
    }

}
