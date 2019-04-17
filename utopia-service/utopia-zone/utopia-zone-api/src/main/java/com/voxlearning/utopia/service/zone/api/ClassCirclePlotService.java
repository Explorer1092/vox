package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.entity.ZoneClazzRewardNotice;
import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181110")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClassCirclePlotService {
    Boolean upsetRewardConfig(Integer activityId, Integer type, String name, String pic);

    MapMessage findClazzProgressList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    MapMessage findThankList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    MapMessage findRewardList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    MapMessage gaveGift(Integer activityId, Long userId,Long gaveUserId,Integer type);

    MapMessage findEveryDayReward(Integer activityId, Long userId,Long schoolId, Long clazzId);

    //添加班级贡献值
    Boolean addClazzContribution(Integer activityId, Long userId,Integer value);

    Boolean addStudentContribution(Integer activityId, Long userId,Integer value);

    void insertOrder(ZoneClazzRewardNotice zoneClazzRewardNotice);

    MapMessage batchGaveGift(Integer activityId, Long userId, List<Long> gaveUserId, Integer type);

    public Boolean addZoneClazzRewardNotice(Long userId, ZoneClazzRewardNotice zoneClazzRewardNotice,String appName);

    List<AwardDetail> getLastDayRankReward(Integer activityId, Long userId, Date date);

    boolean sendEveryDayReward(Integer activityId, Long userId,Date date);

}
