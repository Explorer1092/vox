package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.ReadReciteSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.service.ReadReciteHomeworkService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.List;

/**
 * Created by tanguohong on 2017/6/2.
 */
public class ReadReciteHomeworkServiceClient implements ReadReciteHomeworkService {

    @ImportService(interfaceClass = ReadReciteHomeworkService.class)
    private ReadReciteHomeworkService remoteReference;

    @Override
    public List<ReadReciteSummaryResult> getReadReciteSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return remoteReference.getReadReciteSummaryInfo(homeworkId, objectiveConfigType, studentId);
    }

    @Override
    public List<ReadReciteSummaryResult> getVacationReadReciteSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return remoteReference.getVacationReadReciteSummaryInfo(homeworkId, objectiveConfigType, studentId);
    }
}
