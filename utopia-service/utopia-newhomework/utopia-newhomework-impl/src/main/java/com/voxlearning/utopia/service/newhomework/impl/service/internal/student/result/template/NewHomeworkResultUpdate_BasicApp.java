package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guohong.tan
 * @since 2016/11/23
 */
@Named
public class NewHomeworkResultUpdate_BasicApp extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.BASIC_APP;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
//        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();

//        // 从作业题量和做题量上判断单题与多题提交
//        if (CollectionUtils.isNotEmpty(processResultMap.values())) {
        processBasicAppOnePractice(context);
//        } else {
//            processBasicAppOneQuestion(context);
//        }
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {
//        NewHomework.Location location = context.getHomework().toLocation();
//        NewHomeworkResult newHomeworkResult = context.getNewHomeworkResult();
//        if(newHomeworkResult == null) return;
//        Integer categoryId = context.getPracticeType().getCategoryId() != null ? context.getPracticeType().getCategoryId() : 0;
//        String lessonId = context.getLessonId();
//
//        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(categoryId.toString(), lessonId);
//        if (CollectionUtils.isNotEmpty(processIds)) {
//            String key = StringUtils.join(Arrays.asList(context.getPracticeType().getCategoryId(), context.getLessonId()), "-");
//            NewHomeworkResultAppAnswer appAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP).getAppAnswers().get(key);
//            boolean finished = SafeConverter.toBoolean(context.getFinished());
//            // 校验是否真的没完成
//            if (finished || validatePracticeFinished(context.getHomework(), appAnswer, categoryId, lessonId)) {
//                // 布置的题目和做过的题一致，将剩下的属性补全
//                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(context.getHomeworkId(), processIds);
//                Double score = 0d;
//                Long duration = 0L;
//                for (NewHomeworkProcessResult npr : processResultMap.values()) {
//                    score += npr.getScore();
//                    duration += npr.getDuration();
//                }
//                Double avgScore = score;
//                //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
//                if (context.getPracticeType().getNeedRecord()) {
//                    avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                }
//                newHomeworkResultService.finishHomeworkBasicAppPractice(location, context.getUserId(), context.getObjectiveConfigType(), key, avgScore, duration);
//            }
//        }
    }

    /**
     * 用于处理基础训练一题一题提交
     */
//    private void processBasicAppOneQuestion(HomeworkResultContext context) {
//        NewHomework.Location location = context.getHomework().toLocation();
//        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();
//
//        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
//        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
//        for (NewHomeworkProcessResult npr : processResultMap.values()) {
//            answers.put(npr.getQuestionId(), npr.getId());
//        }
//
//        nhraa.setLessonId(context.getLessonId());
//        nhraa.setPracticeId(context.getPracticeId());
//        nhraa.setPracticeName(context.getPracticeType().getPracticeName());
//        nhraa.setCategoryId(context.getPracticeType().getCategoryId());
//
//        nhraa.setAnswers(answers);
//        String key = StringUtils.join(Arrays.asList(context.getPracticeType().getCategoryId(), context.getLessonId()), "-");
//        NewHomeworkResult newHomeworkResult = newHomeworkResultDao.doHomeworkBasicAppPractice(
//                location,
//                context.getUserId(),
//                context.getObjectiveConfigType(),
//                key,
//                nhraa);
//
//        context.setNewHomeworkResult(newHomeworkResult);
//        context.setIsOneByOne(true);
//    }

    /**
     * 用于处理基础训练一个练习整体提交的情况
     */
    private void processBasicAppOnePractice(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, NewHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();

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
        if (allQuestionsRight && score != null && score < 100D && !context.getPracticeType().fetchNeedRecord()) {
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
        //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
        if ((ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType()))
                && context.getPracticeType().fetchNeedRecord()) {
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
        newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                nhraa);
        context.setIsOneByOne(false);
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
//    private boolean validatePracticeFinished(NewHomework newHomework, NewHomeworkResultAppAnswer appAnswer, Integer categoryId, String lessonId) {
//        boolean result = false;
//        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkQuestions(ObjectiveConfigType.BASIC_APP, lessonId, categoryId);
//        if (CollectionUtils.isNotEmpty(questionList)) {
//            Set<String> homeworkQids = questionList.stream()
//                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
//                    .map(NewHomeworkQuestion::getQuestionId)
//                    .collect(Collectors.toSet());
//            Set<String> resultQids = appAnswer.getAnswers().keySet();
//            result = CollectionUtils.isEqualCollection(homeworkQids, resultQids);
//        }
//        return result;
//    }
}
