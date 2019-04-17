package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@CacheBean(type = MizarShopGoods.class, useValueWrapper = true)
public class MizarShopGoodsDao extends AlpsStaticMongoDao<MizarShopGoods, String> {

    @Override
    protected void calculateCacheDimensions(MizarShopGoods document, Collection<String> dimensions) {
        dimensions.add(MizarShopGoods.ck_id(document.getId()));
        dimensions.add(MizarShopGoods.ck_shopId(document.getShopId()));
    }

    @CacheMethod
    public List<MizarShopGoods> loadByShopId(@CacheParameter(value = "shopId") String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("shop_id").is(shopId);
        Query query = Query.query(criteria);
        return query(query);
    }

    public Page<MizarShopGoods> loadByPage(Pageable page, String goodsName) {
        Criteria criteria = new Criteria();
        goodsName = StringRegexUtils.escapeExprSpecialWord(goodsName);
        if (StringUtils.isNotBlank(goodsName)) {
            criteria = Criteria.where("goods_name").regex(Pattern.compile(".*" + goodsName + ".*"));
        }
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }

    @CacheMethod
    public Map<String, List<MizarShopGoods>> loadByShopIds(@CacheParameter(value = "shopId", multiple = true)
                                                                   Collection<String> shopIds) {
        Criteria criteria = Criteria.where("shop_id").in(shopIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(MizarShopGoods::getShopId));
    }


    // 获取全部ID 任务调用
    public Set<String> loadAllShopGoodsIds() {
        Query query = Query.query(new Criteria());
        query.field().includes("_id");
        return query(query).stream().map(MizarShopGoods::getId).collect(Collectors.toSet());
    }

    public Page<MizarShopGoods> loadPageByGoodsType(Collection<String> type, Pageable page, MizarGoodsStatus status) {
        if (CollectionUtils.isEmpty(type) || page == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        Criteria criteria = Criteria.where("goodsType").in(type);
        if (status != null) {
            criteria.and("status").is(status);
        }
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }
}
