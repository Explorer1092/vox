package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.permission.SystemPageElement;

import javax.inject.Named;
import java.util.List;

/**
 * SystemPageElementPersistence
 *
 * @author song.wang
 * @date 2018/5/16
 */
@Named
@CacheBean(type = SystemPageElement.class)
public class SystemPageElementPersistence extends StaticCacheDimensionDocumentJdbcDao<SystemPageElement, Long> {

    @CacheMethod(key = "ALL")
    public List<SystemPageElement> findAll(){
        Criteria criteria = Criteria.where("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
