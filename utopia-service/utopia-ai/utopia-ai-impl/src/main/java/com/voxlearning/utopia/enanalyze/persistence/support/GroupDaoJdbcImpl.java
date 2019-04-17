package com.voxlearning.utopia.enanalyze.persistence.support;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.enanalyze.entity.GroupEntity;
import com.voxlearning.utopia.enanalyze.persistence.GroupDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 群持久层实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Repository
public class GroupDaoJdbcImpl extends AlpsStaticJdbcDao<GroupEntity, Long> implements GroupDao {

    @Override
    public GroupEntity findByOpenGroupId(String openGroupId) {
        Criteria criteria = Criteria.where("OPEN_GROUP_ID").is(openGroupId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @Override
    public List<GroupEntity> findByOpenId(String openId) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @Override
    protected void calculateCacheDimensions(GroupEntity document, Collection<String> dimensions) {

    }
}
