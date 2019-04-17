package com.voxlearning.utopia.enanalyze.persistence.support;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.persistence.UserGroupDao;
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
public class UserGroupDaoJdbcImpl extends AlpsStaticJdbcDao<UserGroupEntity, Long> implements UserGroupDao {

    @Override
    public List<UserGroupEntity> findByOpenId(String openId) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @Override
    public List<UserGroupEntity> findByGroupId(String openGroupId) {
        Criteria criteria = Criteria.where("OPEN_GROUP_ID").is(openGroupId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @Override
    public void delete(String openId, String openGroupId) {
        $remove(Criteria.where("OPEN_ID").is(openId).and("OPEN_GROUP_ID").is(openGroupId));
    }

    @Override
    protected void calculateCacheDimensions(UserGroupEntity document, Collection<String> dimensions) {

    }
}
