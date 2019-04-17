package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.OralDetailBranchInformation;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.OralPracticeAnswer;
import com.voxlearning.utopia.service.newhomework.impl.template.VacationInternalProcessHomeworkAnswerTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class VacationInternalProcessHomeworkAnswerForOralPracticeTemple extends VacationInternalProcessHomeworkAnswerTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_PRACTICE;
    }


    @Override
    public void internalProcessHomeworkAnswer(Map<ObjectiveConfigType, Object> resultMap, Map<String, VacationHomeworkProcessResult> allProcessResultMap, Map<Integer, NewContentType> contentTypeMap, Map<String, NewQuestion> allQuestionMap, VacationHomework vacationHomework, VacationHomeworkResult vacationHomeworkResult, ObjectiveConfigType type) {
        List<OralPracticeAnswer> value = new LinkedList<>();
        List<String> oralPracticeQuestionIds = vacationHomework.findQuestionIds(ObjectiveConfigType.ORAL_PRACTICE, false);
        NewHomeworkResultAnswer newHomeworkResultAnswer = vacationHomeworkResult.getPractices().get(type);
        if (newHomeworkResultAnswer == null)
            return;
        LinkedHashMap<String, String> answers = newHomeworkResultAnswer.processAnswers();
        for (String qid : oralPracticeQuestionIds) {
            if (!allQuestionMap.containsKey(qid))
                continue;
            if (!answers.containsKey(qid))
                continue;
            String processId = answers.get(qid);
            if (!allProcessResultMap.containsKey(processId))
                continue;
            NewQuestion question = allQuestionMap.get(qid);
            VacationHomeworkProcessResult pr = allProcessResultMap.get(processId);
            OralPracticeAnswer oralPracticeAnswer = new OralPracticeAnswer();
            int showType = 0;
            if (question.getSubmitWays() == null)
                continue;
            Set<Integer> submitWays = question
                    .getSubmitWays()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(submitWays) &&
                    submitWays.contains(1)) {
                showType = 1;
            } else if (CollectionUtils.isNotEmpty(submitWays) &&
                    submitWays.contains(2)) {
                showType = 2;
            }
            oralPracticeAnswer.setQid(qid);
            oralPracticeAnswer.setStore(SafeConverter.toDouble(pr.getScore()));
            oralPracticeAnswer.setContentType(contentTypeMap.get(question.getContentTypeId()) != null ?
                    contentTypeMap.get(question.getContentTypeId()).getName() : "无题型");
            oralPracticeAnswer.setDifficulty(question.getDifficultyInt());
            oralPracticeAnswer.setShowType(showType);
            List<List<BaseHomeworkProcessResult.OralDetail>> oralDetails = pr.getOralDetails();
            for (List<BaseHomeworkProcessResult.OralDetail> oralDetailsForBranches : oralDetails) {
                OralDetailBranchInformation oralDetailBranchInformation = new OralDetailBranchInformation();
                List<String> userVoiceUrls = new LinkedList<>();
                oralDetailBranchInformation.setUserVoiceUrls(userVoiceUrls);
                double totalOralScore = 0;
                for (BaseHomeworkProcessResult.OralDetail or : oralDetailsForBranches) {
                    String voiceUrl = or.getAudio();
                    if (StringUtils.isNotEmpty(voiceUrl)) {
                        VoiceEngineType voiceEngineType = pr.getVoiceEngineType();
                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                        userVoiceUrls.add(voiceUrl);
                    }
                    totalOralScore += SafeConverter.toDouble(or.getOralScore());
                }
                int realScore = oralDetailsForBranches.size() > 0 ?
                        new BigDecimal(totalOralScore)
                                .divide(new BigDecimal(oralDetailsForBranches.size()), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue() :
                        0;
                String score = oralDetailsForBranches.size() == 1 ?
                        oralDetailsForBranches.get(0).getOralScoreInterval() :
                        realScore + "";

                oralDetailBranchInformation.setScore(score);
                oralDetailBranchInformation.setRealScore(realScore);
                oralPracticeAnswer.getAnswerList().add(oralDetailBranchInformation);
            }
            value.add(oralPracticeAnswer);
        }
        if (CollectionUtils.isNotEmpty(value)) {
            resultMap.put(type, value);
        }
    }
}
