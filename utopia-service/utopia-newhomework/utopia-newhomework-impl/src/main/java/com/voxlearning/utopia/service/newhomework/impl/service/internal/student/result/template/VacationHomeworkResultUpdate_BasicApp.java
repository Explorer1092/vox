package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
@Lazy(false)
public class VacationHomeworkResultUpdate_BasicApp extends VacationHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.BASIC_APP;
    }

    @Override
    public void processVacationHomeworkContext(VacationHomeworkResultContext context) {
        // 假期作业不考虑一题一题提交
        processBasicAppOnePractice(context);
    }

    @Override
    public void checkVacationHomeworkAppFinish(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        VacationHomeworkResult vacationHomeworkResult = context.getVacationHomeworkResult();
        if (vacationHomeworkResult == null) return;
        Integer categoryId = context.getPracticeType().getCategoryId() != null ? context.getPracticeType().getCategoryId() : 0;
        String lessonId = context.getLessonId();

        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        List<String> processIds = vacationHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId.toString(), lessonId, objectiveConfigType);
        if (CollectionUtils.isNotEmpty(processIds)) {
            String key = StringUtils.join(Arrays.asList(context.getPracticeType().getCategoryId(), context.getLessonId()), "-");
            NewHomeworkResultAppAnswer appAnswer = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP).getAppAnswers().get(key);
            boolean finished = SafeConverter.toBoolean(context.getFinished());
            // 校验是否真的没完成
            if (finished || validatePracticeFinished(context.getVacationHomework(), appAnswer, categoryId, lessonId)) {
                // 布置的题目和做过的题一致，将剩下的属性补全
                Map<String, VacationHomeworkProcessResult> processResultMap = vacationHomeworkProcessResultDao.loads(processIds);
                Double score = 0d;
                Long duration = 0L;
                for (VacationHomeworkProcessResult npr : processResultMap.values()) {
                    score += npr.getScore();
                    duration += npr.getDuration();
                }
                Double avgScore = score;
                //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
                if (context.getPracticeType().getNeedRecord()) {
                    avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                vacationHomeworkResultDao.finishHomeworkBasicAppPractice(location, context.getObjectiveConfigType(), key, avgScore, duration);
            }
        }
    }

    /**
     * 用于处理基础训练一个练习整体提交的情况
     */
    private void processBasicAppOnePractice(VacationHomeworkResultContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        Map<String, VacationHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, VacationHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();

        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        Double score = 0d;
        Long duration = 0L;
        boolean allQuestionsRight = true;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (VacationHomeworkProcessResult npr : processResultMap.values()) {
            score += npr.getScore();
            duration += npr.getDuration();
            answers.put(npr.getQuestionId(), npr.getId());
            if (!SafeConverter.toBoolean(npr.getGrasp())) {
                allQuestionsRight = false;
            }
        }

        //当题目全部正确时，但是总分计算结果不是100分就把总分设置为100分
        if (allQuestionsRight && score != null && score < 100D && !context.getPracticeType().fetchNeedRecord()) {
            score = 100D;
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
        //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
        if ((ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType()))
                && context.getPracticeType().getNeedRecord()) {
            avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        nhraa.setScore(avgScore);
        nhraa.setDuration(duration);
        nhraa.setLessonId(context.getLessonId());
        nhraa.setPracticeId(context.getPracticeId());
        nhraa.setPracticeName(context.getPracticeType().getPracticeName());
        nhraa.setCategoryId(context.getPracticeType().getCategoryId());

        nhraa.setAnswers(answers);
        nhraa.setOralAnswers(oralAnswers);
        String key = StringUtils.join(Arrays.asList(context.getPracticeType().getCategoryId(), context.getLessonId()), "-");
        vacationHomeworkResultDao.doHomeworkBasicApp(
                location,
                context.getObjectiveConfigType(),
                key,
                nhraa);
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(VacationHomework vacationHomework, NewHomeworkResultAppAnswer appAnswer, Integer categoryId, String lessonId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = vacationHomework.findNewHomeworkQuestions(ObjectiveConfigType.BASIC_APP, lessonId, categoryId);
        if (CollectionUtils.isNotEmpty(questionList)) {
            Set<String> homeworkQids = questionList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQids = appAnswer.getAnswers().keySet();
            result = CollectionUtils.isEqualCollection(homeworkQids, resultQids);
        }
        return result;
    }
}
