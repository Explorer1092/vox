package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopRating;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiang.lv
 *         机构大数据信息Dao
 * @date 2016-09-13
 */
@Named
@UtopiaCacheSupport(MizarShopRating.class)
public class MizarShopRatingDao extends AlpsStaticMongoDao<MizarShopRating, String> {

    @Override
    protected void calculateCacheDimensions(MizarShopRating document, Collection<String> dimensions) {
        dimensions.add(MizarShopRating.ck_shopId(document.getShopId()));
    }

    @CacheMethod
    public Map<String, MizarShopRating> loadMizarShopRatingByShopIds(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds) {
        Criteria criteria = Criteria.where("shop_id").in(shopIds);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(MizarShopRating::getShopId, Function.identity()));
    }
}
