package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.async.AsyncDynamicMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkPractice;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Named
@UtopiaCacheSupport(value = SubHomeworkPractice.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SubHomeworkPracticeDao extends AsyncDynamicMongoDao<SubHomeworkPractice, String> {

    @Override
    protected String calculateDatabase(String template, SubHomeworkPractice document) {
        String month = document.parseIdMonth();
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SubHomeworkPractice document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(SubHomeworkPractice document, Collection<String> dimensions) {
        dimensions.add(SubHomeworkPractice.ck_id(document.getId()));
    }
}
