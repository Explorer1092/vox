package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramCheck;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
public class UserMiniProgramCheckDao extends AlpsStaticMongoDao<UserMiniProgramCheck, String> {


    @Override
    protected void calculateCacheDimensions(UserMiniProgramCheck document, Collection<String> dimensions) {
        dimensions.add(UserMiniProgramCheck.ck_id(document.getId()));
        dimensions.add(UserMiniProgramCheck.ck_uid(document.getUid()));
        dimensions.add(UserMiniProgramCheck.ck_type(document.getType()));
    }

    @CacheMethod
    public UserMiniProgramCheck loadByUid(@CacheParameter("UID")Long uid, @CacheParameter("TYPE")MiniProgramType type) {
        Criteria criteria = Criteria.where("uid").is(uid).and("type").is(type);
        Sort sort = new Sort(Sort.Direction.DESC, "_id");
        Query query = Query.query(criteria).with(sort).limit(1);

        List<UserMiniProgramCheck> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void save(UserMiniProgramCheck po) {
        super.insert(po);
        evictCacheByUidType(po.getUid(), po.getType());
    }


    private void evictCacheByUidType(Long uid, MiniProgramType type) {
        UserMiniProgramCheck po = new UserMiniProgramCheck();
        po.setUid(uid);
        po.setType(type);
        evictDocumentCache(po);
    }

}
