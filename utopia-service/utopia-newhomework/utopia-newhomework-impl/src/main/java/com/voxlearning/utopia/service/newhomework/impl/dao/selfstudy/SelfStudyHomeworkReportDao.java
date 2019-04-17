package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author xuesong.zhang
 * @since 2017/3/22
 */
@Named
@CacheBean(type = SelfStudyHomeworkReport.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SelfStudyHomeworkReportDao extends AsyncDynamicMongoPersistence<SelfStudyHomeworkReport, String> {
    @Override
    protected String calculateDatabase(String template, SelfStudyHomeworkReport document) {
        SelfStudyHomeworkReport.ID id = document.parseID();
        String month = StringUtils.substring(id.getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SelfStudyHomeworkReport document) {
        return null;
    }

    @Override
    protected void calculateCacheDimensions(SelfStudyHomeworkReport document, Collection<String> dimensions) {
        dimensions.add(SelfStudyHomeworkReport.ck_id(document.getId()));
    }
}
