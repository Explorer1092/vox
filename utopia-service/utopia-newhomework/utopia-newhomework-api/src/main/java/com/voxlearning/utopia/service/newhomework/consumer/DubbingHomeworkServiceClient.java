package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.service.DubbingHomeworkService;

import java.util.List;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
public class DubbingHomeworkServiceClient implements DubbingHomeworkService {
    @ImportService(interfaceClass = DubbingHomeworkService.class)
    private DubbingHomeworkService remoteReference;

    @Override
    public List<DubbingSummaryResult> getDubbingSummerInfo(String homeworkId, Long studentId, String objectiveConfigType) {
        return remoteReference.getDubbingSummerInfo(homeworkId, studentId, objectiveConfigType);
    }

    @Override
    public List<DubbingSummaryResult> getVacationDubbingSummerInfo(String homeworkId, Long studentId, String objectiveConfigType) {
        return remoteReference.getVacationDubbingSummerInfo(homeworkId, studentId, objectiveConfigType);
    }

    public List<DubbingSummaryResult> getLiveCastDubbingSummerInfo(String homeworkId, Long studentId){
        return remoteReference.getLiveCastDubbingSummerInfo(homeworkId, studentId);
    }
}
