package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.service.DPLiveCastHomeworkService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.Map;

public class DPLiveCastHomeworkServiceClient implements DPLiveCastHomeworkService {
    @ImportService(interfaceClass = DPLiveCastHomeworkService.class)
    private DPLiveCastHomeworkService remoteReference;

    @Override
    public MapMessage deleteHomework(Long teacherId, String hid) {
        return remoteReference.deleteHomework(teacherId, hid);
    }

    @Override
    public MapMessage noteComment(Long teacherId, String comment, Collection<Long> useIds, String hid) {
        return remoteReference.noteComment(teacherId, comment, useIds, hid);
    }

    @Override
    public MapMessage correctQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap) {
        return remoteReference.correctQuestions(homeworkId, studentId, correctInfoMap);
    }

    @Override
    public MapMessage newCorrectQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap, ObjectiveConfigType type) {
        return remoteReference.newCorrectQuestions(homeworkId, studentId, correctInfoMap, type);
    }
}