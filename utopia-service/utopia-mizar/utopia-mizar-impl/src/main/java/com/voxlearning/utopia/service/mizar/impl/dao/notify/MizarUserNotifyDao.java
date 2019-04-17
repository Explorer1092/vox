package com.voxlearning.utopia.service.mizar.impl.dao.notify;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarUserNotify;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.ReturnDocument.AFTER;

/**
 * Created by Yuechen.Wang on 2016/12/1.
 */
@Named
@CacheBean(type = MizarUserNotify.class)
public class MizarUserNotifyDao extends StaticCacheDimensionDocumentMongoDao<MizarUserNotify, String> {

    // 这个加载出来的时候未根据 disabled 字段过滤，方便发送人回溯
    @CacheMethod
    public List<MizarUserNotify> loadByUser(@CacheParameter("U") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(new Query(criteria));
    }

    public boolean updateFlag(String refId, String field) {
        Criteria criteria = Criteria.where("_id").is(refId);
        Update update = Update.update(field, true);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER);

        MizarUserNotify notify = executeFindOneAndUpdate(createMongoConnection(), criteria, update, options);
        if (notify == null) {
            return false;
        }
        evictDocumentCache(notify);
        return true;
    }
}
