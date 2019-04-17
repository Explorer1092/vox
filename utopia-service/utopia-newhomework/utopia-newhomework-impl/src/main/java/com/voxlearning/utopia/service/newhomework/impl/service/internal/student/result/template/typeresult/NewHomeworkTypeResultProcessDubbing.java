package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Named
public class NewHomeworkTypeResultProcessDubbing extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.DUBBING;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomework newHomework = newHomeworkLoader.load(baseHomeworkResult.getHomeworkId());
        if(newHomework == null){
            return MapMessage.errorMessage("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = resultAnswer.getAppAnswers();
        List<String> dubbingIds = appAnswers.values()
                .stream()
                .map(NewHomeworkResultAppAnswer::getDubbingId)
                .collect(Collectors.toList());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);

        // 生成合成配音ids
        String homeworkId = baseHomeworkResult.getHomeworkId();
        Long studentId = baseHomeworkResult.getUserId();
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            for (String dubbingId : dubbingIds) {
                ids.add(new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString());
            }
        }
        Map<String, DubbingSyntheticHistory> dubbingSyntheticHistoryMap = newHomeworkLoader.loadDubbingSyntheticHistories(ids);

        List<Map<String, Object>> results = new ArrayList<>();
        for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
            String dubbingId = appAnswer.getDubbingId();
            Dubbing dubbing = dubbingMap.get(dubbingId);
            String id = new DubbingSyntheticHistory.ID(homeworkId, studentId, dubbingId).toString();
            DubbingSyntheticHistory dubbingSyntheticHistory = dubbingSyntheticHistoryMap.get(id);
            // 合成配音是否成功
            Boolean synthetic = dubbingSyntheticHistory == null || SafeConverter.toBoolean(dubbingSyntheticHistory.isSyntheticSuccess(newHomework.getCreateAt()));
            results.add(MapUtils.m(
                    "videoName", dubbing != null ? dubbing.getVideoName() : "",
                    "videoUrl", appAnswer.getVideoUrl(),
                    "synthetic", synthetic));
        }
        return MapMessage.successMessage().add("datas", results);
    }
}
