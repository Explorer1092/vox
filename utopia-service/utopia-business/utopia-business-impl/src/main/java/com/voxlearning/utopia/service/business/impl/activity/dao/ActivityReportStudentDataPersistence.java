package com.voxlearning.utopia.service.business.impl.activity.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.dao.AlpsDynamicJdbDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportStudentData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@CacheBean(type = ActivityReportStudentData.class)
@Named("com.voxlearning.utopia.service.business.impl.activity.dao.ActivityReportStudentDataPersistence")
public class ActivityReportStudentDataPersistence extends AlpsDynamicJdbDao<ActivityReportStudentData, String> {

    @Override
    protected void calculateCacheDimensions(ActivityReportStudentData document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateTableName(String template, ActivityReportStudentData document) {
        Objects.requireNonNull(document);

        long mod = getTableSuffix(document.getActivityId());
        return StringUtils.formatMessage(template, mod);
    }

    private long getTableSuffix(String activityId) {
        Objects.requireNonNull(activityId);
        long i = (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) ? 2 : 20;
        return Math.abs(activityId.hashCode()) % i;
    }

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql miscSql;

    @Override
    public void afterPropertiesSet() {
        miscSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    public void deleteActivityReportStudentDataById(String id) {
        long tableSuffix = getTableSuffix(id);
        miscSql.withSql("DELETE FROM VOX_ACTIVITY_REPORT_STUDENT_DATA_" + tableSuffix + " WHERE ACTIVITY_ID = '" + id + "'").executeUpdate();
    }
}
