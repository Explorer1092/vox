package com.voxlearning.utopia.service.newhomework.impl.dao.basicreview;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkReport;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Named
@CacheBean(type = BasicReviewHomeworkReport.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class BasicReviewHomeworkReportDao extends DynamicMongoShardPersistence<BasicReviewHomeworkReport, String> {

    @Override
    protected String calculateDatabase(String template, BasicReviewHomeworkReport document) {
        BasicReviewHomeworkReport.ID id = document.parseId();
        String month = StringUtils.substring(id.getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, BasicReviewHomeworkReport document) {
        BasicReviewHomeworkReport.ID id = document.parseId();
        return StringUtils.formatMessage(template, id.getDay());
    }

    @Override
    protected void calculateCacheDimensions(BasicReviewHomeworkReport document, Collection<String> dimensions) {
        dimensions.add(BasicReviewHomeworkReport.ck_id(document.getId()));
    }
}
