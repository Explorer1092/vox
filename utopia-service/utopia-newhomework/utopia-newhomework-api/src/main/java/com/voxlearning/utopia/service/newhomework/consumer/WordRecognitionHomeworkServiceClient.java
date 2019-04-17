package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.entity.WordRecognitionSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.service.WordRecognitionHomeworkService;

import java.util.List;

public class WordRecognitionHomeworkServiceClient implements WordRecognitionHomeworkService {
    @ImportService(interfaceClass = WordRecognitionHomeworkService.class)
    private WordRecognitionHomeworkService remoteReference;

    @Override
    public List<WordRecognitionSummaryResult> getWordRecognitionSummaryInfo(String homeworkId, Long studentId) {
        return remoteReference.getWordRecognitionSummaryInfo(homeworkId, studentId);
    }
}
