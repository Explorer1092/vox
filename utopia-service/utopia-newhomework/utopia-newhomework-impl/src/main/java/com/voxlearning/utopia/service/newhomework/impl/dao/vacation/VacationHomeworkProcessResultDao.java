package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.lang.support.RangeableIdVersion;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2016/11/25
 */
@Named
@CacheBean(type = VacationHomeworkProcessResult.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class VacationHomeworkProcessResultDao extends DynamicMongoShardPersistence<VacationHomeworkProcessResult, String> {

    @Override
    protected String calculateDatabase(String template, VacationHomeworkProcessResult document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, VacationHomeworkProcessResult document) {
        RangeableId rangeableId = RangeableIdVersion.V1.parse(document.getId());
        Objects.requireNonNull(rangeableId);
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.D).toString());
    }

    @Override
    protected void calculateCacheDimensions(VacationHomeworkProcessResult document, Collection<String> dimensions) {
        dimensions.add(VacationHomeworkProcessResult.ck_id(document.getId()));
    }
}
