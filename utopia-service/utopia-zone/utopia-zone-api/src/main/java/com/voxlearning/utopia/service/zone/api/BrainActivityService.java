package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.vo.ActivityRank;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 最强大脑活动
 *
 * @author chensn
 * @date 2018-11-10 16:53
 */
@ServiceVersion(version = "20181128")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface BrainActivityService {

    public List<ActivityRank> getRank(Integer activityId, Long userId);

    public List<ActivityRank> getDailyRank(Integer activityId, Long userId, Date date);

    public ActivityRank getSelfDailyRank(Integer activityId, Long userId, Date date);

    public List<ActivityRank> getClazzRank(Integer activityId, Long userId, Integer timeType, Date date);

    public ActivityRank getSelfClazzRank(Integer activityId, Long userId, Integer timeType, Date date);

    public List<ActivityRank> getPersonInLevelRank(Integer activityId, Long userId, Integer timeType, Date date);

    public ActivityRank getSelfPersonInLevelRank(Integer activityId, Long userId, Integer timeType, Date date);

    public List<ActivityRank> getClazzInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date);

    public ActivityRank getSelfClazzInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date);

    public List<ActivityRank> getPersonInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date);

    public ActivityRank getSelfPersonInSchoolRank(Integer activityId, Long userId, Integer timeType, Date date);

    public void updateAllRank(Integer activityId, Long schoolId, Integer level, Long clazzId, Long userId, Integer num);

    MapMessage getReward(Long userId);

    MapMessage sendReward(Integer activityId, Long userId, Integer type);

    MapMessage doRankLike(Integer activityId, Long userId, Integer rankType, String toObjectId);

}
