package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.VideoSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.service.VideoHomeworkService;

import java.util.List;

/**
 * @author tanguohong
 * @since 2016/11/25
 */
public class VideoHomeworkServiceClient implements VideoHomeworkService {

    @ImportService(interfaceClass = VideoHomeworkService.class)
    private VideoHomeworkService remoteReference;

    @Override
    public List<VideoSummaryResult> getVideoSummaryInfo(String homeworkId, Long studentId) {
        return remoteReference.getVideoSummaryInfo(homeworkId, studentId);
    }

    @Override
    public List<VideoSummaryResult> getVacationVideoSummaryInfo(String homeworkId, Long studentId) {
        return remoteReference.getVacationVideoSummaryInfo(homeworkId, studentId);
    }
}
