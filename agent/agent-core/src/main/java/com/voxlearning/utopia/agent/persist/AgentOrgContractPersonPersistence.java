package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentOrgContactPerson;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 异业机构联系人
 *
 * @author deliang.che
 * @since  2019/4/11
 */
@Named
@CacheBean(type = AgentOrgContactPerson.class)
public class AgentOrgContractPersonPersistence extends StaticCacheDimensionDocumentJdbcDao<AgentOrgContactPerson, Long> {

    @CacheMethod
    public List<AgentOrgContactPerson> queryByHoneycombId(@CacheParameter(value = "hid") Long honeycombId) {
        Criteria criteria = Criteria.where("HONEYCOMB_ID").is(honeycombId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<AgentOrgContactPerson>> queryByHoneycombIds(@CacheParameter(value = "hid", multiple = true) Collection<Long> honeycombId) {
        Criteria criteria = Criteria.where("HONEYCOMB_ID").in(honeycombId).and("DISABLED").is(false);
        return Optional.ofNullable(query(Query.query(criteria))).orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(AgentOrgContactPerson::getHoneycombId));
    }
}
