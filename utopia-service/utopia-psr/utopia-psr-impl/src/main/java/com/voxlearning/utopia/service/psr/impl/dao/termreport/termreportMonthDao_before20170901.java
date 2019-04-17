package com.voxlearning.utopia.service.psr.impl.dao.termreport;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.termreport.termReportMonth;
import com.voxlearning.utopia.service.psr.entity.termreport.termReportMonth_before20170901;

import javax.inject.Named;
import java.util.*;

/**
 * Created by dongxue.zhao on 2017/7/18.
 */
@Named
@UtopiaCacheSupport(termreportMonthDao_before20170901.class)
public class termreportMonthDao_before20170901 extends AlpsStaticMongoDao <termReportMonth_before20170901, String> {
    @Override
    protected void calculateCacheDimensions(termReportMonth_before20170901 source, Collection<String> dimensions){
    }

    public List<termReportMonth> getStatisticsResultByMonth(Integer groupId, String subject, Set<String>months) {
        if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(subject) || groupId == null || groupId <= 0)
            return Collections.emptyList();

        Criteria criteria = Criteria.where("group_id").is(groupId).and("subject").is(subject).and("month").in(months);
        return CopyToTermReportMonth(query(Query.query(criteria)));
    }
    private List<termReportMonth> CopyToTermReportMonth(List<termReportMonth_before20170901> sourceList) {
        List<termReportMonth> newList = new ArrayList<termReportMonth>();
        for (termReportMonth_before20170901 oldMonthData: sourceList) {
            termReportMonth newMonthData = new termReportMonth();
            newMonthData.setId(oldMonthData.getId());
            newMonthData.setGroupId(oldMonthData.getGroupId());
            newMonthData.setUpdatedAt(oldMonthData.getUpdatedAt());
            newMonthData.setCreatedAt(oldMonthData.getCreatedAt());
            newMonthData.setMonth(oldMonthData.getMonth());
            newMonthData.setSubject(oldMonthData.getSubject());
            newMonthData.setLayout_times(oldMonthData.getLayout_times());
            newMonthData.setStudent_infos(oldMonthData.getStudent_infos());
            newList.add(newMonthData);
        }
        return newList;
    }
}
