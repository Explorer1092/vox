package com.voxlearning.utopia.service.psr.impl.dao.termreport;


import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.termreport.termReportDaily;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Created by Administrator on 2016/10/10.
 */
@Named
@UtopiaCacheSupport(termReportDaily.class)
public class termreportDailyDao extends AlpsStaticMongoDao<termReportDaily, String> {
    @Override
    protected void calculateCacheDimensions(termReportDaily source, Collection<String> dimensions){
    }

    public List<termReportDaily> getStatisticsResultByGroupAndUnitid(String unit_id, Integer groupId) {
        if (StringUtils.isEmpty(unit_id) || groupId == null || groupId <= 0)
            return Collections.emptyList();

        Criteria criteria = Criteria.where("unit_id").is(unit_id).and("group_id").is(groupId);
        return query(Query.query(criteria));
    }

}


