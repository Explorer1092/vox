package com.voxlearning.utopia.service.newhomework.impl.dao.shard;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkPractice;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = ShardHomeworkPractice.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class ShardHomeworkPracticeDao extends DynamicMongoShardPersistence<ShardHomeworkPractice, String> {
    @Override
    protected String calculateDatabase(String template, ShardHomeworkPractice document) {
        String month = document.parseIdMonth();
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, ShardHomeworkPractice document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(ShardHomeworkPractice document, Collection<String> dimensions) {
        dimensions.add(ShardHomeworkPractice.ck_id(document.getId()));
    }
}
