package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkTypeResultProcessWordRecognition extends NewHomeworkTypeResultProcessTemplate {

    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.WORD_RECOGNITION_AND_READING;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);

        // 总的达标字数
        Integer totalStandardNum = 0;
        // 总的字数
        Integer totalQuestionNum = 0;
        //作业明细id(homework_process的id)
        List<String> processResultIds = new ArrayList<>();
        Set<String> lessonIds = new HashSet<>();
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = new LinkedHashMap<>();
        if (resultAnswer != null && MapUtils.isNotEmpty(resultAnswer.getAppAnswers())) {
            appAnswers = resultAnswer.getAppAnswers();
            if (MapUtils.isNotEmpty(appAnswers)) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> appAnswerEntry : appAnswers.entrySet()) {
                    NewHomeworkResultAppAnswer resultAppAnswer = appAnswerEntry.getValue();
                    if (resultAppAnswer != null) {
                        totalStandardNum += SafeConverter.toInt(resultAppAnswer.getStandardNum());
                        totalQuestionNum += SafeConverter.toInt(resultAppAnswer.getAppQuestionNum());

                        if (MapUtils.isNotEmpty(resultAppAnswer.getAnswers())) {
                            processResultIds.addAll(resultAppAnswer.getAnswers().values());
                        } else {
                            logger.error("processHomeworkTypeResult WORD_RECOGNITION_AND_READING error, homeworkId:{}, studentId:{}",
                                    baseHomeworkResult.getHomeworkId(),
                                    baseHomeworkResult.getUserId());
                        }
                        lessonIds.add(resultAppAnswer.getLessonId());
                    }
                }
            }
        }
        String standardInfo = totalStandardNum + "/" + totalQuestionNum + "个字达标";
        //做题信息
        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        List<Map> wordReadList = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appAnswers.values()) {
            Map<String, Object> map = new HashMap<>();
            NewBookCatalog newBookCatalog = lessonMap.get(nraa.getLessonId());
            // 判断达标
            int standardNum = SafeConverter.toInt(nraa.getStandardNum());
            int appQuestionNum = SafeConverter.toInt(nraa.getAppQuestionNum());
            String standard = standardNum + "/" + appQuestionNum + " 字达标";
            // 音频信息
            List<String> audios = new LinkedList<>();
            //将音频的播放顺序按照学生答题录音时的顺序展示
            List<String> processIds = nraa.getAnswers()
                    .values()
                    .stream()
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
            for (String processResultId : processIds) {
                NewHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) {
                    continue;
                }
                if (CollectionUtils.isNotEmpty(nhpr.getOralDetails())) {
                    audios.addAll(nhpr
                            .getOralDetails()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(BaseHomeworkProcessResult.OralDetail::getAudio)
                            .collect(Collectors.toList()));
                }
            }
            map.put("lessonId", nraa.getLessonId());
            map.put("lessonName", newBookCatalog == null ? "" : newBookCatalog.getName());
            map.put("standard", standard);
            map.put("audios", audios);
            wordReadList.add(map);
        }
        return MapMessage.successMessage()
                .add("standardInfo", standardInfo)
                .add("wordReadList", wordReadList);
    }
}
