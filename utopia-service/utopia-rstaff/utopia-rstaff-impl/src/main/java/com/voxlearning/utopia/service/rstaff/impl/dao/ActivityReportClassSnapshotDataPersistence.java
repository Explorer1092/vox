package com.voxlearning.utopia.service.rstaff.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsDynamicJdbDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportClassSnapshotData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Named
@CacheBean(type = ActivityReportClassSnapshotData.class)
public class ActivityReportClassSnapshotDataPersistence extends AlpsDynamicJdbDao<ActivityReportClassSnapshotData, String> {

    @Override
    protected void calculateCacheDimensions(ActivityReportClassSnapshotData document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @Override
    protected String calculateTableName(String template, ActivityReportClassSnapshotData document) {
        Objects.requireNonNull(document);

        long mod = getTableSuffix(document.getActivityId());
        return StringUtils.formatMessage(template, mod);
    }

    private static long getTableSuffix(String activityId) {
        Objects.requireNonNull(activityId);
        long i = (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) ? 2 : 20;
        return Math.abs(activityId.hashCode()) % i;
    }

    public static void main(String[] args) {
        System.out.println(getTableSuffix("5bbee401a29361d07aaf8e3b"));
    }

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql miscSql;

    @Override
    public void afterPropertiesSet() {
        miscSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    @CacheMethod
    public List<ActivityReportClassSnapshotData> getByActivityIdClazzId(@CacheParameter("AID") String activityId, @CacheParameter("CID") Long clazzId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId).and("CLAZZ_ID").is(clazzId);
        ActivityReportClassSnapshotData mock = new ActivityReportClassSnapshotData();
        mock.setActivityId(activityId);
        return executeQuery(Query.query(criteria), getDocumentTableName(mock));
    }

    public void deleteByActivityIdCurDate(String activityId, String curDate) {
        long tableSuffix = getTableSuffix(activityId);
        miscSql.withSql("DELETE FROM VOX_ACTIVITY_REPORT_CLASS_SNAPSHOT_DATA_" + tableSuffix + " WHERE ACTIVITY_ID = '" + activityId + "' AND CUR_DATE = '" + curDate + "'").executeUpdate();
    }
}
