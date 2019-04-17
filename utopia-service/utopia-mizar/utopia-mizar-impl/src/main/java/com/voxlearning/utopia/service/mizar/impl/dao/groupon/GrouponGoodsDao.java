package com.voxlearning.utopia.service.mizar.impl.dao.groupon;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsDataSourceType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xiang.lv on 2016/9/21.
 *
 * @author xiang.lv
 * @date 2016/9/21   11:52
 */
@Named
@CacheBean(type = GrouponGoods.class)
public class GrouponGoodsDao extends AlpsStaticMongoDao<GrouponGoods, String> {

    @Override
    protected void calculateCacheDimensions(GrouponGoods document, Collection<String> dimensions) {
        dimensions.add(GrouponGoods.ck_category(document.getCategoryCode()));
        dimensions.add(GrouponGoods.ck_id(document.getId()));
    }

    @CacheMethod
    public List<GrouponGoods> loadByCategory(@CacheParameter(value = "CC") String categoryCode) {
        if (StringUtils.isBlank(categoryCode)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("category_code").is(categoryCode);//.and("status").is(ONLINE.name());
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public Map<String, List<GrouponGoods>> loadByCategory(@CacheParameter(value = "CC", multiple = true) Collection<String> categoryCodes) {
        if (CollectionUtils.isEmpty(categoryCodes)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("category_code").in(categoryCodes);//.and("status").is(ONLINE.name());
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(GrouponGoods::getCategoryCode));
    }

    public List<GrouponGoods> getGrouponGoodsByDataSouce(final GrouponGoodsDataSourceType dataSourceType) {
        if (Objects.isNull(dataSourceType)) {
            return Collections.emptyList();

        }
        Criteria criteria = Criteria.where("data_source").is(dataSourceType.getCode());
        return query(Query.query(criteria));
    }

}
