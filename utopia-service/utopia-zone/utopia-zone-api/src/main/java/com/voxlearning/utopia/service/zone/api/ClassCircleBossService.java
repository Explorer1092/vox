package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import com.voxlearning.utopia.service.zone.api.entity.boss.RewordResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181105")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClassCircleBossService {

    List<ClazzBossAward> getClazzBossAwardList(Integer activityId);

    boolean deleteClazzBossAwardById(String id);

    ClazzBossAward detail(Integer activityId, Integer selfOrClazz, Integer type);

    ClazzBossAward updateOrInsert(Integer activityId, Integer selfOrClazz, Integer type, Double targetValue, String boxPic, String name, String awardDetails);

    Long increaseCountBySchoolIdAndType(Long schoolId, Integer type);

    Long loadCountBySchoolIdAndType(Long schoolId, Integer type);

    Long increaseCountByClazzIdAndType(Long clazzId);

    Long loadCountByClazzIdAndType(Long clazzId);

    Boolean setClazzBossCountByKey(String key, Long value);

    Boolean deleteClazzBossCountByKey(String key);

    Map<String, Object> userClazzActivityRecord(Integer activityId, Long userId, Long schoolId, Long clazzId);

    boolean setClazzBossSubject(String key, String value);

    String getClazzBossSubject(String key);

    List<RewordResponse> findRewardList(Integer activity, Long studentId, Long clazzId, Long schoolId);

    boolean setSubjectTime(String chinese, String english, String math);

    Boolean setSchoolBossCountByKey(String key, Long value);

    Long increaseCountByStudentId(Long userId);

    Long loadCountByStudentId(Long userId);
}
