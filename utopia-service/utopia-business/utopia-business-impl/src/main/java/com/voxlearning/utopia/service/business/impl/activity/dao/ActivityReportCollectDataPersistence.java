package com.voxlearning.utopia.service.business.impl.activity.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named("com.voxlearning.utopia.service.business.impl.activity.dao.ActivityReportCollectDataPersistence")
@CacheBean(type = ActivityReportCollectData.class)
public class ActivityReportCollectDataPersistence extends AlpsStaticJdbcDao<ActivityReportCollectData, String> {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql miscSql;

    @Override
    public void afterPropertiesSet() {
        miscSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    @Override
    protected void calculateCacheDimensions(ActivityReportCollectData document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public void deleteAll() {
        String deleteSql = "DELETE FROM VOX_ACTIVITY_REPORT_COLLECT_DATA WHERE 1=1";
        miscSql.withSql(deleteSql).executeUpdate();
    }

    public void deleteActivityId(String activityId) {
        String deleteSql = "DELETE FROM VOX_ACTIVITY_REPORT_COLLECT_DATA WHERE ACTIVITY_ID = '" + activityId + "'";
        miscSql.withSql(deleteSql).executeUpdate();
    }

}
