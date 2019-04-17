package com.voxlearning.utopia.agent.persist.honeycomb;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombFans;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type = HoneycombFans.class)
public class HoneycombFansDao extends StaticCacheDimensionDocumentMongoDao<HoneycombFans, String> {

    @CacheMethod
    public List<HoneycombFans> loadByHid(@CacheParameter("hid") Long honeycombId){
        Criteria criteria = Criteria.where("honeycombId").is(honeycombId);
        return query(Query.query(criteria));
    }
}
