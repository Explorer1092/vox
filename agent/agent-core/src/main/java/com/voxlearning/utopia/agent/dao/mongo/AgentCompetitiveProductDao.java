package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentCompetitiveProduct;

import javax.inject.Named;
import java.util.List;

/**
 * 学校竞品收集Dao层
 * @author deliang.che
 * @date 2018/3/9
 */
@Named
@CacheBean(type = AgentCompetitiveProduct.class)
public class AgentCompetitiveProductDao extends StaticCacheDimensionDocumentMongoDao<AgentCompetitiveProduct, String> {

    @CacheMethod
    public List<AgentCompetitiveProduct> loadBySchoolId(@CacheParameter("sid") Long schoolId){
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        return query(Query.query(criteria));
    }
}
