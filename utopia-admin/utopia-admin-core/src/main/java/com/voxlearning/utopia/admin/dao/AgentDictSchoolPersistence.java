package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.agent.AgentDictSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * AgentDictSchoolPersistence
 *
 * @author song.wang
 * @date 2016/6/24
 */
@Named("admin.AgentDictSchoolPersistence")
@CacheBean(type = AgentDictSchool.class)
public class AgentDictSchoolPersistence extends AlpsStaticJdbcDao<AgentDictSchool, Long> {

    @Override
    protected void calculateCacheDimensions(AgentDictSchool source, Collection<String> dimensions) {
        dimensions.add(AgentDictSchool.ck_all());
        dimensions.add(AgentDictSchool.ck_sId(source.getSchoolId()));
    }

    @CacheMethod(key = "ALL")
    public List<AgentDictSchool> findAllDictSchool() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentDictSchool> findBySchoolId(@CacheParameter("s_id") Long schoolId) {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        criteria.and("SCHOOL_ID").is(schoolId);
        Query query = Query.query(criteria);
        return query(query);
    }
}
