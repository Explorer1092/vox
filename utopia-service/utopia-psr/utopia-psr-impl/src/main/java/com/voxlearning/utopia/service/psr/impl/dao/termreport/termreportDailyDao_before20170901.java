package com.voxlearning.utopia.service.psr.impl.dao.termreport;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.psr.entity.termreport.termReportDaily;
import com.voxlearning.utopia.service.psr.entity.termreport.termReportDaily_before20170901;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
/**
 * Created by dongxue.zhao on 2017/7/21.
 */
@Named
@UtopiaCacheSupport(termreportDailyDao_before20170901.class)
public class termreportDailyDao_before20170901 extends AlpsStaticMongoDao<termReportDaily_before20170901, String> {
    @Override
    protected void calculateCacheDimensions(termReportDaily_before20170901 source, Collection<String> dimensions){
    }

    public List<termReportDaily> getStatisticsResultByGroupAndUnitid(String unit_id, Integer groupId) {
        if (StringUtils.isEmpty(unit_id) || groupId == null || groupId <= 0)
            return Collections.emptyList();

        Criteria criteria = Criteria.where("unit_id").is(unit_id).and("group_id").is(groupId);
        return CopyToTermReportDaily(query(Query.query(criteria)));
    }
    private List<termReportDaily> CopyToTermReportDaily(List<termReportDaily_before20170901> sourceList) {
        List<termReportDaily> newList = new ArrayList<termReportDaily>();
        for (termReportDaily_before20170901 oldDailyData: sourceList) {
            termReportDaily newDailyData = new termReportDaily();
            newDailyData.setId(oldDailyData.getId());
            newDailyData.setGroupId(oldDailyData.getGroupId());
            newDailyData.setUpdatedAt(oldDailyData.getUpdatedAt());
            newDailyData.setCreatedAt(oldDailyData.getCreatedAt());
            newDailyData.setUnit_id(oldDailyData.getUnit_id());
            newDailyData.setLayout_times(oldDailyData.getLayout_times());
            newDailyData.setStudent_infos(oldDailyData.getStudent_infos());
            newList.add(newDailyData);
        }
        return newList;
    }
}
