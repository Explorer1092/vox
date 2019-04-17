package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.calendar.DateUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmVisitPlan;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CrmVisitPlanDao
 *
 * @author song.wang
 * @date 2016/7/4
 */
@Named("agent.CrmVisitPlanDao")
@CacheBean(type = CrmVisitPlan.class)
public class CrmVisitPlanDao extends StaticMongoDao<CrmVisitPlan, String> {

    @Override
    protected void calculateCacheDimensions(CrmVisitPlan source, Collection<String> dimensions) {
        dimensions.add(CrmVisitPlan.ck_user(source.getUserId()));
    }

    // 获取用户指定时间段内的拜访计划
    @UtopiaCacheable
    public List<CrmVisitPlan> loadUserVisitPlan(@UtopiaCacheKey(name = "UID") Long userId) {
        // 员工的拜访计划默认只取半年以内的
        Date dateFrom = DateUtils.calculateDateDay(new Date(), -180);
        Filter filter = filterBuilder.where("userId").is(userId)
                .and("visitTime").gte(dateFrom)
                .and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.ASC, "visitTime");
        return __find_OTF(Find.find(filter).with(sort));
    }

    @CacheMethod(type = List.class)
    public Map<Long, List<CrmVisitPlan>> loadUserVisitPlan(@CacheParameter(value = "UID", multiple = true) Collection<Long> userIds) {
        // 员工的拜访计划默认只取半年以内的
        Date dateFrom = DateUtils.calculateDateDay(new Date(), -180);
        Filter filter = filterBuilder.where("userId").in(userIds)
                .and("visitTime").gte(dateFrom)
                .and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.ASC, "visitTime");
        return __find_OTF(Find.find(filter).with(sort)).stream().collect(Collectors.groupingBy(CrmVisitPlan::getUserId));
    }

}
