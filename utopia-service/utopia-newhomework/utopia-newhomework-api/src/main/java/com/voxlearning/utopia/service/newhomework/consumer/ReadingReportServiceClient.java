package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.service.ReadingReportService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;

public class ReadingReportServiceClient implements ReadingReportService {
    @ImportService(interfaceClass = ReadingReportService.class)
    private ReadingReportService remoteReference;

    @Override
    public MapMessage submitReadingDubbingData(Teacher teacher ,String hid, ObjectiveConfigType type, String pictureId, List<ReadingDubbingRecommend.ReadingDubbing> readingDubbings, String recommendComment) {
        return remoteReference.submitReadingDubbingData(teacher,hid, type, pictureId, readingDubbings, recommendComment);
    }
}
