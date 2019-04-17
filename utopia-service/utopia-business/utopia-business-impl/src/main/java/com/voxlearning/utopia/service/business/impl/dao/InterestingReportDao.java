package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.entity.activity.InterestingReport;

import javax.inject.Named;
import java.util.Collection;

/**
 * Created by Summer on 2016/12/20.
 */
@Named
@CacheBean(type = InterestingReport.class)
public class InterestingReportDao extends AlpsStaticMongoDao<InterestingReport, Long> {

    @Override
    protected void calculateCacheDimensions(InterestingReport document, Collection<String> dimensions) {
        dimensions.add(InterestingReport.ck_id(document.getId()));
    }
}
