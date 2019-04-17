package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2016/11/23
 */
@Named
public class NewHomeworkResultUpdate_Reading extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.READING;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {

        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, NewHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();
        // 绘本
        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        Double score = 0d;
        Long duration = 0L;
        boolean allQuestionsRight = true;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (NewHomeworkProcessResult npr : processResultMap.values()) {
            score += npr.getScore();
            duration += npr.getDuration();
            answers.put(npr.getQuestionId(), npr.getId());
            if (!SafeConverter.toBoolean(npr.getGrasp())) {
                allQuestionsRight = false;
            }
        }
        //当题目全部正确时，但是总分计算结果不是100分就把总分设置为100分
        if (allQuestionsRight && score != null && score < 100D) {
            score = 100D;
        }

        // 绘本特殊属性
        LinkedHashMap<String, String> oralAnswers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processOralResultMap)) {
            for (NewHomeworkProcessResult npr : processOralResultMap.values()) {
                // 绘本的跟读题时间计入
                duration += npr.getDuration();
                oralAnswers.put(npr.getQuestionId(), npr.getId());
            }
        }

        nhraa.setFinishAt(new Date());
        Double avgScore = score;
        nhraa.setScore(avgScore);
        nhraa.setDuration(duration);
        nhraa.setPictureBookId(context.getPictureBookId());
        nhraa.setConsumeTime(context.getConsumeTime());
        nhraa.setAnswers(answers);
        nhraa.setOralAnswers(oralAnswers);
        String key = context.getPictureBookId();
        newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                nhraa);
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {

    }
}
