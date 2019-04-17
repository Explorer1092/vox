package com.voxlearning.utopia.service.newhomework.impl.dao.bonus;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * @author lei.liu
 * @version 18-10-31
 */
@Named
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
@CacheBean(type = AbilityExamBasic.class, useValueWrapper = true, cacheName = "utopia-homework-cache")
public class AbilityExamBasicDao extends DynamicMongoShardPersistence<AbilityExamBasic, String> {
    @Override
    protected String calculateDatabase(String template, AbilityExamBasic document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, AbilityExamBasic document) {
        Objects.requireNonNull(document);
        long mod = SafeConverter.toLong(document.getId()) % (RuntimeMode.current().lt(Mode.STAGING) ? 2 : 10);
        return StringUtils.formatMessage(template, mod);
    }

    @Override
    protected void calculateCacheDimensions(AbilityExamBasic document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }
}
