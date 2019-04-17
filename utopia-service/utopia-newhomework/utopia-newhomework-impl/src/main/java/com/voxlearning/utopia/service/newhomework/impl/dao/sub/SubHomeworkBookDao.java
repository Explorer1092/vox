package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.async.AsyncDynamicMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkBook;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xuesong.zhang
 * @since 2017/1/13
 */
@Named
@UtopiaCacheSupport(value = SubHomeworkBook.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SubHomeworkBookDao extends AsyncDynamicMongoDao<SubHomeworkBook, String> {

    @Override
    protected String calculateDatabase(String template, SubHomeworkBook document) {
        SubHomeworkBook.ID id = document.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected String calculateCollection(String template, SubHomeworkBook document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(SubHomeworkBook document, Collection<String> dimensions) {
        dimensions.add(SubHomeworkBook.ck_id(document.getId()));
    }
}
