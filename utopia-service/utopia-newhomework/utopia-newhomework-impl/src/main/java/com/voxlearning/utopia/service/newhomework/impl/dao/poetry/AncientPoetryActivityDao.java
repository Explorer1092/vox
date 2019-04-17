package com.voxlearning.utopia.service.newhomework.impl.dao.poetry;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/21
 */
@Named
@CacheBean(type = AncientPoetryActivity.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class AncientPoetryActivityDao extends StaticMongoShardPersistence<AncientPoetryActivity, String> {

    @Override
    protected void calculateCacheDimensions(AncientPoetryActivity document, Collection<String> dimensions) {
        dimensions.add(AncientPoetryActivity.ck_id(document.getId()));
    }

    public List<AncientPoetryActivity> loadByDate(Date startDate, Date endDate) {
        Criteria criteria = Criteria.where("startDate").lt(startDate)
                .and("endDate").gt(endDate);
        Query query = Query.query(criteria);
        return query(query);
    }

    /**
     * CRM
     * 获取所有的活动
     * @return
     */
    public List<AncientPoetryActivity> loadAllActivity() {
        Query query = Query.query(new Criteria());
        return query(query);
    }

    /**
     * 更新活动状态
     * @param activityId
     * @param status
     * @return
     */
    public Boolean updateActivityStatus(String activityId, boolean status) {
        if (StringUtils.isBlank(activityId)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(activityId);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", status);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

        IMongoConnection connection = createMongoConnection(calculateIdMongoNamespace(activityId), activityId);
        AncientPoetryActivity modified = $executeFindOneAndUpdate(connection, criteria, update, options).getUninterruptibly();
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(AncientPoetryActivity.ck_id(activityId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();

            String ck_cg = AncientPoetryActivity.ck_id(modified.getId());
            getCache().delete(ck_cg);
        }
        return modified != null;
    }
}
