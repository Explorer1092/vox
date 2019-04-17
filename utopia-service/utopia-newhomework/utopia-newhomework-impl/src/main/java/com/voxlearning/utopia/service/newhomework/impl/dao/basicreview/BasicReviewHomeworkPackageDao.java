package com.voxlearning.utopia.service.newhomework.impl.dao.basicreview;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Named
@CacheBean(type = BasicReviewHomeworkPackage.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class BasicReviewHomeworkPackageDao extends StaticMongoShardPersistence<BasicReviewHomeworkPackage, String> {
    @Override
    protected void calculateCacheDimensions(BasicReviewHomeworkPackage document, Collection<String> dimensions) {
        dimensions.add(BasicReviewHomeworkPackage.ck_id(document.getId()));
        dimensions.add(BasicReviewHomeworkPackage.ck_clazzGroupId(document.getClazzGroupId()));
    }

    @CacheMethod
    public Map<Long, List<BasicReviewHomeworkPackage>> loadBasicReviewHomeworkPackageByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {
        //根据时间过滤历史数据:时间
        Date dueDate = DateUtils.stringToDate("2018-11-24 00:00:00");
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds).and("disabled").is(Boolean.FALSE).and("createAt").gte(dueDate);
        Query query = Query.query(criteria);
        Map<Long, List<BasicReviewHomeworkPackage>> ret = query(query).stream()
                .collect(Collectors.groupingBy(BasicReviewHomeworkPackage::getClazzGroupId));
        // 为空时返回空list，避免空击穿
        return groupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    public Boolean updateDisableTrue(String packageId) {
        if (StringUtils.isBlank(packageId)) {
            return false;
        }
        Criteria criteria = Criteria.where("_id").is(packageId);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", Boolean.TRUE);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER)
                .upsert(false);

        BasicReviewHomeworkPackage modified = $executeFindOneAndUpdate(createMongoConnection(calculateIdMongoNamespace(packageId), packageId), criteria, update, options).getUninterruptibly();
        if (modified != null) {
            Set<String> dimensions = new HashSet<>();
            calculateCacheDimensions(modified, dimensions);
            getCache().deletes(dimensions);
        }
        return modified != null;
    }
}
