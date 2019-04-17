package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroup;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class ActivityGroupService {

    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityGroupDao activityGroupDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private ActivityGroupUserService groupUserService;
    @Inject
    private ActivityGroupStatisticsService groupStatisticsService;
    @Inject
    private ActivityGroupUserStatisticsService groupUserStatisticsService;

    public void addNewGroupData(String activityId, String groupId, Date groupTime, Long groupUserId, Long userId){

        if(StringUtils.isBlank(activityId) || StringUtils.isBlank(groupId) || groupUserId == null || groupUserId < 1 || userId == null || userId < 1){
            return;
        }

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return;
        }

        ActivityGroup dbGroup = activityGroupDao.loadByGid(groupId);
        if(dbGroup != null){
            return;
        }

        ActivityGroup group = new ActivityGroup();
        group.setActivityId(activityId);
        group.setGroupId(groupId);
        group.setGroupTime(groupTime == null? new Date() : groupTime);
        group.setIsComplete(false);
        group.setUserId(userId);
        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser != null){
            group.setUserName(agentUser.getRealName());
        }
        activityGroupDao.insert(group);

        // 保存组团对应的人员数据
        AlpsThreadPool.getInstance().submit(() -> groupUserService.userJoinGroup(groupId, groupUserId, groupTime == null? new Date() : groupTime, true));

        // 组团数量统计
        AlpsThreadPool.getInstance().submit(() -> groupStatisticsService.newGroupStatistics(group));
    }

    public void completeGroup(String groupId, Date completeTime){
        ActivityGroup group = activityGroupDao.loadByGid(groupId);
        if(group == null || SafeConverter.toBoolean(group.getIsComplete())){
            return;
        }
        group.setIsComplete(true);
        group.setCompleteTime(completeTime == null ? new Date() : completeTime);
        activityGroupDao.upsert(group);

        // 组团数量统计
        AlpsThreadPool.getInstance().submit(() -> groupStatisticsService.completeGroupStatistics(group));

        AlpsThreadPool.getInstance().submit(() -> groupUserStatisticsService.completeUserCountStatistics(group));

    }

    public List<ActivityGroup> loadByAidAndUidsAndTime(String activityId, Collection<Long> userIds, Date startDate, Date endDate){

        List<ActivityGroup> groupList = activityGroupDao.loadByAidAndUids(activityId, userIds);
        if(CollectionUtils.isEmpty(groupList)){
            return new ArrayList<>();
        }
        return groupList.stream().filter(p -> {
            if(startDate != null){
                if(endDate != null){
                    return p.getGroupTime().after(startDate) && p.getGroupTime().before(endDate);
                }else {
                    return p.getGroupTime().after(startDate);
                }
            }else {
                if(endDate != null){
                    return p.getGroupTime().before(endDate);
                }else {
                    return true;
                }
            }
        }).collect(Collectors.toList());
    }
}
