package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarReserveRecord;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 16/8/17.
 */
@Named
@CacheBean(type = MizarReserveRecord.class)
public class MizarReserveRecordPersistence extends AlpsStaticJdbcDao<MizarReserveRecord, Long> {

    @Override
    protected void calculateCacheDimensions(MizarReserveRecord document, Collection<String> dimensions) {
        dimensions.add(MizarReserveRecord.ck_id(document.getId()));
        dimensions.add(MizarReserveRecord.ck_parentId(document.getParentId()));
        dimensions.add(MizarReserveRecord.ck_shopId(document.getShopId()));
        dimensions.add(MizarReserveRecord.ck_mobileAndShopId(document.getMobile(), document.getShopId()));
        dimensions.add(MizarReserveRecord.ck_mobileAndgoodsId(document.getMobile(), document.getShopGoodsId()));
        dimensions.add(MizarReserveRecord.ck_schoolIdAndShopId(document.getSchoolId(), document.getShopId()));
        dimensions.add(MizarReserveRecord.ck_goodsId(document.getShopGoodsId()));
    }

    @CacheMethod
    public List<MizarReserveRecord> loadByParentId(@CacheParameter("parentId") Long parentId) {
        Criteria criteria = Criteria.where("PARENT_ID").is(parentId).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    @CacheMethod
    public Map<String, List<MizarReserveRecord>> loadBySchoolIdAndShopId(@CacheParameter("schoolId") Long schoolId,
                                                                         @CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds) {
        return query(Query.query(Criteria.where("SCHOOL_ID").is(schoolId).and("SHOP_ID").in(shopIds).and("DISABLED").is(false)))
                .stream().collect(Collectors.groupingBy(MizarReserveRecord::getShopId));
    }

    // 默认查询最近90天内的数据
    @CacheMethod
    public List<MizarReserveRecord> loadByShopId(@CacheParameter("shopId") String shopId) {
        Date startDate = DateUtils.calculateDateDay(new Date(), -90);
        return query(Query.query(Criteria.where("SHOP_ID").is(shopId).and("DISABLED").is(false).and("CREATE_DATETIME").gte(startDate)));
    }

    @CacheMethod
    public Map<String, List<MizarReserveRecord>> loadByShopIds(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds) {
        Set<String> shopIdSet = CollectionUtils.toLinkedHashSet(shopIds);
        if (CollectionUtils.isEmpty(shopIdSet)) {
            return Collections.emptyMap();
        }
        return query(Query.query(Criteria.where("SHOP_ID").in(shopIdSet).and("DISABLED").is(false)))
                .stream()
                .collect(Collectors.groupingBy(MizarReserveRecord::getShopId));
    }

    @CacheMethod
    public List<MizarReserveRecord> loadByParentIdAndShopId(@CacheParameter("PID") Long parentId, @CacheParameter("SID") String shopId) {
        Criteria criteria = Criteria.where("PARENT_ID").is(parentId)
                .and("SHOP_ID").is(shopId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<MizarReserveRecord> loadByMobileAndShopId(@CacheParameter("M") String mobile, @CacheParameter("SID") String shopId) {
        Criteria criteria = Criteria.where("MOBILE").is(mobile)
                .and("SHOP_ID").is(shopId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<String, List<MizarReserveRecord>> loadByMobileAndGoodsId(@CacheParameter(value = "M", multiple = true) Collection<String> mobile, @CacheParameter("GID") String goodsId) {
        Criteria criteria = Criteria.where("MOBILE").in(mobile)
                .and("SHOP_GOODS_ID").is(goodsId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(MizarReserveRecord::getMobile));
    }

    @CacheMethod
    public List<MizarReserveRecord> loadGoodsRecords(@CacheParameter("GID") String goodsId) {
        Criteria criteria = Criteria.where("SHOP_GOODS_ID").is(goodsId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<MizarReserveRecord> loadByPeriod(Date start, Date end) {
        if (start == null || end == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("DISABLED").is(false)
                .and("CREATE_DATETIME").gte(start).lte(end);
        return query(Query.query(criteria));
    }

    public int updateStatus(Collection<Long> records, MizarReserveRecord.Status status) {
        records = CollectionUtils.toLinkedHashSet(records);
        if (CollectionUtils.isEmpty(records) || status == null) {
            return 0;
        }
        List<MizarReserveRecord> originals = new ArrayList<>($loads(records).values());
        if (CollectionUtils.isEmpty(originals)) {
            return 0;
        }
        Criteria criteria = Criteria.where("ID").in(records);
        Update update = Update.update("STATUS", status);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            originals.forEach(o -> calculateCacheDimensions(o, cacheKeys));
            getCache().delete(cacheKeys);
        }
        return rows;
    }
}
