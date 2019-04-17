package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerLinkMan;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerMarketer;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 合作机构市场专员关系
 * @author: kaibo.he
 * @create: 2019-04-03 15:39
 **/
@Named
@CacheBean(type = AgentPartnerMarketer.class)
public class AgentPartnerMarketerPersistence extends StaticCacheDimensionDocumentJdbcDao<AgentPartnerMarketer, Long> {

    @CacheMethod
    public List<AgentPartnerMarketer> queryByPartnerId(@CacheParameter(value = "pid")Long partnerId) {
        Criteria criteria = Criteria.where("PARTNER_ID").is(partnerId).and("DISABLED").is(false);;
        return super.query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<AgentPartnerMarketer>> queryByPartnerIds(@CacheParameter(value = "pid", multiple = true) Collection<Long> partnerIds) {
        if (CollectionUtils.isEmpty(partnerIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("PARTNER_ID").in(partnerIds).and("DISABLED").is(false);;
        return Optional.ofNullable(query(Query.query(criteria))).orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(AgentPartnerMarketer::getPartnerId));
    }

    @CacheMethod
    public List<AgentPartnerMarketer> queryByUseridId(@CacheParameter(value = "uid")Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);;
        return super.query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<AgentPartnerMarketer>> queryByUseridIds(@CacheParameter(value = "uid", multiple = true) Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("USER_ID").in(userIds).and("DISABLED").is(false);;
        return Optional.ofNullable(query(Query.query(criteria))).orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(AgentPartnerMarketer::getUserId));
    }

    public Integer removeByUserId(Long userId) {
        Integer rows = 0;
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("DISABLED").is(false);;
        List<AgentPartnerMarketer> partnerMarketerList = query(Query.query(criteria));
        if (CollectionUtils.isNotEmpty(partnerMarketerList)) {
            Update update = new Update();
            update.set("DISABLED", Boolean.TRUE);
            rows = (int)super.$update(update, criteria);
            if (rows > 0 && CollectionUtils.isNotEmpty(partnerMarketerList)) {
                Set<String> dimensions = new HashSet<>();
                partnerMarketerList.forEach(e -> calculateCacheDimensions(e, dimensions));
                getCache().delete(dimensions);
            }
        }
        return rows;
    }

}
