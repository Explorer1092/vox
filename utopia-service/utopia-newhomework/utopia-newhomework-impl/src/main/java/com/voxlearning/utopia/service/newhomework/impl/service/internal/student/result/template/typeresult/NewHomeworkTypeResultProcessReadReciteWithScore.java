package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
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

/**
 * @author zhangbin
 * @since 2018/1/8
 */

@Named
public class NewHomeworkTypeResultProcessReadReciteWithScore extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.READ_RECITE_WITH_SCORE;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);

        // 总的达标段落数
        Integer totalStandardNum = 0;
        // 总的段落数
        Integer totalQuestionNum = 0;
        List<String> processResultIds = new ArrayList<>();
        Set<String> lessonIds = new HashSet<>();
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = new LinkedHashMap<>();
        if (resultAnswer != null && MapUtils.isNotEmpty(resultAnswer.getAppAnswers())) {
            appAnswers = resultAnswer.getAppAnswers();
            if (MapUtils.isNotEmpty(appAnswers)) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
                    NewHomeworkResultAppAnswer resultAppAnswer = entry.getValue();
                    if (resultAppAnswer != null) {
                        totalStandardNum += SafeConverter.toInt(resultAppAnswer.getStandardNum());
                        totalQuestionNum += SafeConverter.toInt(resultAppAnswer.getAppQuestionNum());

                        if (MapUtils.isEmpty(resultAppAnswer.getAnswers())) {
                            logger.error("processHomeworkTypeResult READ_RECITE_WITH_SCORE error, homeworkId:{}, studentId:{}",
                                    baseHomeworkResult.getHomeworkId(),
                                    baseHomeworkResult.getUserId());
                        } else {
                            processResultIds.addAll(resultAppAnswer.getAnswers().values());
                        }
                        lessonIds.add(resultAppAnswer.getLessonId());
                    }
                }
            }
        }
        String standardInfo = totalStandardNum + "/" + totalQuestionNum + "个段落达标";

        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        List<Map> readList = new ArrayList<>();
        List<Map> reciteList = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appAnswers.values()) {
            Map<String, Object> map = new HashMap<>();
            NewBookCatalog newBookCatalog = lessonMap.get(nraa.getLessonId());
            // 判断达标
            int standardNum = SafeConverter.toInt(nraa.getStandardNum());
            int appQuestionNum = SafeConverter.toInt(nraa.getAppQuestionNum());
            String standard = standardNum + "/" + appQuestionNum + " 段落达标";

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
            if (QuestionBoxType.READ.equals(nraa.getQuestionBoxType())) {
                readList.add(map);
            } else {
                reciteList.add(map);
            }
        }
        return MapMessage.successMessage()
                .add("standardInfo", standardInfo)
                .add("readList", readList)
                .add("reciteList", reciteList);
    }
}
