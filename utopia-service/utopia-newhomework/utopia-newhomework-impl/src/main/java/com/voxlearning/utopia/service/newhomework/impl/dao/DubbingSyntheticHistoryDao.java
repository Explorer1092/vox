package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsDynamicMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Named
@CacheBean(type = DubbingSyntheticHistory.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(value = CacheDimensionDistribution.ID_FIELD)
public class DubbingSyntheticHistoryDao extends AlpsDynamicMongoDao<DubbingSyntheticHistory, String> {
    @Override
    protected void calculateCacheDimensions(DubbingSyntheticHistory document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateDatabase(String template, DubbingSyntheticHistory document) {
        DubbingSyntheticHistory.ID id = document.parseID();
        if(id == null) return null;
        String[] subHomeworkSegments = StringUtils.split(id.getHomeworkId(), "_");
        String[] vacationHomeworkSegments = StringUtils.split(id.getHomeworkId(), "-");
        String[] liveCastHomeworkSegments = StringUtils.split(id.getHomeworkId(), "_");

        if (subHomeworkSegments.length == 3){
            return StringUtils.formatMessage(template, subHomeworkSegments[0]);
        }else if(vacationHomeworkSegments.length == 4){
            return StringUtils.formatMessage(template, DateUtils.dateToString(new ObjectId(vacationHomeworkSegments[0]).getDate(), "yyyyMM"));
        }else if(subHomeworkSegments.length == 2){
            return StringUtils.formatMessage(template, liveCastHomeworkSegments[0]);
        }else {
            return null;
        }
    }

    @Override
    protected String calculateCollection(String template, DubbingSyntheticHistory document) {
        return null;
    }

    public boolean updateSyntheticState(String id, Boolean success) {
        if (StringUtils.isBlank(id)) {
            return false;
        }

        Criteria criteria = Criteria.where("_id").is(id);
        Date d = new Date();
        Update update = new Update();
        update.set("syntheticSuccess", success);
        update.set("updateAt", d);

        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
        DubbingSyntheticHistory modified = executeFindOneAndUpdate(createMongoConnection(namespace), criteria, update, options);
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(DubbingSyntheticHistory.generateCacheKey(id))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified != null;
    }
}
