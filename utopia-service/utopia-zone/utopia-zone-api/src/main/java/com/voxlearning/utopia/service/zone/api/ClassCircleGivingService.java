package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCircleRewardNotice;
import com.voxlearning.utopia.service.zone.api.entity.giving.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181114")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClassCircleGivingService {

    Map<Integer, List<ChickenHelpResponse>> findClazzStudentList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    Boolean inviteHelp(Integer activityId, Long userId, Long schoolId, Long clazzId,Integer type);

    List<ChickenRewardResponse> findChickenStudentList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    Boolean helpClassmate(Integer activityId, Long userId, Long schoolId, Long clazzId,String chickenHelpId);

    MapMessage helpOtherUser(Integer activityId, Long schoolId, Long clazzId, Long userId, String ahId);

    ChickenClazzResponse findClassChickenList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    List<ChickenStudentRecordResponse> findEatChickenList(Integer activityId, Long userId, Long schoolId, Long clazzId);

    MapMessage getSelfActivityProgress(Integer activityId, Long schoolId, Long clazzId, Long userId);

    MapMessage composeActivityGoods(Integer activityId, Long schoolId, Long clazzId, Long userId);

    List<ChickenClazzProgressResponse> findClazzProgressList(Integer activityId, Integer len, Integer pageSize);

    void addClassCount(String key);

    MapMessage receiveSelfRank(String activityAwardId, Long userId,Long schooleId,Long clazzId);

    MapMessage receiveClassRank(String activityAwardId, Long userId,Long schooleId,Long clazzId);

     Long increaseCountByStudentId(Long userId) ;

    public String getStudentSubject(Long userId);

  List<ClazzCircleRewardNotice>  queryClazzCircleRewardNotice(Integer activityId, Long userId,Long schoolId,Long classId);

    void upsetClazzCircleRewardNotice(Integer activityId,Long userId,Boolean isShow,Integer rewardType);


}
