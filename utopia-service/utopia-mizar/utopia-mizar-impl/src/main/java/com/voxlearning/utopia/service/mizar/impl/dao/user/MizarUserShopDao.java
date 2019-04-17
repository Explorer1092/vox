package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.bson.BsonConverter;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserShop;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mizar User DAO class
 * Created by alex on 2016/8/16.
 */
@Named
@CacheBean(type = MizarUserShop.class)
public class MizarUserShopDao extends AlpsStaticMongoDao<MizarUserShop, String> {

    @Override
    protected void calculateCacheDimensions(MizarUserShop document, Collection<String> dimensions) {
        dimensions.add(MizarUserShop.ck_user(document.getUserId()));
        dimensions.add(MizarUserShop.ck_shop(document.getShopId()));
    }

    @CacheMethod
    public List<MizarUserShop> findByUser(@CacheParameter(value = "uid") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toList());
    }

    @CacheMethod
    public List<MizarUserShop> loadByShopId(@CacheParameter(value = "sid") String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("shopId").is(shopId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toList());
    }

    public MizarUserShop disableUserShop(final String userId, final String shopId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("shopId").is(shopId).and("disabled").is(false);
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = Update.update("disabled", true);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).findOneAndUpdate(filter, updateTranslator.translate(update));
        MizarUserShop userShop = BsonConverter.fromBsonDocument(document, getDocumentClass());
        if (userShop != null) {
            Set<String> cacheKeys = new HashSet<>();
            cacheKeys.add(MizarUserShop.ck_shop(userShop.getShopId()));
            cacheKeys.add(MizarUserShop.ck_user(userShop.getUserId()));
            getCache().delete(cacheKeys);
        }
        return userShop;
    }



}
