package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.permission.SystemRoleOperation;

import javax.inject.Named;
import java.util.List;

/**
 * SystemRoleOperationPersistence
 *
 * @author song.wang
 * @date 2018/6/11
 */
@Named
@CacheBean(type = SystemRoleOperation.class)
public class SystemRoleOperationPersistence extends StaticCacheDimensionDocumentJdbcDao<SystemRoleOperation, Long> {

    @CacheMethod(key = "ALL")
    public List<SystemRoleOperation> findAll(){
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<SystemRoleOperation> findByRoleId(@CacheParameter("rid") Integer roleId){
        Criteria criteria = Criteria.where("ROLE_ID").is(roleId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<SystemRoleOperation> findByOperationId(@CacheParameter("oid") Long operationId ){
        Criteria criteria = Criteria.where("OPERATION_ID").is(operationId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}
