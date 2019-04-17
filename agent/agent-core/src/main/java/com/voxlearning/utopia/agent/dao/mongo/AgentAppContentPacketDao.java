package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.constants.AgentAppContentType;
import com.voxlearning.utopia.agent.constants.AgentDataPacketType;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 市场App内容数据
 * Created by yagaung.wang on 2016/8/2.
 */
@Named
@CacheBean(type = AgentAppContentPacket.class)
public class AgentAppContentPacketDao extends AlpsStaticMongoDao<AgentAppContentPacket, String> {
    @Override
    protected void calculateCacheDimensions(AgentAppContentPacket document, Collection<String> dimensions) {
        dimensions.add(AgentAppContentPacket.ck_content_type(document.getContentType()));
        dimensions.add(AgentAppContentPacket.ck_data_packet_type(document.getDatumType()));
    }

    @CacheMethod
    public List<AgentAppContentPacket> findByContentType(@CacheParameter(value = "type") AgentAppContentType contentType) {
        Criteria criteria = Criteria.where("contentType").is(contentType);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

    @CacheMethod
    public List<AgentAppContentPacket> findByDatumType(@CacheParameter(value = "d_type") AgentDataPacketType datumType) {
        Criteria criteria = Criteria.where("datumType").is(datumType);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

    /* @CacheMethod
     public List<AgentAppContentPacket> findByCityCode(Collection<Integer> regionCode) {
         Criteria criteria = Criteria.where("activityCity.cityCode").in(regionCode);
         Query query = Query.query(criteria);
         return query(query);
     }
 */
    //删除
    public boolean deleteAgentAppContentPacket(String id) {
        AgentAppContentPacket content = load(id);
        if (content == null) {
            return false;
        }
        content.setDisabled(true);
        content = replace(content);
        return content != null;
    }
}
