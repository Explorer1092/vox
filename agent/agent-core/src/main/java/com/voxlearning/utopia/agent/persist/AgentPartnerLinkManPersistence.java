package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.partner.AgentPartnerLinkMan;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 合作机构联系人关系持久层
 * @author: kaibo.he
 * @create: 2019-04-02 10:42
 **/
@Named
@CacheBean(type = AgentPartnerLinkMan.class)
public class AgentPartnerLinkManPersistence extends StaticCacheDimensionDocumentJdbcDao<AgentPartnerLinkMan, Long> {

    @CacheMethod
    public List<AgentPartnerLinkMan> queryByPartnerId(@CacheParameter(value = "pid")Long partnerId) {
        Criteria criteria = Criteria.where("PARTNER_ID").is(partnerId).and("DISABLED").is(false);
        return super.query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<AgentPartnerLinkMan>> queryByPartnerIds(@CacheParameter(value = "pid", multiple = true) Collection<Long> partnerIds) {
        if (CollectionUtils.isEmpty(partnerIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("PARTNER_ID").in(partnerIds).and("DISABLED").is(false);
        return Optional.ofNullable(query(Query.query(criteria))).orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(AgentPartnerLinkMan::getPartnerId));
    }

    public void insert(Long linkManId, Long partnerId) {
        AgentPartnerLinkMan man = new AgentPartnerLinkMan();
        man.setLinkManId(linkManId);
        man.setPartnerId(partnerId);
        this.insert(man);
    }

    public Integer removeByPartnerIds(Collection<Long> partnerIds) {
        Criteria criteria = Criteria.where("PARTNER_ID").in(partnerIds).and("DISABLED").is(false);
        List<AgentPartnerLinkMan> partnerLinkManList = query(Query.query(criteria));
        return doRemove(partnerLinkManList, criteria);
    }

    public Integer removeByLinkManIds(Collection<Long> linkManIds) {
        Criteria criteria = Criteria.where("LINK_MAN_ID").in(linkManIds).and("DISABLED").is(false);
        List<AgentPartnerLinkMan> list = query(Query.query(criteria));
        return doRemove(list, criteria);
    }

    private Integer doRemove(List<AgentPartnerLinkMan> list, Criteria criteria) {
        Integer rows = 0;
        if (CollectionUtils.isNotEmpty(list)) {
            Update update = new Update();
            update.set("DISABLED", true);
            rows = (int)super.$update(update, criteria);
            if (rows > 0 && CollectionUtils.isNotEmpty(list)) {
                Set<String> dimensions = new HashSet<>();
                list.forEach(e -> calculateCacheDimensions(e, dimensions));
                getCache().delete(dimensions);
            }
        }
        return rows;
    }
}
