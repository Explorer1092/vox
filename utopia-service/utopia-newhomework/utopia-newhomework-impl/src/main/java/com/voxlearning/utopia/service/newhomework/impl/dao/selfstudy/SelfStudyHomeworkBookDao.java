package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkBook;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author steven
 * @since 2017/1/24
 */
@Named
@CacheBean(type = SelfStudyHomeworkBook.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SelfStudyHomeworkBookDao extends AsyncDynamicMongoPersistence<SelfStudyHomeworkBook, String> {

    @Override
    protected String calculateDatabase(String template, SelfStudyHomeworkBook document) {
        SelfStudyHomeworkBook.ID id = document.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected String calculateCollection(String template, SelfStudyHomeworkBook document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(SelfStudyHomeworkBook document, Collection<String> dimensions) {
        dimensions.add(SelfStudyHomeworkBook.ck_id(document.getId()));
    }
}
