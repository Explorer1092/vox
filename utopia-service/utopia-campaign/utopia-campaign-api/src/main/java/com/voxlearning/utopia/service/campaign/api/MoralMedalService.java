package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceVersion(version = "20190223")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface MoralMedalService {

    /**
     * 查询老师班级
     */
    MapMessage loadTeacherGroup(Long teacherId);

    /**
     * 查询老师班级详情
     */
    MapMessage loadTeacherGroupDetail(Long teacherId, Long groupId);

    /**
     * 查询某一次详情发放详情
     */
    MapMessage loadMedalDetail(Long moralMedalId);

    /**
     * 给某个勋章动态点赞
     */
    MapMessage parentLike(Long parentId, Long moralMedalId);

    /**
     * 批量给学生发勋章
     */
    MapMessage sendMedal(Long teacherId, Long groupId, List<Long> studentIds, List<Integer> medalIds);

    /**
     * 班级德育表现
     */
    MapMessage loadClazzMoral(Long id, Long groupId, String date);

    MapMessage hotMedal(Long sid, String date);

    MapMessage historyMedal(Long sid, Integer page, Integer pagesize);

    @Deprecated
    MapMessage deleteAll();
}
