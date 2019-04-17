package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
public class NewHomeworkTypeResultProcessLevelReadings extends NewHomeworkTypeResultProcessTemplate {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.LEVEL_READINGS;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        List<String> readingIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            if (MapUtils.isNotEmpty(nraa.getAnswers())) {
                processResultIds.addAll(nraa.getAnswers().values());
            }
            readingIds.add(nraa.getPictureBookId());
        }
        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        Map<String, PictureBookPlus> readingMap = pictureBookPlusServiceClient.loadByIds(readingIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            PictureBookPlus reading = readingMap.get(nraa.getPictureBookId());
            if (reading != null) {
                int errorCount = 0;
                int rightCount = 0;
                for (String processResultId : nraa.getAnswers().values()) {
                    NewHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                    if (nhpr == null) continue;
                    if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                        rightCount++;
                        totalRightCount++;
                    } else {
                        errorCount++;
                        totalErrorCount++;
                    }
                }
                results.add(MapUtils.m("readingName", reading.getEname(),
                        "rightCount", rightCount,
                        "errorCount", errorCount));
            }
        }
        return MapMessage.successMessage().add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }
}
