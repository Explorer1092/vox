package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.permission.SystemOperation;

import javax.inject.Named;
import java.util.List;

/**
 * SystemOperationPersistence
 *
 * @author song.wang
 * @date 2018/6/11
 */
@Named
@CacheBean(type = SystemOperation.class)
public class SystemOperationPersistence extends StaticCacheDimensionDocumentJdbcDao<SystemOperation, Long> {

    @CacheMethod(key = "ALL")
    public List<SystemOperation> findAll(){
        Criteria criteria = Criteria.where("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
