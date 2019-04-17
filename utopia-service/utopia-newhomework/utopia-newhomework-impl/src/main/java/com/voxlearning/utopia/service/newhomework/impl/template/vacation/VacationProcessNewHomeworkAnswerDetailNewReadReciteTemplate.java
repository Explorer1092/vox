package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecite.ReadReciteBasicData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecite.ReadReciteData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.report.ProcessNewHomeworkAnswerDetailNewReadReciteTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class VacationProcessNewHomeworkAnswerDetailNewReadReciteTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.NEW_READ_RECITE;
    }

    @Inject
    private ProcessNewHomeworkAnswerDetailNewReadReciteTemplate processNewHomeworkAnswerDetailNewReadReciteTemplate;

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
        VacationHomeworkResult newHomeworkResult = reportRateContext.getNewHomeworkResult();
        if (!newHomeworkResult.getPractices().containsKey(ObjectiveConfigType.NEW_READ_RECITE))
            return;
        Map<String, NewQuestion> allNewQuestionMap = reportRateContext.getAllNewQuestionMap();
        VacationHomework newHomework = reportRateContext.getNewHomework();
        User user = reportRateContext.getUser();
        Map<String, VacationHomeworkProcessResult> newHomeworkProcessResultMap = reportRateContext.getNewHomeworkProcessResultMap();
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.NEW_READ_RECITE);
        ReadReciteData readReciteData = new ReadReciteData();
        Map<String, ReadReciteBasicData> readReciteBasicDataMap = new HashMap<>();

        processNewHomeworkAnswerDetailNewReadReciteTemplate.handleChineseReadRecite(target, allNewQuestionMap, readReciteData, readReciteBasicDataMap);

        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.NEW_READ_RECITE);

        for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = entry.getValue();
            if (!readReciteBasicDataMap.containsKey(entry.getKey()))
                continue;
            ReadReciteBasicData readReciteBasicData = readReciteBasicDataMap.get(entry.getKey());

            ReadReciteBasicData.UserVoice userVoice = new ReadReciteBasicData.UserVoice();
            readReciteBasicData.getUsers().add(userVoice);

            LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();

            List<String> voiceUrls = new LinkedList<>();

            readReciteBasicData.setPersonalVoiceToApp(voiceUrls);

            for (ReadReciteBasicData.ParagraphDetailed paragraphDetailed : readReciteBasicData.getParagraphDetaileds()) {
                if (!answers.containsKey(paragraphDetailed.getQuestionId()))
                    continue;
                String processId = answers.get(paragraphDetailed.getQuestionId());
                if (!newHomeworkProcessResultMap.containsKey(processId))
                    continue;
                VacationHomeworkProcessResult newHomeworkProcessResult = newHomeworkProcessResultMap.get(processId);
                if (newHomeworkProcessResult != null) {
                    paragraphDetailed.setPersonalVoiceToParagraph(CollectionUtils.isNotEmpty(newHomeworkProcessResult.getFiles()) ?
                            newHomeworkProcessResult
                                    .getFiles()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                    .collect(Collectors.toList()) :
                            Collections.emptyList());
                    voiceUrls.addAll(paragraphDetailed.getPersonalVoiceToParagraph());
                }

            }
            userVoice.setUserId(reportRateContext.getNewHomeworkResult().getUserId());
            int time = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                    .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                    .intValue();

            String duration = NewHomeworkUtils.handlerEnTime(time);
            userVoice.setDuration(duration);

            userVoice.setUserName(user.fetchRealnameIfBlankId());

            userVoice.setShowPics(voiceUrls);

            userVoice.setReview(SafeConverter.toBoolean(newHomeworkResultAppAnswer.getReview()));

            userVoice.setCorrection(SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection(), ""));

            if (userVoice.isReview()) {
                userVoice.setCorrect_des(newHomeworkResultAppAnswer.getCorrection() != null ? SafeConverter.toString(newHomeworkResultAppAnswer.getCorrection().getDescription(), "") : "阅");
            } else {
                userVoice.setCorrect_des("未批改");
            }
            if (newHomeworkResultAppAnswer.getCorrection() != null) {
                readReciteBasicData.setCorrectionInfo(newHomeworkResultAppAnswer.getCorrection().getDescription());
                readReciteBasicData.setCorrected(true);
            } else if (newHomeworkResultAppAnswer.getReview() != null) {
                readReciteBasicData.setCorrected(true);
                readReciteBasicData.setCorrectionInfo("阅");
            } else {
                readReciteBasicData.setCorrectionInfo("未批改");
            }
        }
        reportRateContext.getResultMap().put(reportRateContext.getType(), readReciteData);
    }

}
