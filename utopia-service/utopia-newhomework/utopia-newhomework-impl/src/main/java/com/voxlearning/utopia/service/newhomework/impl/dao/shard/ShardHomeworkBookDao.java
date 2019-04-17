package com.voxlearning.utopia.service.newhomework.impl.dao.shard;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkBook;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = ShardHomeworkBook.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class ShardHomeworkBookDao extends DynamicMongoShardPersistence<ShardHomeworkBook, String> {

    @Override
    protected void calculateCacheDimensions(ShardHomeworkBook document, Collection<String> dimensions) {
        dimensions.add(ShardHomeworkBook.ck_id(document.getId()));
    }

    @Override
    protected String calculateDatabase(String template, ShardHomeworkBook document) {
        ShardHomeworkBook.ID id = document.parseId();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected String calculateCollection(String template, ShardHomeworkBook document) {
        return null;
    }
}
