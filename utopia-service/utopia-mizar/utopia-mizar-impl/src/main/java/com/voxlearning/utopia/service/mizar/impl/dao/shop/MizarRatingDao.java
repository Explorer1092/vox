package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarRating;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@CacheBean(type = MizarRating.class, useValueWrapper = true)
public class MizarRatingDao extends AlpsStaticMongoDao<MizarRating, String> {

    @Override
    protected void calculateCacheDimensions(MizarRating document, Collection<String> dimensions) {
        dimensions.add(MizarRating.ck_id(document.getId()));
        dimensions.add(MizarRating.ck_shopId(document.getShopId()));
        dimensions.add(MizarRating.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<MizarRating> loadByShopId(@CacheParameter(value = "shopId") String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("shop_id").is(shopId).and("status").is(MizarRatingStatus.ONLINE.name());
        Sort sort = new Sort(Sort.Direction.DESC, "rating_time");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

    @CacheMethod
    public List<MizarRating> loadByUserId(@CacheParameter(value = "userId") Long userId) {
        Criteria criteria = Criteria.where("user_id").is(userId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<String, List<MizarRating>> loadByShopIds(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds) {
        Criteria criteria = Criteria.where("shop_id").in(shopIds).and("status").is(MizarRatingStatus.ONLINE.name());
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(MizarRating::getShopId));
    }

    @CacheMethod
    public List<MizarRating> loadAllByShopId(@CacheParameter(value = "shopId") String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("shop_id");
        Sort sort = new Sort(Sort.Direction.DESC, "rating_time");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

    @CacheMethod
    public Map<String, List<MizarRating>> loadAllByShopIds(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds) {
        Criteria criteria = Criteria.where("shop_id").in(shopIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(MizarRating::getShopId));
    }

    public PageImpl<MizarRating> loadPageByActivityIdAndTime(Integer activityId, Long time, Pageable page) {
        Criteria criteria = Criteria.where("activity_id").is(activityId);
        criteria.and("rating_time").lte(time);
        criteria.and("good_rating").is(true);
        criteria.and("status").is(MizarRatingStatus.ONLINE.name());
        Sort sort = new Sort(Sort.Direction.DESC, "rating_time");
        Query query = Query.query(criteria).with(sort);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }

    public List<MizarRating> findByParam(Integer rating, String status, String content) {
        Criteria criteria = new Criteria();
        if (StringUtils.isBlank(status)) {
            status = MizarRatingStatus.PENDING.name();
        }
        if (!"ALL".equals(status)) {
            criteria.and("status").is(status);
        }
        if (rating != null && rating > 0) {
            criteria.and("rating").is(rating);
        }
        if (StringUtils.isNotBlank(content)) {
            content = StringRegexUtils.escapeExprSpecialWord(content);
            criteria.and("rating_content").regex(Pattern.compile(".*" + content + ".*"));
        }
        Sort sort = new Sort(Sort.Direction.DESC, "rating_time");
        Query query = Query.query(criteria).with(sort).limit(200);
        return query(query);
    }

    public void updateStatus(String id, MizarRatingStatus status) {
        Update update = Update.update("status", status.name());
        Criteria criteria = Criteria.where("_id").is(id);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER);
        MongoNamespace namespace = calculateIdMongoNamespace(id);
        MongoConnection connection = createMongoConnection(namespace);

        MizarRating rating = executeFindOneAndUpdate(connection, criteria, update, options);
        if (rating != null) {
            evictDocumentCache(rating);
        }
    }

    // 任务调用
    public Set<String> loadAllRatingIds() {
        Query query = Query.query(new Criteria());
        query.field().includes("_id");
        return query(query).stream().map(MizarRating::getId).collect(Collectors.toSet());
    }
}
