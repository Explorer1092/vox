package com.voxlearning.utopia.service.newhomework.impl.dao.outside;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.lang.support.RangeableIdVersion;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingProcessResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.Objects;

@Named
@CacheBean(type = OutsideReadingProcessResult.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class OutsideReadingProcessResultDao extends DynamicMongoShardPersistence<OutsideReadingProcessResult, String> {

    @Override
    protected String calculateDatabase(String template, OutsideReadingProcessResult document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, OutsideReadingProcessResult document) {
        RangeableId rangeableId = RangeableIdVersion.V1.parse(document.getId());
        Objects.requireNonNull(rangeableId);
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.M).toString());
    }

    @Override
    protected void calculateCacheDimensions(OutsideReadingProcessResult document, Collection<String> dimensions) {
        dimensions.add(OutsideReadingProcessResult.ck_id(document.getId()));
    }
}
