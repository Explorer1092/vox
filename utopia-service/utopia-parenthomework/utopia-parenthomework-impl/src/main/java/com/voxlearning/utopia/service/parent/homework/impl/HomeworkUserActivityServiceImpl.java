package com.voxlearning.utopia.service.parent.homework.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkActivityService;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserActivityService;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserActivityDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 作业用户活动服务实现
 *
 * @author Wenlong Meng
 * @since Feb 24, 2019
 */
@Named
@ExposeService(interfaceClass = HomeworkUserActivityService.class)
public class HomeworkUserActivityServiceImpl implements HomeworkUserActivityService {

    //local variable
    @Inject private HomeworkActivityService activityService;
    @Inject private HomeworkUserActivityDao userActivityDao;
    UtopiaCache utopiaCache = CacheSystem.CBS.getCache("flushable");
    private RateLimiter rateLimiter = RateLimiter.create(66);

    //Logic

    /**
     * 报名参加活动
     *
     * @param userActivity 活动
     * @return
     */
    @Override
    public MapMessage join(UserActivity userActivity) {
        LoggerUtils.debug("UserActivityService.join", userActivity);
        Activity activity = activityService.load(userActivity.getActivityId());
        if(activity == null){
            return MapMessage.errorMessage("活动不存在").set("status", -9);
        }
        if(activity.getStatus() == -1 ||activity.getStartTime().after(new Date())){
            return MapMessage.errorMessage("活动未开始").set("status", -1);
        }
        String id = HomeworkUtil.generatorID(userActivity.getStudentId(), userActivity.getActivityId());
        UserActivity ouserActivity = this.load(id);
        if(ouserActivity != null){
            return MapMessage.successMessage("已报名");
        }
        userActivity.setId(id);
        userActivity.setFinished(Boolean.FALSE);
        userActivity.setStatus(1);
        if(ouserActivity == null){
            userActivity.setCreateTime(new Date());
        }
        userActivity.setUpdateTime(new Date());
        userActivityDao.upsert(userActivity);
        return MapMessage.successMessage();
    }

    /**
     * 查询活动信息
     *
     * @param studentId  学生id
     * @param activityId 活动id
     * @return
     */
    @Override
    public MapMessage load(Long studentId, String activityId) {
        Activity activity = activityService.load(activityId);
        if(activity == null){
            return MapMessage.errorMessage("活动不存在").set("status", -9);
        }
        int activityStatus = 0;
        if(activity.getStatus() == -1 || activity.getStartTime().after(new Date())){
            activityStatus = -1;
        }else if(activity.getStatus() == 1 || activity.getEndTime().before(new Date())){
            activityStatus = 1;
        }
        int activityPeriod = activity.periodCount();
        UserActivity userActivity = this.load(HomeworkUtil.generatorID(studentId, activityId));
        int userStatus = -1;
        int userPeriod = 0;
        if(userActivity != null) {
            userStatus = 1;
            if(userActivity.getExtInfo()!=null){
                userPeriod = userActivity.getExtInfo().size();
            }
        }
        String key = HomeworkUtil.generatorDayID("parentHomework_activity_reward", studentId);
        List<Integer> rewards = utopiaCache.load(key);
        if(!ObjectUtils.anyBlank(rewards)){
            utopiaCache.delete(key);
        }

        int userCount = this.count(activity.getId());
        MapMessage mapMessage = MapMessage.successMessage().set("data", MapUtils.m("activityPeriod", activityPeriod,
                "currentPeriod", activity.currentPeriod().getIndex(),
                "userPeriod", userPeriod,
                "activityStatus", activityStatus,
                "userStatus", userStatus,
                "userCount", userCount,
                "rewards", rewards));
        LoggerUtils.debug("UserActivityService.load", studentId, activityId, mapMessage);
        return mapMessage;
    }

    /**
     * 查询用户活动信息
     *
     * @param id
     * @return
     */
    @Override
    public UserActivity load(String id) {
        return userActivityDao.load(id);
    }

    /**
     * 统计参加活动人数
     *
     * @param activityId
     * @return
     */
    @Override
    public int count(String activityId) {
        return (int)this.userActivityDao.count(activityId);
    }

    /**
     * 查询未做活动的用户信息
     *
     * @param activityId
     * @return
     */
    @Override
    public List<Long> loadDNFUserIds(String activityId, Date startTime, Date endTime) {
        Set<Long> udfParentIds = Sets.newHashSet();
        int offset = 0;
        int limit = 1000;
        do{
            List<UserActivity> userActivitys = userActivityDao.loadDNFUsers(activityId, offset, limit, startTime, endTime);
            Set<Long> parentIds = userActivitys.stream().filter(ua->ObjectUtils.get(()->ua.getExtInfo()==null, false)).map(ua->ua.getParentId()).collect(Collectors.toSet());
            udfParentIds.addAll(parentIds);
            if(userActivitys.size() < limit){
                break;
            }
            rateLimiter.acquire();
            offset += userActivitys.size();
        }while (udfParentIds.size() < 100000);
        return Lists.newArrayList(udfParentIds);
    }

    /**
     * 查询完成任务用户
     *
     * @param activityId 活动id
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<UserActivity> loadDoneUsers(String activityId, Date startTime, Date endTime) {
        return this.userActivityDao.loadDoneUsers(activityId, startTime, endTime);
    }
}