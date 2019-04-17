package com.voxlearning.utopia.service.rstaff.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportBaseData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class ActivityReportBaseDataPersistence extends AlpsStaticJdbcDao<ActivityReportBaseData, String> {
    @Override
    protected void calculateCacheDimensions(ActivityReportBaseData document, Collection<String> dimensions) {
    }

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql miscSql;

    @Override
    public void afterPropertiesSet() {
        miscSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
//        miscSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    public void deleteAll() {
            String deleteSql = "DELETE FROM VOX_ACTIVITY_REPORT_BASE_DATA WHERE 1=1";
            miscSql.withSql(deleteSql).executeUpdate();
    }

    public List<ActivityReportBaseData> loadAllActivityReportBaseDatas() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityType(String activityCode) {
        Criteria criteria = Criteria.where("ACTIVITY_TYPE").is(activityCode).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<ActivityReportBaseData> loadActivityReportBaseDatasByActivityId(String activityId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }
}
