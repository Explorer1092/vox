package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/12/28
 */
@ServiceVersion(version = "20180319")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface LiveCastGenerateDataService extends IPingable {

    /**
     * 生成首页数据
     *
     * @param homeworkId 作业id
     * @param studentId  学生id
     * @return Map
     */
    Map<String, Object> generateIndexData(String homeworkId, Long studentId, String token);


    MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    @Deprecated
    Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType);

    @Deprecated
    Map<String, Object> loadHomeworkQuestionsAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Long studentId);

    Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Integer categoryId, String lessonId, String videoId);

    Map<String, Object> loadHomeworkQuestionsAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Long studentId, Integer categoryId, String lessonId, String videoId);

}
