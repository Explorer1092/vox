package com.voxlearning.utopia.service.rstaff.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
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

    @CacheMethod
    public Map<Long, Long> loadParticipateCountByClazzIds(@CacheParameter("AID") String activityId) {
        String deleteSql = "SELECT A.CLAZZ_ID as clazzId,SUM(A.PARTICIPANT_STUDS) as clazzCount FROM VOX_ACTIVITY_REPORT_COLLECT_DATA AS A " +
                "WHERE A.ACTIVITY_ID  = '" + activityId + "' GROUP BY A.CLAZZ_ID ";

        List<Map<String, Object>> maps = miscSql.withSql(deleteSql).queryAll();

        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            Long clazzId = MapUtils.getLong(map, "clazzId");
            Long clazzCount = MapUtils.getLong(map, "clazzCount");
            result.put(clazzId, clazzCount);
        }

        return result;
    }

    public void deleteAll() {
        String deleteSql = "DELETE FROM VOX_ACTIVITY_REPORT_COLLECT_DATA WHERE 1=1";
        miscSql.withSql(deleteSql).executeUpdate();
    }

    public List<ActivityReportCollectData> loadActivityReportCollectDatasByRegionCode(String regionLevel, String regionCode, String id) {
        Criteria criteria = null;
        if("city".equals(regionLevel)){
            criteria = Criteria.where("city_code").is(regionCode).and("activity_id").is(id).and("DISABLED").is(false);
        }else if("county".equals(regionLevel)){
            criteria = Criteria.where("region_code").is(regionCode).and("activity_id").is(id).and("DISABLED").is(false);
        }else{
            criteria = Criteria.where("school_id").is(regionCode).and("activity_id").is(id).and("DISABLED").is(false);
        }
        return query(Query.query(criteria));
    }
}
