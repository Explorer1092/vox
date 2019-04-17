package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 新绘本阅读
 * @author: Mr_VanGogh
 * @date: 2018/5/30 下午5:41
 */
@Named
public class VacationHomeworkResultUpdate_LevleReadings extends VacationHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.LEVEL_READINGS;
    }

    @Override
    public void processVacationHomeworkContext(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        Map<String, VacationHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, VacationHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();
        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();

        long duration = context.getDurations().values()
                .stream()
                .mapToLong(SafeConverter::toLong)
                .sum();

        if (duration > NewHomeworkConstants.LEVEL_READINGS_MAX_DURATION_MILLISECONDS) {
            duration = NewHomeworkConstants.LEVEL_READINGS_MAX_DURATION_MILLISECONDS;
        }

        boolean allQuestionRight = true;

        //新绘本阅读 基准分数:60
        Double score = 60D;
        LinkedHashMap<String, String> answerMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processResultMap)) {
            for (VacationHomeworkProcessResult result : processResultMap.values()) {
                score += result.getScore();
                answerMap.put(result.getQuestionId(), result.getId());
                if (!SafeConverter.toBoolean(result.getGrasp())) {
                    allQuestionRight = false;
                }
            }
        }

        //绘本阅读特殊属性
        LinkedHashMap<String, String> oralAnswerMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processOralResultMap)) {
            for (VacationHomeworkProcessResult result : processOralResultMap.values()) {
                score += result.getScore();
                oralAnswerMap.put(result.getQuestionId(), result.getId());
                if (AppOralScoreLevel.A != result.getAppOralScoreLevel()) {
                    allQuestionRight = false;
                }
            }
        }

        if (allQuestionRight) {
            score = 100D;
        }

        //score = score > 100D ? 100D : score;
        if (score > 100D) {
            score = 100D;
        }

        String pictureBookId = context.getPictureBookId();
        nhraa.setFinishAt(new Date());
        nhraa.setScore(score);
        nhraa.setDuration(duration);
        nhraa.setPictureBookId(pictureBookId);
        nhraa.setConsumeTime(context.getConsumeTime());
        nhraa.setAnswers(answerMap);
        nhraa.setOralAnswers(oralAnswerMap);
        nhraa.setDurations(context.getDurations());
        nhraa.setDubbingId(context.getDubbingId());
        nhraa.setDubbingScore(context.getDubbingScore());
        nhraa.setDubbingScoreLevel(context.getDubbingScoreLevel());
        vacationHomeworkResultDao.doHomeworkBasicApp(
                location,
                context.getObjectiveConfigType(),
                pictureBookId,
                nhraa
        );
    }

    @Override
    public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context) {

    }
}
