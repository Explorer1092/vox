package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.OralStarScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/23
 * \* Time: 8:37 PM
 * \* Description: 口语交际
 * \
 */
@Named
public class NewHomeworkResultUpdate_OralCommunication extends NewHomeworkResultUpdateTemplate {

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.ORAL_COMMUNICATION;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();

        Long duration = 0L;
        Double score;
        Double totalScore = 0D;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        if (MapUtils.isEmpty(processResultMap)
                || CollectionUtils.isEmpty(processResultMap.values())
                || CollectionUtils.isEmpty(processResultMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList()))
                ) {
            logger.error("NewHomeworkResultUpdate_OralCommunication : context : {} ", JsonUtils.toJson(context));
            context.getResult().put("NewHomeworkOralCommunication", MapUtils.m("hasError", true));
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
            return;
        }
        for (NewHomeworkProcessResult processResult : processResultMap.values()) {
            answers.put(processResult.getDialogId(), processResult.getId());
            duration += processResult.getDuration();
            totalScore += processResult.getScore() != null ? processResult.getScore() : 0;
        }
        score = processResultMap.values().size() == 0 ? 0 : new BigDecimal(totalScore)
                .divide(new BigDecimal(processResultMap.values().size()), 0, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        List<Map<String, Object>> oralCommunicationScoreLevels = voxScoreLevelHelper.loadVoxOralCommunicationTotalLevel(studentDetail);
        AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(
                oralCommunicationScoreLevels,
                score));
        NewHomeworkResultAppAnswer appAnswer = new NewHomeworkResultAppAnswer();
        appAnswer.setFinishAt(new Date());
        appAnswer.setScore(appOralScoreLevel.getScore());
        appAnswer.setDuration(duration);
        appAnswer.setConsumeTime(context.getConsumeTime());
        appAnswer.setStoneId(context.getStoneId());
        appAnswer.setStoneType(OralCommunicationContentType.valueOf(context.getStoneType()));
        appAnswer.setRoleTopicId(context.getTopicRoleId());
        appAnswer.setAnswers(answers);
        String key = context.getStoneId();
        newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                appAnswer
        );
        OralStarScoreLevel oralStarScoreLevel = OralStarScoreLevel.of(NewHomeworkUtils.processScoreLevel(
                oralCommunicationScoreLevels,
                appOralScoreLevel.getScore()));
        context.getResult().put("oral_communication_result", MapUtils.m("score", appOralScoreLevel.getScore(), "star", oralStarScoreLevel.getStartCount()));
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {

    }
}
