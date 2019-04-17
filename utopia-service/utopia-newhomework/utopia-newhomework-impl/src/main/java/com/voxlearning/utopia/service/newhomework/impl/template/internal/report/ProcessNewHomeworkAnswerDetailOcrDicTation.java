package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.OcrDictationStudent;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ocrmentalarithmetic.OcrMentalArithmeticData;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/25
 * \* Time: 6:47 PM
 * \* Description:
 * \
 */
@Named
public class ProcessNewHomeworkAnswerDetailOcrDicTation extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.OCR_DICTATION;
    }

    /**
     * 处理个人答题详情
     *
     * @param reportRateContext
     */
    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        if (reportRateContext.getNewHomeworkResult() != null && reportRateContext.getNewHomeworkResult().getPractices().get(type) != null) {
            NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(reportRateContext.getNewHomework().getId());
            String unitInfo="";
            if (newHomeworkBook != null && MapUtils.isNotEmpty(newHomeworkBook.getPractices()) && CollectionUtils.isNotEmpty(newHomeworkBook.getPractices().get(type))) {
                List<NewHomeworkBookInfo> bookInfoList = newHomeworkBook.getPractices().get(type);
                List<String> unitList=bookInfoList.stream().map(NewHomeworkBookInfo::getUnitName).distinct().collect(Collectors.toList());
                unitInfo= StringUtils.join(unitList,",");
            }
            OcrMentalArithmeticData ocrMentalArithmeticData = new OcrMentalArithmeticData();
            ocrMentalArithmeticData.setWorkBookName(unitInfo);
            ocrMentalArithmeticData.setHomeworkDetail("");
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
        List<OcrDictationStudent> ocrDictationStudentList = Lists.newArrayList();
        for (User user : userMap.values()) {
            OcrDictationStudent student = new OcrDictationStudent();
            student.setUserId(user.getId());
            student.setUserName(user.fetchRealnameIfBlankId());
            if (resultMap.containsKey(user.getId())) {
                NewHomeworkResult newHomeworkResult = resultMap.get(user.getId());
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(context.getType());
                student.setScore(newHomeworkResultAnswer.processScore(context.getType()));
                student.setIdentifyCount(newHomeworkResultAnswer.getOcrDictationQuestionCount());
                student.setErrorCount(newHomeworkResultAnswer.getOcrDictationQuestionCount() - newHomeworkResultAnswer.getOcrDictationCorrectQuestionCount());
                student.setManualCorrect(newHomeworkResultAnswer.isCorrected());
                student.setFinished(newHomeworkResultAnswer.isFinished());
                student.setResultUrl(UrlUtils.buildUrlQuery("/teacher/new/homework/report/type/result" + Constants.AntiHijackExt,
                        MapUtils.m("homeworkId", context.getNewHomework().getId(),
                                "studentId", user.getId(),
                                "objectiveConfigType", context.getType())));
            }
            ocrDictationStudentList.add(student);
        }
        ocrDictationStudentList.sort(OcrDictationStudent.comparator);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("ocrOcrDictationStudents", ocrDictationStudentList);
        context.setMapMessage(mapMessage);
    }
}
