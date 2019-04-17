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
 * @since 2017/4/19
 */
@ServiceVersion(version = "20170419")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface SmallPaymentGenerateDataService extends IPingable {

    Map<String, Object> generateIndexData(String homeworkId, Long studentId);

    MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType);

    Map<String, Object> loadHomeworkQuestionsAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Long studentId);

}
