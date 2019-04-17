package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.api.concurrent.UninterruptiblyFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

/**
 * @author steven
 * @since 2017/1/23
 */
@Named
@CacheBean(type = SelfStudyHomework.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SelfStudyHomeworkDao extends AsyncDynamicMongoPersistence<SelfStudyHomework, String> {

    @Override
    protected String calculateDatabase(String template, SelfStudyHomework document) {
        String month = document.parseID().getMonth();
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SelfStudyHomework document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(SelfStudyHomework document, Collection<String> dimensions) {
        dimensions.add(SelfStudyHomework.ck_id(document.getId()));
    }

    public Boolean updateDisabledTrue(String homeworkId) {

        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(homeworkId);
        Update update = new Update();
        update.set("updateAt", new Date());
        update.set("disabled", Boolean.TRUE);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        MongoNamespace namespace = calculateIdMongoNamespace(homeworkId);
        UninterruptiblyFuture<SelfStudyHomework> future = $executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options);
        SelfStudyHomework modified = future.getUninterruptibly();

        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(SelfStudyHomework.ck_id(homeworkId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified != null;
    }
}
