package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationQuestionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.OralCommunicationService;

import java.util.List;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/22
 * \* Time: 8:05 PM
 * \* Description:
 * \
 */
public class OralCommunicationClient implements OralCommunicationService {

    @ImportService(interfaceClass = OralCommunicationService.class)
    private OralCommunicationService oralCommunicationService;

    @Override
    public List<Map> getHomeworkSummaryGroupByType(String homeworkId, String objectiveConfigType) {
        return oralCommunicationService.getHomeworkSummaryGroupByType(homeworkId,objectiveConfigType);
    }

    @Override
    public List<OralCommunicationSummaryResult> getHomeworkStoneInfo(String homeworkId, Long studentId, String objectiveConfigType) {
        return oralCommunicationService.getHomeworkStoneInfo(homeworkId, studentId, objectiveConfigType);
    }

    @Override
    public OralCommunicationQuestionResult getHomeworkStonDetaiInfo(String stoneId) {
        return oralCommunicationService.getHomeworkStonDetaiInfo(stoneId);
    }

    @Override
    public OralCommunicationQuestionResult getHomeworkStoneAnswerInfo(NewHomework newHomework, NewHomeworkResult newHomeworkResult, Long studentId, String stoneId) {
        return oralCommunicationService.getHomeworkStoneAnswerInfo(newHomework, newHomeworkResult, studentId, stoneId);
    }

}
