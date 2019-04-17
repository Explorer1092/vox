package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartner;

import javax.inject.Named;
import java.util.List;

/**
 * @description: 合作机构持久层
 * @author: kaibo.he
 * @create: 2019-04-02 10:37
 **/
@Named
@CacheBean(type = AgentPartner.class)
public class AgentPartnerPersistence extends StaticCacheDimensionDocumentJdbcDao<AgentPartner, Long> {

    /**
     * 根据机构名称查询
     * @param name
     * @return
     */
    @CacheMethod
    public List<AgentPartner> queryByName(@CacheParameter(value = "name")String name) {
        Criteria criteria = Criteria.where("NAME").is(name).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}
