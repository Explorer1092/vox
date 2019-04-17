package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.service.WordTeachHomeworkService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.List;
import java.util.Map;

/**
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/12/3 下午2:06
 */
public class WordTeachHomeworkServiceClient implements WordTeachHomeworkService {

    @ImportService(interfaceClass = WordTeachHomeworkService.class)
    private WordTeachHomeworkService remoteReference;

    @Override
    public List<Map> getWordTeachSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        return remoteReference.getWordTeachSummaryInfo(homeworkId, objectiveConfigType, studentId);
    }

    @Override
    public List<Map> getModuleSummaryInfo(String homeworkId, Long studentId, String stoneDataId, WordTeachModuleType wordTeachModuleType) {
        return remoteReference.getModuleSummaryInfo(homeworkId, studentId, stoneDataId, wordTeachModuleType);
    }
}
