package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
@Lazy(false)
public class VacationHomeworkResultUpdate_Reading extends VacationHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.READING;
    }

    @Override
    public void processVacationHomeworkContext(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        Map<String, VacationHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, VacationHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();
        // 绘本
        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        Double score = 0d;
        Long duration = 0L;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (VacationHomeworkProcessResult npr : processResultMap.values()) {
            score += npr.getScore();
            duration += npr.getDuration();
            answers.put(npr.getQuestionId(), npr.getId());
        }

        // 绘本特殊属性
        LinkedHashMap<String, String> oralAnswers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processOralResultMap)) {
            for (VacationHomeworkProcessResult npr : processOralResultMap.values()) {
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
        vacationHomeworkResultDao.doHomeworkBasicApp(
                location,
                context.getObjectiveConfigType(),
                key,
                nhraa);
    }

    @Override
    public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context) {

    }
}
