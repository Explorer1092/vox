package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusDubbing;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = PictureBookPlusDubbing.class, cacheName = "utopia-homework-cache", useEagerInsert = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class PictureBookPlusDubbingDao extends DynamicMongoShardPersistence<PictureBookPlusDubbing, String> {
    @Override
    protected String calculateDatabase(String template, PictureBookPlusDubbing document) {
        PictureBookPlusDubbing.ID id = document.parseID();
        if (id == null) return null;
        String[] subHomeworkSegments = StringUtils.split(id.getHomeworkId(), "_");
        String[] vacationHomeworkSegments = StringUtils.split(id.getHomeworkId(), "-");
        String[] liveCastHomeworkSegments = StringUtils.split(id.getHomeworkId(), "_");

        if (subHomeworkSegments.length == 3) {
            return StringUtils.formatMessage(template, subHomeworkSegments[0]);
        } else if (vacationHomeworkSegments.length == 4) {
            return StringUtils.formatMessage(template, DateUtils.dateToString(new ObjectId(vacationHomeworkSegments[0]).getDate(), "yyyyMM"));
        } else if(subHomeworkSegments.length == 2){
            return StringUtils.formatMessage(template, liveCastHomeworkSegments[0]);
        } else {
            return null;
        }
    }

    @Override
    protected String calculateCollection(String template, PictureBookPlusDubbing document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(PictureBookPlusDubbing document, Collection<String> dimensions) {
        dimensions.add(PictureBookPlusDubbing.ck_id(document.getId()));
    }
}
