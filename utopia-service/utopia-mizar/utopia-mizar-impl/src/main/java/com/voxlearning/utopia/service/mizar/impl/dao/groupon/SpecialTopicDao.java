package com.voxlearning.utopia.service.mizar.impl.dao.groupon;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by yuechen.wang on 2016/10/18.
 */
@Named
@CacheBean(type = SpecialTopic.class)
public class SpecialTopicDao extends AlpsStaticMongoDao<SpecialTopic, String> {

    @Override
    protected void calculateCacheDimensions(SpecialTopic document, Collection<String> dimensions) {
        dimensions.add(SpecialTopic.ck_all());
        dimensions.add(SpecialTopic.ck_active());
        dimensions.add(SpecialTopic.ck_id(document.getId()));
    }

    @CacheMethod(key = "ALL")
    public List<SpecialTopic> loadAllSpecialTopic() {
        return query();
    }

    @CacheMethod(key = "active")
    public List<SpecialTopic> loadActiveSpecialTopic() {
        Date now = new Date();
        Criteria criteria = Criteria.where("status").is("ONLINE").and("startTime").lte(now).and("endTime").gte(now);
        Query query = Query.query(criteria);
        return query(query);
    }
}
