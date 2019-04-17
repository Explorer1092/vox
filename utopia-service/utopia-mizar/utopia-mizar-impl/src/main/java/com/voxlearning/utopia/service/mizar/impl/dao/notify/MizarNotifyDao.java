package com.voxlearning.utopia.service.mizar.impl.dao.notify;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Yuechen.Wang on 2016/12/1.
 */
@Named
@CacheBean(type = MizarNotify.class)
public class MizarNotifyDao extends StaticCacheDimensionDocumentMongoDao<MizarNotify, String> {

    @CacheMethod
    public List<MizarNotify> loadByCreator(@CacheParameter("C") String creator) {
        Criteria criteria = Criteria.where("creator").is(creator);
        return query(Query.query(criteria));
    }
}
