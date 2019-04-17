package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.service.DPHomeworkService;

public class DPHomeworkServiceClient implements DPHomeworkService {
    @ImportService(interfaceClass = DPHomeworkService.class)
    private DPHomeworkService remoteReference;

    @Override
    public NewHomework loadNewHomework(String homeworkId) {
        return remoteReference.loadNewHomework(homeworkId);
    }
}
