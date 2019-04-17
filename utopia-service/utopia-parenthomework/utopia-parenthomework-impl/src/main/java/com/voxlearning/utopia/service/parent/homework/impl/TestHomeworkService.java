package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.integral.api.mapper.IntegralInfo;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 作业服务接口：提供布置作业、做作业功能
 * 仅供测试使用，TODO DELETE
 *
 * @author Wenlong Meng
 * @since  Nov 11, 2018
 */
@ServiceVersion(version = "20190303")
public interface TestHomeworkService {

    /**
     * 保存活动
     *
     * @param activity
     * @return
     */
    MapMessage saveActivity(Activity activity);

    /**
     * 保存用户活动
     *
     * @param userActivity
     * @return
     */
    MapMessage saveUserActivity(UserActivity userActivity);

    /**
     * 删除用户活动
     *
     * @return
     */
    MapMessage deleteUserActivity(String id);

    /**
     * 布置作业
     *
     * @param studentId
     * @param bookId
     * @param unitId
     * @param regionCode
     * @return
     */
    MapMessage assignExam(Long studentId, String bookId, String unitId, String regionCode);

    /**
     * 布置作业
     *
     * @param studentId
     * @param bookId
     * @param unitId
     * @param regionCode
     * @return
     */
    MapMessage assignMentalArithmetic(Long studentId, String bookId, String unitId, String regionCode);

    /**
     * latext to pdf
     *
     * @return
     */
    MapMessage latex();

    /**
     * test
     *
     * @return
     */
    MapMessage integral(IntegralInfo integralInfo, String command);


    /**
     * mq sender
     *
     * @return
     */
    MapMessage mq(String topic, Map args);

    /**
     * 查询完成任务用户
     *
     * @param activityId 活动id
     * @param startTime
     * @param endTime
     * @return
     */
    List<Long> loadDoneUsers(String activityId, String startTime, String endTime);
}
