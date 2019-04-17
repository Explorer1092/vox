package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.permission.SystemRolePageElement;

import javax.inject.Named;
import java.util.List;

/**
 * SystemRolePageElementPersistence
 *
 * @author song.wang
 * @date 2018/5/16
 */
@Named
@CacheBean(type = SystemRolePageElement.class)
public class SystemRolePageElementPersistence extends StaticCacheDimensionDocumentJdbcDao<SystemRolePageElement, Long> {

    @CacheMethod(key = "ALL")
    public List<SystemRolePageElement> findAll(){
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<SystemRolePageElement> findByRoleId(@CacheParameter("rid") Integer roleId){
        Criteria criteria = Criteria.where("ROLE_ID").is(roleId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<SystemRolePageElement> findByPageElementId(@CacheParameter("peId") Long pageElementId ){
        Criteria criteria = Criteria.where("PAGE_ELEMENT_ID").is(pageElementId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}
