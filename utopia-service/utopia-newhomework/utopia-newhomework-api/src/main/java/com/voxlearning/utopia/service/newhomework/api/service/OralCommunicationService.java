package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationQuestionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/22
 * \* Time: 7:55 PM
 * \* Description: 口语交际
 * \
 */
@ServiceVersion(version = "20190108")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface OralCommunicationService extends IPingable {

    /**
     * 获取作业中口语交际题
     * @param homeworkId
     * @param objectiveConfigType
     * @return
     */
    List<Map> getHomeworkSummaryGroupByType(String homeworkId,  String objectiveConfigType);

    /**
     * 获取作业基本数据
     */
    @Idempotent
    List<OralCommunicationSummaryResult> getHomeworkStoneInfo(String homeworkId, Long studentId, String objectiveConfigType);

    /**
     * 获取学生开始答题后的详细数据
     *
     * @param stoneId
     * @return
     */
    OralCommunicationQuestionResult getHomeworkStonDetaiInfo(String stoneId);

    /**
     * 获取口语交际答题结果
     * @param newHomework
     * @param newHomeworkResult
     * @param studentId
     * @param stoneId
     * @return
     */
    OralCommunicationQuestionResult getHomeworkStoneAnswerInfo(NewHomework newHomework, NewHomeworkResult newHomeworkResult, Long studentId, String stoneId);

}
