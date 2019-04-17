package com.voxlearning.utopia.service.mizar.impl.dao.hbs;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsUser;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by haitian.gan on 2017/2/15.
 */
@Named
@CacheBean(type = HbsUser.class,expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
public class HbsUserDao extends AlpsStaticJdbcDao<HbsUser,Long>{

    @Override
    protected void calculateCacheDimensions(HbsUser document, Collection<String> dimensions) {
        dimensions.add(HbsUser.ck_uid(document.getId()));
        dimensions.add(HbsUser.ck_uname(document.getUserName()));
    }

    @CacheMethod
    public HbsUser loadUser(@CacheParameter("UID") Long userId){
        return load(userId);
    }

    @CacheMethod
    public HbsUser loadUserByName(@CacheParameter("UNAME")String userName){
        Criteria criteria = Criteria.where("username").is(userName);
        Query query = Query.query(criteria);
        return query(query).stream().findAny().orElse(null);
    }

    public int updateUserName(Long userId, String phoneNumber) {
        HbsUser origin = load(userId);
        if(origin == null)
            return 0;

        Criteria criteria = Criteria.where("user_id").is(userId);
        Update update = Update.update("username",phoneNumber);
        int rows = (int)$update(update,criteria);
        if(rows > 0){
            evictDocumentCache(origin);
        }

        return rows;
    }

    public int updateUserPassword(Long userId,String password){
        HbsUser origin = load(userId);
        if(origin == null)
            return 0;

        Criteria criteria = Criteria.where("user_id").is(userId);
        Update update = Update.update("password",password);
        int rows = (int)$update(update,criteria);
        if(rows > 0){
            evictDocumentCache(origin);
        }

        return rows;
    }
}
