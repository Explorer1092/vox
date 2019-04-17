package com.voxlearning.utopia.admin.productpromotion.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageInfo;
import com.voxlearning.utopia.admin.productpromotion.dao.entity.ProductPromotionSmsEntity;

import javax.inject.Named;
import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-29 14:08
 **/
@Named
public class ProductPromotionSmsDao extends AlpsStaticMongoDao<ProductPromotionSmsEntity, String> {

    @Override
    protected void calculateCacheDimensions(ProductPromotionSmsEntity entity, Collection<String> collection) {

    }

    public List<ProductPromotionSmsEntity> query(String operationUserName, PageInfo pageInfo) {
        List<ProductPromotionSmsEntity> result = query(Query.query(Criteria.where("operationUserName").is(operationUserName)));
        return result
                .stream()
                .skip((pageInfo.getPage()) * (pageInfo.getSize()))
                .limit(pageInfo.getSize())
                .collect(Collectors.toList());
    }

    public List<ProductPromotionSmsEntity> query(Date beginTime, Date endTime) {
        return Optional.ofNullable(query(Query.query(Criteria.where("sendTime").lte(endTime).and("sendTime").gte(beginTime))))
                .orElse(Collections.emptyList()) ;
    }

    public Integer count(String operationUserName) {
        return Optional.ofNullable(query(Query.query(Criteria.where("operationUserName").is(operationUserName))))
                .orElse(Collections.emptyList()).size();
    }
}
