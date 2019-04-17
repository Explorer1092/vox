package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

@Named
public class NewHomeworkTypeResultProcessOcrMentalArithmetic extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.OCR_MENTAL_ARITHMETIC;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomework newHomework = newHomeworkLoader.load(baseHomeworkResult.getHomeworkId());
        String workBookName = "";
        String homeworkDetail = "";
        if (newHomework != null && newHomework.getPractices() != null) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
            if (newHomeworkPracticeContent != null) {
                workBookName = newHomeworkPracticeContent.getWorkBookName();
                homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                // 如果返回为多个，则展示首个
                List<String> bookNameList = Arrays.asList(StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                List<String> homeworkDetailList = Arrays.asList(StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                if (length > 1) {
                    workBookName = bookNameList.get(0);
                    homeworkDetail = homeworkDetailList.get(0);
                }
            }
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = resultMap.get(objectiveConfigType);
        List<String> ocrMentalAnswers = newHomeworkResultAnswer.getOcrMentalAnswers();
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(ocrMentalAnswers);
        processResultMap = MapUtils.resort(processResultMap, ocrMentalAnswers);
        List<OcrMentalImageDetail> ocrMentalImageDetails = new ArrayList<>();
        List<String> processIds = new ArrayList<>();
        Integer correctCount = 0;
        Integer pointCount = 0;
        for(SubHomeworkProcessResult processResult : processResultMap.values()){
            ocrMentalImageDetails.add(processResult.getOcrMentalImageDetail());
            processIds.add(processResult.getId());
            for(OcrMentalImageDetail.Form form :processResult.getOcrMentalImageDetail().getForms()){
                if(form.isCorrect()){
                    correctCount++;
                }
            }
            if(processResult.getOcrMentalImageDetail().getOmads() != null && processResult.getOcrMentalImageDetail().getOmads().getItemPoints() != null){
                for(OcrMentalImageDetail.ItemPoint point : processResult.getOcrMentalImageDetail().getOmads().getItemPoints()){
                    pointCount += point.getPoints().size();
                }
            }
        }

        return MapMessage.successMessage()
                .add("workBookName", workBookName)
                .add("homeworkDetail", homeworkDetail)
                .add("results", ocrMentalImageDetails)
                .add("processIds", processIds)
                .add("pointCount", pointCount)
                .add("questionCount", newHomeworkResultAnswer.getOcrMentalQuestionCount())
                .add("errorQuestionCount", newHomeworkResultAnswer.getOcrMentalQuestionCount()-newHomeworkResultAnswer.getOcrMentalCorrectQuestionCount())
                .add("correctCount", correctCount);
    }
}
