package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author zhangbin
 * @since 2017/2/8 14:05
 */
@Named
@CacheBean(type = NewHomeworkSyllable.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class NewHomeworkSyllableDao extends AsyncDynamicMongoPersistence<NewHomeworkSyllable, String> {

    @Override
    protected void calculateCacheDimensions(NewHomeworkSyllable document, Collection<String> dimensions) {
        dimensions.add(NewHomeworkSyllable.ck_id(document.getId()));
    }

    @Override
    protected String calculateDatabase(String template, NewHomeworkSyllable document) {
        String month = StringUtils.substring(document.parseID().getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, NewHomeworkSyllable document) {
        return StringUtils.formatMessage(template, document.parseID().getDay());
    }
}
