package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramRef;

import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-09-15 下午2:59
 **/
@Named
@CacheBean(type = UserMiniProgramRef.class)
public class UserMiniProgramRefPersistence extends StaticCacheDimensionDocumentJdbcDao<UserMiniProgramRef, Long> {


    @CacheMethod
    public UserMiniProgramRef findByOpenId(@CacheParameter("openId") String openId, @CacheParameter("type") Integer type) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId).and("TYPE").is(type).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public UserMiniProgramRef findByUserId(@CacheParameter("userId") Long userId, @CacheParameter("type") Integer type) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("TYPE").is(type).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }


    public void deleteUserMiniProgramRefByOpenId(String openId, Integer type) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId).and("TYPE").is(type).and("DISABLED").is(false);
        Update update = Update.update("DISABLED", true);
        long l = $update(update, criteria);
        if (l >= 1) {
            evictCacheByOpenIdType(openId, type);
        }
    }

    public void deleteUserMiniProgramRefByUserId(Long userId, Integer type) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("TYPE").is(type).and("DISABLED").is(false);
        Update update = Update.update("DISABLED", true);
        long l = $update(update, criteria);
        if (l >= 1) {
            evictCacheByUserIdType(userId, type);
        }
    }

    private void evictCacheByOpenIdType(String openId, Integer type) {
        UserMiniProgramRef userMiniProgramRef = new UserMiniProgramRef();
        userMiniProgramRef.setOpenId(openId);
        userMiniProgramRef.setType(type);
        evictDocumentCache(userMiniProgramRef);
    }

    private void evictCacheByUserIdType(Long userId, Integer type) {
        UserMiniProgramRef userMiniProgramRef = new UserMiniProgramRef();
        userMiniProgramRef.setUserId(userId);
        userMiniProgramRef.setType(type);
        evictDocumentCache(userMiniProgramRef);
    }
}
