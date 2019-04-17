package com.voxlearning.utopia.service.psr.impl.dao.termreport;


import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.termreport.termReportMonth;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Created by mingming.zhao on 2016/10/10.
 */
@Named
@UtopiaCacheSupport(termReportMonth.class)
public class termreportMonthDao extends AlpsStaticMongoDao<termReportMonth, String> {
    @Override
    protected void calculateCacheDimensions(termReportMonth source, Collection<String> dimensions){
    }

    public List<termReportMonth> getStatisticsResultByMonth(Integer groupId, String subject, Set<String>months) {
        if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(subject) || groupId == null || groupId <= 0)
            return Collections.emptyList();

        Criteria criteria = Criteria.where("group_id").is(groupId).and("subject").is(subject).and("month").in(months);
        return query(Query.query(criteria));
    }

}


