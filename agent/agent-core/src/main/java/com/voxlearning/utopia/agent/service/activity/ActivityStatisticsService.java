package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderStatisticsDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderStatistics;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class ActivityStatisticsService {

    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityOrderDao activityOrderDao;
    @Inject
    private ActivityOrderStatisticsDao activityOrderStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void calOrderStatisticsData(){
        calOrderStatisticsData(null, null);
    }

    public void calOrderStatisticsData(Date startDate, Date endDate){
        List<AgentActivity> activityList = agentActivityDao.loadAll();
        activityList.forEach(p -> calOrderStatisticsData(p.getId(), startDate, endDate));
    }

    public void calOrderStatisticsData(String activityId, Date startDate, Date endDate){
        List<AgentUser> userList = baseOrgService.findAllAgentUsers();
        userList.forEach(p -> calOrderStatisticsData(activityId, p.getId(), startDate, endDate));
    }

    private void calOrderStatisticsData(String activityId, Long userId, Date startDate, Date endDate){
        List<ActivityOrder> orderList = activityOrderDao.loadByAidAndUid(activityId, userId);
        if(CollectionUtils.isEmpty(orderList)){
            return;
        }

        orderList = orderList.stream().filter(p -> {
            if(startDate != null){
                if(endDate != null){
                    return p.getOrderPayTime().after(startDate) && p.getOrderPayTime().before(endDate);
                }else {
                    return p.getOrderPayTime().after(startDate);
                }
            }else {
                if(endDate != null){
                    return p.getOrderPayTime().before(endDate);
                }
                return true;
            }
        }).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(orderList)){
            return;
        }

        Map<Integer, List<ActivityOrder>> dayOrderMap = orderList.stream().collect(Collectors.groupingBy(p -> SafeConverter.toInt(DateUtils.dateToString(p.getOrderPayTime(), "yyyyMMdd"))));

        dayOrderMap.forEach((k, v) -> {
            ActivityOrderStatistics orderStatistics = activityOrderStatisticsDao.loadByUidAndDay(activityId, userId, k);
            if(orderStatistics == null){
                orderStatistics = new ActivityOrderStatistics();
                orderStatistics.setActivityId(activityId);
                orderStatistics.setUserId(userId);
                orderStatistics.setUserName(v.get(0).getUserName());
                orderStatistics.setDay(k);
            }
            orderStatistics.setCount(v.size());
            activityOrderStatisticsDao.upsert(orderStatistics);
        });
    }




}
