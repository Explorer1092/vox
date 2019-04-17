package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/7/10
 */
@Named
@Lazy(false)
public class LiveCastHomeworkResultUpdate_BasicApp extends LiveCastHomeworkResultUpdateTemplate {

    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;
    @Inject private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.BASIC_APP;
    }

    @Override
    public void processLiveCastHomeworkContext(LiveCastHomeworkResultContext context) {
        processBasicAppOnePractice(context);
    }

    @Override
    public void checkLiveCastHomeworkAppFinish(LiveCastHomeworkResultContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        LiveCastHomeworkResult homeworkResult = context.getLiveCastHomeworkResult();
        if (homeworkResult == null) return;
        Integer categoryId = context.getPracticeType().getCategoryId() != null ? context.getPracticeType().getCategoryId() : 0;
        String lessonId = context.getLessonId();

        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        List<String> processIds = homeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId.toString(), lessonId, objectiveConfigType);
        if (CollectionUtils.isNotEmpty(processIds)) {
            String key = StringUtils.join(Arrays.asList(context.getPracticeType().getCategoryId(), context.getLessonId()), "-");
            NewHomeworkResultAppAnswer appAnswer = homeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP).getAppAnswers().get(key);
            boolean finished = SafeConverter.toBoolean(context.getFinished());
            // 校验是否真的没完成
            if (finished || validatePracticeFinished(context.getLiveCastHomework(), appAnswer, categoryId, lessonId)) {
                // 布置的题目和做过的题一致，将剩下的属性补全
                Map<String, LiveCastHomeworkProcessResult> processResultMap = liveCastHomeworkProcessResultDao.loads(processIds);
                Double score = 0d;
                Long duration = 0L;
                for (LiveCastHomeworkProcessResult npr : processResultMap.values()) {
                    score += npr.getScore();
                    duration += npr.getDuration();
                }
                Double avgScore = score;
                //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
                if (context.getPracticeType().getNeedRecord()) {
                    avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                liveCastHomeworkResultDao.finishHomeworkBasicAppPractice(location, context.getUserId(), context.getObjectiveConfigType(), key, avgScore, duration);
            }
        }
    }

    /**
     * 用于处理基础训练一个练习整体提交的情况
     */
    private void processBasicAppOnePractice(LiveCastHomeworkResultContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        Map<String, LiveCastHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, LiveCastHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();

        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        Double score = 0d;
        Long duration = 0L;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        boolean allQuestionsRight = true;
        for (LiveCastHomeworkProcessResult npr : processResultMap.values()) {
            score += npr.getScore();
            duration += npr.getDuration();
            answers.put(npr.getQuestionId(), npr.getId());
            if (!SafeConverter.toBoolean(npr.getGrasp())) {
                allQuestionsRight = false;
            }
        }

        // 绘本特殊属性
        LinkedHashMap<String, String> oralAnswers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processOralResultMap)) {
            for (LiveCastHomeworkProcessResult npr : processOralResultMap.values()) {
                // 绘本的跟读题时间计入
                duration += npr.getDuration();
                oralAnswers.put(npr.getQuestionId(), npr.getId());
            }
        }

        //当题目全部正确时，但是总分计算结果不是100分就把总分设置为100分
        if (allQuestionsRight && score < 100D && !context.getPracticeType().fetchNeedRecord()) {
            score = 100D;
        }

        nhraa.setFinishAt(new Date());
        Double avgScore = score;
        //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType()) && context.getPracticeType().getNeedRecord()) {
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
        liveCastHomeworkResultDao.doHomeworkBasicApp(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                nhraa);
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(LiveCastHomework homework, NewHomeworkResultAppAnswer appAnswer, Integer categoryId, String lessonId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = homework.findNewHomeworkQuestions(ObjectiveConfigType.BASIC_APP, lessonId, categoryId);
        if (CollectionUtils.isNotEmpty(questionList)) {
            Set<String> homeworkQuestionIds = questionList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQuestionIds = appAnswer.getAnswers().keySet();
            result = CollectionUtils.isEqualCollection(homeworkQuestionIds, resultQuestionIds);
        }
        return result;
    }
}
