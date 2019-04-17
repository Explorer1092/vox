package com.voxlearning.utopia.enanalyze.persistence.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.persistence.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户持久层mysql实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Repository
public class UserDaoJdbcImpl extends AlpsStaticJdbcDao<UserEntity, Long> implements UserDao {

    @Override
    public UserEntity findByOpenId(String openId) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @Override
    public Map<String, UserEntity> findByOpenIds(List<String> openIds) {
        Criteria criteria = Criteria.where("OPEN_ID").in(openIds);
        Query query = Query.query(criteria);
        List<UserEntity> rs = query(query);
        return rs.stream().collect(Collectors.toMap(UserEntity::getOpenId, i -> i));
    }

    @Override
    public long totalCount() {
        return count(Query.query(new Criteria()));
    }

//    @Override
//    public void insert(UserEntity user) {
//        super.insert(user);
//    }

    @Override
    public void update(UserEntity user) {
        Update update = new Update();
        if (StringUtils.isNotBlank(user.getNickName()))
            update.set("NICK_NAME", user.getNickName());
        if (StringUtils.isNotBlank(user.getGender()))
            update.set("GENDER", user.getGender());
        if (StringUtils.isNotBlank(user.getCity()))
            update.set("CITY", user.getCity());
        if (StringUtils.isNotBlank(user.getProvince()))
            update.set("PROVINCE", user.getProvince());
        if (StringUtils.isNotBlank(user.getAvatarUrl()))
            update.set("AVATAR_URL", user.getAvatarUrl());
        if (StringUtils.isNotBlank(user.getUnionId()))
            update.set("UNION_ID", user.getUnionId());
        if (StringUtils.isNotBlank(user.getSessionKey()))
            update.set("SESSION_KEY", user.getSessionKey());
        if (null != user.getUpdateDate())
            update.set("UPDATE_DATE", user.getUpdateDate());
        Criteria criteria = Criteria.where("OPEN_ID").is(user.getOpenId());
        super.$update(update, criteria);
    }

    @Override
    protected void calculateCacheDimensions(UserEntity document, Collection<String> dimensions) {

    }
}
