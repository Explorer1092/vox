package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.OcrMentalArithmeticStudent;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ocrmentalarithmetic.OcrMentalArithmeticData;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named
public class ProcessNewHomeworkAnswerDetailOcrMentalArithmetic extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.OCR_MENTAL_ARITHMETIC;
    }

    /**
     * 处理个人答题详情
     *
     * @param reportRateContext
     */
    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        if(reportRateContext.getNewHomeworkResult() != null && reportRateContext.getNewHomeworkResult().getPractices().get(type) != null){
            NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
            OcrMentalArithmeticData ocrMentalArithmeticData = new OcrMentalArithmeticData();
            ocrMentalArithmeticData.setWorkBookName(target.getWorkBookName());
            ocrMentalArithmeticData.setHomeworkDetail(target.getHomeworkDetail());
            ocrMentalArithmeticData.setResultUrl(UrlUtils.buildUrlQuery("/teacher/new/homework/report/type/result" + Constants.AntiHijackExt,
                    MapUtils.m("homeworkId", reportRateContext.getNewHomework().getId(),
                            "studentId", reportRateContext.getUser().getId(),
                            "objectiveConfigType", type)));
            NewHomeworkResultAnswer resultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);
            ocrMentalArithmeticData.setScore(resultAnswer.processScore(type));
            ocrMentalArithmeticData.setCorrected(resultAnswer.getCorrectedAt() != null);
            reportRateContext.getResultMap().put(type, ocrMentalArithmeticData);
        }


    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
       Map<Long, User> userMap = context.getUserMap();
       Map<Long, NewHomeworkResult> resultMap = context.getNewHomeworkResultMap();
       List<OcrMentalArithmeticStudent> ocrMentalArithmeticStudents = new ArrayList<>();
       for(User user : userMap.values()){
           OcrMentalArithmeticStudent omas = new OcrMentalArithmeticStudent();
           omas.setUserId(user.getId());
           omas.setUserName(user.fetchRealnameIfBlankId());
           if(resultMap.containsKey(user.getId())){
               NewHomeworkResult newHomeworkResult = resultMap.get(user.getId());
               NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(context.getType());
               omas.setScore(newHomeworkResultAnswer.processScore(context.getType()));
               omas.setIdentifyCount(newHomeworkResultAnswer.getOcrMentalQuestionCount());
               omas.setErrorCount(newHomeworkResultAnswer.getOcrMentalQuestionCount() - newHomeworkResultAnswer.getOcrMentalCorrectQuestionCount());
               omas.setManualCorrect(newHomeworkResultAnswer.isCorrected());
               omas.setFinished(newHomeworkResultAnswer.isFinished());
               omas.setResultUrl(UrlUtils.buildUrlQuery("/teacher/new/homework/report/type/result" + Constants.AntiHijackExt,
                       MapUtils.m("homeworkId", context.getNewHomework().getId(),
                               "studentId", user.getId(),
                               "objectiveConfigType", context.getType())));
           }
           ocrMentalArithmeticStudents.add(omas);
       }
       ocrMentalArithmeticStudents.sort(OcrMentalArithmeticStudent.comparator);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("ocrMentalArithmeticStudents", ocrMentalArithmeticStudents);
        context.setMapMessage(mapMessage);
    }
}
