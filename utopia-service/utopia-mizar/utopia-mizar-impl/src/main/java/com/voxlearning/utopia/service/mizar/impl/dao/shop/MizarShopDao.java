package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType.ONLINE;

/**
 * Mizar Shop DAO Class
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@UtopiaCacheSupport(MizarShop.class)
public class MizarShopDao extends AlpsStaticMongoDao<MizarShop, String> {

    @Override
    protected void calculateCacheDimensions(MizarShop document, Collection<String> dimensions) {
        dimensions.add(MizarShop.ck_id(document.getId()));
        dimensions.add(MizarShop.ck_region(document.getRegionCode()));
        dimensions.add(MizarShop.ck_brand(document.getBrandId()));
        dimensions.add(MizarShop.ck_type(document.getType()));
    }

    @CacheMethod
    public Map<String,List<MizarShop>> loadShopByIds(@CacheParameter(value = "BID", multiple = true) List<String> branchIds){
        if (CollectionUtils.isEmpty(branchIds)) {
            return Collections.emptyMap();
        }
        //查询上线的了
        Criteria criteria = Criteria.where("brand_id").in(branchIds).and("shop_status").is(MizarShopStatusType.ONLINE.name());
        return query( Query.query(criteria)).stream().collect(Collectors.groupingBy(MizarShop::getBrandId));
    }

    @CacheMethod
    public List<MizarShop> loadByBrandId(@CacheParameter(value = "BID") String brandId) {
        if (StringUtils.isBlank(brandId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("brand_id").is(brandId).and("shop_status").is(ONLINE.getName());
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<MizarShop> loadByRegionCode(@CacheParameter(value = "RC") Integer regionCode) {
        if (regionCode == null || Objects.equals(regionCode, 0)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("region_code").is(regionCode).and("shop_status").is(ONLINE.getName());
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<MizarShop> loadByType(@CacheParameter(value = "T") Integer type) {
        if (type == null) {
            return Collections.emptyList();
        }

        Criteria criteria = Criteria.where("type").is(type).and("shop_status").is(ONLINE.getName());
        Query query = Query.query(criteria);
        return query(query);
    }

    // FIXME no cache here, maybe cause performance issue
    public List<MizarShop> loadByGpsPos(Double minLongitude, Double maxLongitude, Double minLatitude, Double maxLatitude) {
        Criteria criteria = Criteria.where("gps_longitude").gte(minLongitude).lte(maxLongitude)
                .and("gps_latitude").gte(minLatitude).lte(maxLatitude)
                .and("shop_status").is(ONLINE.getName());
        Query query = Query.query(criteria);
        return query(query);
    }

    /**
     * 将所有机构查询出来,
     * @param page
     * @param shopName
     * @return
     */
    public Page<MizarShop> loadAllByPage(Pageable page, String shopName) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(shopName)) {
            shopName = StringRegexUtils.escapeExprSpecialWord(shopName);
            criteria = Criteria.where("full_name").regex(Pattern.compile(".*" + shopName + ".*"));
        }
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }

    public Page<MizarShop> loadAllByPage(Pageable page, String shopName,Boolean vip,Boolean  cooperator) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(shopName)) {
            shopName = StringRegexUtils.escapeExprSpecialWord(shopName);
            criteria = Criteria.where("full_name").regex(Pattern.compile(".*" + shopName + ".*"));
        }
        if(null != vip){
            criteria.and("vip").is(vip);
        }
        if(null != cooperator){
            criteria.and("cooperator").is(cooperator);
        }
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }


    public Page<MizarShop> loadByPage(Pageable page, String shopName) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(shopName)) {
            criteria = Criteria.where("full_name").regex(Pattern.compile(".*" + shopName + ".*")).and("shop_status").is(ONLINE.getName());
        }
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }

    // 获取全部ID 任务执行调用
    public Set<String> loadAllShopIds() {
        Query query = Query.query(new Criteria());
        query.field().includes("_id");
        return query(query).stream().map(MizarShop::getId).collect(Collectors.toSet());
    }

    public List<MizarShop> loadByParams(Integer regionCode, List<String> scs, List<String> fcs, boolean isVip) {
        Criteria criteria = new Criteria();
        if (regionCode != null && regionCode > 0) {
            criteria.and("region_code").is(regionCode);
        }
        if (CollectionUtils.isNotEmpty(scs)) {
            criteria.and("second_category").in(scs);
        }
        if (CollectionUtils.isNotEmpty(fcs)) {
            criteria.and("first_category").in(fcs);
        }
        criteria.and("vip").is(isVip);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<MizarShop> findByName(String name, int limit) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(name)) {
            name = StringRegexUtils.escapeExprSpecialWord(name);
            criteria = Criteria.where("full_name").regex(Pattern.compile(".*" + name + ".*"));
        }
        Query query = Query.query(criteria);
        if (limit > 0) query = query.limit(limit);
        return query(query);
    }

}
