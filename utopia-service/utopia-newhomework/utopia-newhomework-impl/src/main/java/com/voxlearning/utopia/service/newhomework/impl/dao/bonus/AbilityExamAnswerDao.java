package com.voxlearning.utopia.service.newhomework.impl.dao.bonus;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamAnswer;

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
@CacheBean(type = AbilityExamAnswer.class, cacheName = "utopia-homework-cache")
public class AbilityExamAnswerDao extends DynamicMongoShardPersistence<AbilityExamAnswer, String> {
    @Override
    protected String calculateDatabase(String template, AbilityExamAnswer document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, AbilityExamAnswer document) {
        Objects.requireNonNull(document);
        long mod = document.fetchUserId() % (RuntimeMode.current().lt(Mode.STAGING) ? 2 : 10);
        return StringUtils.formatMessage(template, mod);
    }

    @Override
    protected void calculateCacheDimensions(AbilityExamAnswer document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

}
